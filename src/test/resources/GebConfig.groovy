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
import org.openqa.selenium.htmlunit.HtmlUnitDriver

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider
import com.gargoylesoftware.htmlunit.WebClient
import com.github.pseudosmtp.test.PsmtpFvtSpec

driver = {
	new HtmlUnitDriver() {
		@Override
		protected WebClient modifyWebClient(WebClient client) {
			DefaultCredentialsProvider creds = new DefaultCredentialsProvider()
			creds.addCredentials('admin', 'admin')
			client.setCredentialsProvider(creds)
			client
		}
	}
}
baseUrl = "http://${PsmtpFvtSpec.EXT_HOST}:${PsmtpFvtSpec.WEB_PORT}/${PsmtpFvtSpec.WEB_CONTEXT ?: ''}"
