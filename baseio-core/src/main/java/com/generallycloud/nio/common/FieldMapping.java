package com.generallycloud.nio.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldMapping{

	private Class				mappingClass	;

	private Map<String, Field>	fieldMapping	= new HashMap<String, Field>();

	private List<Field>			fieldList		= new ArrayList<Field>();

	public FieldMapping(Class clazz) {
		this.mappingClass = clazz;
		analyse(clazz);
	}

	private void analyse(Class clazz) {

		Field[] fields = clazz.getDeclaredFields();

		for (Field f : fields) {
			this.fieldMapping.put(f.getName(), f);
			this.fieldList.add(f);
		}

		Class c = clazz.getSuperclass();

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

	public Class getMappingClass() {
		return mappingClass;
	}
}
