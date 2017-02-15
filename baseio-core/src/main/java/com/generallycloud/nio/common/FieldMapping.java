/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.nio.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldMapping{

	private Class<?>				mappingClass	;

	private Map<String, Field>	fieldMapping	= new HashMap<String, Field>();

	private List<Field>			fieldList		= new ArrayList<Field>();

	public FieldMapping(Class<?> clazz) {
		this.mappingClass = clazz;
		analyse(clazz);
	}

	private void analyse(Class<?> clazz) {

		Field[] fields = clazz.getDeclaredFields();

		for (Field f : fields) {
			this.fieldMapping.put(f.getName(), f);
			this.fieldList.add(f);
		}

		Class<?> c = clazz.getSuperclass();

		if (c != Object.class) {
			analyse(c);
		}
	}
	
	public List<Field> getFieldList(){
		return fieldList;
	}

	public Field getField(String fieldName) {

		return fieldMapping.get(fieldName);
	}

	public Class<?> getMappingClass() {
		return mappingClass;
	}
}
