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

import net.sf.pseudosmtp.j2ee.filters.RestRequestValidator

class SqlFilter {
	String column
	String operator
	def value
	def params
	private String lastError = null
		
	protected boolean isOperatorValid() {
		def rc = operator && (operator.toUpperCase() ==~ /IS (?:NOT )?NULL/ || ['=', '!=', '<', '>', '<=', '>='].contains(operator))
		if(!rc)
			lastError = "Invalid operator: $operator"
		return rc
	}
	
	protected boolean isColumnValid() {
		if(!column) return false
		// Could search the metadata of the DB, but to create a view requires explicit selection of
		// each column so might as well just list them here and make it faster
		def rc = ['name', 'value', 'sent', 'sender', 'data', 'type', 'email', 'file_name', 'mime_type', 'size'].contains(column.toLowerCase())
		if(!rc)
			lastError = "Invalid filter column: $column"
		return rc
	}
	
	@Override
	String toString() { 
		if(value instanceof List || value.getClass().isArray()) {
			def filters = []
			value.each {
				filters << new SqlFilter(column: column, operator: operator, value: it, params: params)
			}
			return new QueryBuilder(filters, params, ' OR ')
		}
		if(['to', 'bcc', 'cc'].contains(column.toLowerCase()))
			return new QueryBuilder([new SqlFilter(column: 'type', operator: '=', value: column, params: params), new SqlFilter(column: 'email', operator: '=', value: value, params: params)], params)
		if(column == 'has_attachment')
			return new QueryBuilder([new SqlFilter(column: 'file_name', operator: "IS ${Boolean.parseBoolean(value.toString()) ? 'NOT ' : ''}NULL", value: value, params: params)], params)
		if(column == '_for')
			return new QueryBuilder([new SqlFilter(column: 'to', operator: operator, value: value, params: params), new SqlFilter(column: 'cc', operator: operator, value: value, params: params), new SqlFilter(column: 'bcc', operator: operator, value: value, params: params)], params, ' OR ')
		if(!isOperatorValid())
			throw new IllegalArgumentException("Invalid operator: $operator")
		if(!isColumnValid()) // Assume user wants to filter on an email header
			return new QueryBuilder([new SqlFilter(column: 'name', operator: '=', value: column, params: params), new SqlFilter(column: 'value', operator: operator, value: value, params: params)], params)
		if(!operator.toUpperCase().startsWith('IS '))
			params << value
		return "$column $operator${operator.toUpperCase().startsWith('IS ') ? '' : ' ?'}"
	}
}
