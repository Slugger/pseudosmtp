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
package net.sf.pseudosmtp.smtp

import net.sf.pseudosmtp.smtp.handlers.DumpToDatabaseHandler

import org.subethamail.smtp.MessageContext
import org.subethamail.smtp.MessageHandler
import org.subethamail.smtp.MessageHandlerFactory

class PsmtpMessageHandlerFactory implements MessageHandlerFactory {

	@Override
	public MessageHandler create(MessageContext ctx) {
		def addr = ctx.remoteAddress.address.toString()
		return new DumpToDatabaseHandler(addr.substring(addr.indexOf('/') + 1))
	}
}
