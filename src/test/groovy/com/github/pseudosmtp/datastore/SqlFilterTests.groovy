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
package com.github.pseudosmtp.datastore

import com.github.pseudosmtp.datastore.SqlFilter;

import spock.lang.Specification
import spock.lang.Unroll

class SqlFilterTests extends Specification {
	def 'Ensure known and expected operators are accepted'() {
		when: 'a valid operator is specified'
			def filter = new SqlFilter(column: 'sender', operator: op, value: 'z', params: [])
		then: 'the filter produces the expected output'
			filter.toString() == "sender $op ?"
		where:
			op << ['=', '!=', '>', '<', '>=', '<=', '<=']
	}
	
	def 'Ensure null/not null operators are accepted'() {
		when: 'a valid operator is specified'
			def filter = new SqlFilter(column: 'sender', operator: op, value: 'z', params: [])
		then: 'the filter produces the expected output'
			filter.toString() == "sender $op"
		where:
			op << ['IS NOT NULL', 'IS NULL']
	}
	
	@Unroll('Ensure invalid operator \'#op\' is rejected')
	def 'Ensure invalid variations of operators are rejected'() {
		when: 'an invalid variation of a valid operator is specified'
			def filter = new SqlFilter(column: 'sender', operator: op, value: 'z', params: [])
			println filter.toString()
		then: 'the filter throws an exception'
			thrown(RuntimeException)
		where:
			op << ['==', '=!', '=>', '=<', '>>', '<>', '!', '<<<', 'IS NOT', 'IS', 'IS ', 'NOT NULL', 'NOT', ' NOT NULL ', ' IS NOT NULL ', ' IS NOT ']
	}
	
	@Unroll('Ensure invalid operator \'#op\' is rejected')
	def 'Ensure unsupported operators are rejected'() {
		when: 'an illegal operator is specified'
			def filter = new SqlFilter(column: 'sender', operator: op, value: 'z', params: [])
			filter.toString()
		then: 'the filter throws an exception'
			thrown(RuntimeException)
		where:
			op << ['foo', '=~', '^', '=1', '&', '=&', '%', '@#$$%']
	}
}
