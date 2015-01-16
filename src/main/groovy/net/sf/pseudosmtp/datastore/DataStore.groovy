/*
 Copyright 2015 Battams, Derek
 
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
 
		http://www.apache.org/licenses/LICENSE-2.0
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package net.sf.pseudosmtp.datastore

import groovy.sql.Sql
import groovy.util.logging.Log4j

import java.sql.SQLException

import javax.mail.Message.RecipientType
import javax.mail.internet.MimeMessage

import net.sf.pseudosmtp.AppSettings
import net.sf.pseudosmtp.j2ee.listeners.SmtpManager

import org.apache.commons.io.FilenameUtils
import org.apache.commons.mail.util.MimeMessageParser

@Log4j
class DataStore {
	static private DataStore INSTANCE = null
	synchronized static DataStore getInstance() {
		if(!INSTANCE)
			INSTANCE = new DataStore()
		return INSTANCE
	}

	static private final String DB_NAME = "${Boolean.parseBoolean(System.getProperty('psmtp.testing')) ? 'memory:' : ''}/${FilenameUtils.separatorsToUnix(FilenameUtils.getPath(new File(AppSettings.instance.appRoot, 'psmtp').absolutePath))}psmtp"

	synchronized static void shutdown() {
		if(INSTANCE?.sql)
			try { INSTANCE.sql.close() } catch(Throwable t) {}
		INSTANCE = null
			
		try {
			Sql.newInstance("jdbc:derby:${DB_NAME};${DB_NAME.startsWith('memory:') ? 'drop' : 'shutdown'}=true")
		} catch(SQLException e) {
			if(false && e.SQLState != '08006') {
				log.error 'SQLError shutting down db!', e
				throw e
			}
		}
		log.info 'Database shutdown completed.'
	}

	private Sql sql = null
	
	private DataStore() {
		def jdbcStr = "jdbc:derby:${DB_NAME};create=true"
		log.info "Connecting to database: $jdbcStr"
		sql = Sql.newInstance(jdbcStr, 'org.apache.derby.jdbc.EmbeddedDriver')
		if(!sql.connection.warnings) {
			createTables()
			setDbVersion()
			log.info 'New database created'
		} else
			log.info 'Connected to existing database'
	}

	synchronized List findByClient(String clnt, QueryBuilder qb = null) {
		def qry
		def params
		if(!qb) {
			qry = 'SELECT id FROM message WHERE client = ?'
			params = [clnt]
		} else {
			qry = new StringBuilder('SELECT DISTINCT m.id FROM message AS m LEFT OUTER JOIN recipients AS r ON m.id = r.id LEFT OUTER JOIN headers AS h ON m.id = h.id LEFT OUTER JOIN attachments AS a ON m.id = a.id WHERE client = ? AND ')
			qry.append(qb.toString())
			params = [clnt]
			params.addAll(qb.parameters)
			qry = qry.toString()
		}	
		if(log.isTraceEnabled())
			log.trace "$qry $params"
		List ids = []
		sql.eachRow(qry, params) {
			ids << it[0]
		}
		return ids
	}
	
	synchronized void deleteAllByClient(String clnt) {
		def qry = "DELETE FROM message WHERE client = $clnt"
		if(log.isTraceEnabled()) {
			def params = sql.getParameters(qry)
			def qryStr = sql.asSql(qry, params)
			log.trace "$qryStr $params"
		}
		sql.execute qry
	}
	
	synchronized boolean deleteMessage(long id, String clnt) {
		def qry = "DELETE FROM message WHERE client = $clnt AND id = $id"
		if(log.isTraceEnabled()) {
			def params = sql.getParameters(qry)
			def qryStr = sql.asSql(qry, params)
			log.trace "$qryStr $params"
		}
		return sql.executeUpdate(qry) == 1
	}
	
	synchronized InputStream findById(long id, String clnt) {
		def qry = "SELECT type, email, data FROM message AS m LEFT OUTER JOIN recipients AS r ON m.id = r.id WHERE m.id = $id AND client = $clnt"
		if(log.isTraceEnabled()) {
			def params = sql.getParameters(qry)
			def qryStr = sql.asSql(qry, params)
			log.trace "$qryStr $params"
		}
		def data = null
		def bcc = []
		sql.eachRow(qry) {
			if(!data)
				data = new ByteArrayInputStream(it.data.binaryStream.bytes)
			if(it.type == 'bcc' && it.email != null)
				bcc << it.email
		}
		if(bcc.size() > 0)
			log.debug "Message #$id BCC: ${bcc.join(',')}"
		return data ? buildMsg(data, bcc) : null
	}
	
	synchronized String getSetting(String name, String defaultValue = null) {
		def qry = "SELECT value FROM settings WHERE name = $name"
		if(log.isTraceEnabled()) {
			def params = sql.getParameters(qry)
			def qryStr = sql.asSql(qry, params)
			log.trace "$qryStr $params"
		}
		return sql.firstRow(qry)?.value ?: defaultValue
	}
	
	synchronized void setSetting(String name, String value) {
		def delQry = "DELETE FROM settings WHERE name = $name"
		def insQry = "INSERT INTO settings (name, value) VALUES ($name, $value)"
		if(log.isTraceEnabled()) {
			def params = sql.getParameters(delQry)
			def qryStr = sql.asSql(delQry, params)
			log.trace "$qryStr $params"
			params = sql.getParameters(insQry)
			qryStr = sql.asSql(insQry, params)
			log.trace "$qryStr $params"
		}
		sql.withTransaction {
			sql.execute delQry
			if(sql.executeUpdate(insQry) != 1)
				throw new RuntimeException('DBError writing setting value')
		}
	}
	
	private InputStream buildMsg(InputStream data, List bcc) {
		if(!bcc || bcc.size() == 0) return data
		MimeMessage msg = new MimeMessage(SmtpManager.SMTP_SESSION, data)
		msg.addRecipients(RecipientType.BCC, bcc.join(','))
		def os = new ByteArrayOutputStream()
		msg.writeTo(os)
		return new ByteArrayInputStream(os.toByteArray())
	}
	
	synchronized void insertMessage(String clnt, InputStream msg, String from, List recipients) {
		def mimeBytes = msg.bytes
		MimeMessage mimeMsg = new MimeMessage(SmtpManager.SMTP_SESSION, new ByteArrayInputStream(mimeBytes))
		List to = []
		List cc = []
		List bcc = []
		[RecipientType.TO, RecipientType.CC, RecipientType.BCC].each {type ->
			mimeMsg.getRecipients(type).each {
				def addr = it.address
				recipients.remove(addr)
				switch(type) {
					case RecipientType.TO: to << addr; break
					case RecipientType.CC: cc << addr; break
					case RecipientType.BCC: bcc << addr; break
				}
			}
		}
		recipients.each { bcc << it.toString() }
		
		def id
		sql.withTransaction {
			def qry = "INSERT INTO message (id, client, sent, sender, data) VALUES (default, $clnt, ${mimeMsg.sentDate ?: new Date()}, $from, $mimeBytes)"
			if(log.isTraceEnabled()) {
				def params = sql.getParameters(qry)
				def qryStr = sql.asSql(qry, params)
				params[-1] = '<binary data>'
				log.trace "$qryStr $params"
			}
			def key = sql.executeInsert qry 
			id = key[0][0]
			log.debug String.format('Attempting to create new message #%s:%n\t TO: %s%n\t CC: %s%n\tBCC: %s', id, to, cc, bcc)
			to.each {
				qry = "INSERT INTO recipients (id, type, email) VALUES ($id, 'to', $it)"
				if(log.isTraceEnabled()) {
					def params = sql.getParameters(qry)
					def qryStr = sql.asSql(qry, params)
					log.trace "$qryStr $params"
				}
				sql.execute qry
			}
			cc.each {
				qry = "INSERT INTO recipients (id, type, email) VALUES ($id, 'cc', $it)"
				if(log.isTraceEnabled()) {
					def params = sql.getParameters(qry)
					def qryStr = sql.asSql(qry, params)
					log.trace "$qryStr $params"
				}
				sql.execute qry
			}
			bcc.each {
				qry = "INSERT INTO recipients (id, type, email) VALUES ($id, 'bcc', $it)"
				if(log.isTraceEnabled()) {
					def params = sql.getParameters(qry)
					def qryStr = sql.asSql(qry, params)
					log.trace "$qryStr $params"
				}
				sql.execute qry
			}
			mimeMsg.getNonMatchingHeaders(['to', 'cc', 'bcc', 'from', 'date'] as String[]).each {
				qry = "INSERT INTO headers (id, name, value) VALUES ($id, $it.name, $it.value)"
				if(log.isTraceEnabled()) {
					def params = sql.getParameters(qry)
					def qryStr = sql.asSql(qry, params)
					log.trace "$qryStr $params"
				}
				sql.execute qry
			}
			
			def parsed = new MimeMessageParser(mimeMsg)
			parsed.parse()
			if(parsed.hasAttachments()) {
				parsed.attachmentList.each {
					qry = "INSERT INTO attachments (id, file_name, mime_type, size) VALUES ($id, $it.name, $it.contentType, 0)"
					if(log.isTraceEnabled()) {
						def params = sql.getParameters(qry)
						def qryStr = sql.asSql(qry, params)
						log.trace "$qryStr $params"
					}
					sql.execute qry
				}
			}
		}
		log.debug "New message #$id created successfully."
	}
	
	private void setDbVersion() {
		def qry = "INSERT INTO settings (name, value) VALUES ('dbVersion', '0')"
		if(log.isTraceEnabled())
			log.trace qry
		sql.execute qry
	}
	
	private void createTables() {
		sql.withTransaction {
			sql.execute '''
				CREATE TABLE message (
					id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1) PRIMARY KEY,
					client VARCHAR(45) NOT NULL,
					sent TIMESTAMP NOT NULL,
					sender VARCHAR(128) NOT NULL,
					data BLOB NOT NULL
				)
			'''

			sql.execute '''
				CREATE TABLE recipients (
					id BIGINT NOT NULL,
					type CHAR(3) NOT NULL,
					email VARCHAR(128) NOT NULL,
					CONSTRAINT RECPT_MSG_FK FOREIGN KEY (id) REFERENCES message(id) ON DELETE CASCADE
				)
			'''
			
			sql.execute '''
				CREATE TABLE attachments (
					id BIGINT NOT NULL,
					file_name VARCHAR(64) NOT NULL,
					mime_type VARCHAR(64) NOT NULL,
					size INTEGER NOT NULL,
					CONSTRAINT ATTACH_MSG_FK FOREIGN KEY (id) REFERENCES message(id) ON DELETE CASCADE
				)
			'''
			
			sql.execute '''
				CREATE TABLE headers (
					id BIGINT NOT NULL,
					name VARCHAR(64) NOT NULL,
					value VARCHAR(4096) NOT NULL,
					CONSTRAINT HDR_MSG_FK FOREIGN KEY (id) REFERENCES message(id) ON DELETE CASCADE
				)
			'''
			
			sql.execute '''
				CREATE TABLE settings (
					name VARCHAR(64) NOT NULL PRIMARY KEY,
					value VARCHAR(128)
				)
			'''
		}
	}
}
