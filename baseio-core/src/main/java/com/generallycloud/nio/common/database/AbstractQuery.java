/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.common.database;

import java.util.Map;

public abstract class AbstractQuery implements DataBaseQuery {

	protected Map<String, String> sys_fields = null;
	
	protected AbstractQuery (){
		setSys_fields();
	}
	
	@Override
	public String getColumnName(String columnName) {
		String key = columnName.toUpperCase();
		if (sys_fields.containsKey(key)) {
			return sys_fields.get(key);
		} else {
			return columnName;
		}
	}
	
	abstract void setSys_fields();;
	
	
	
}
