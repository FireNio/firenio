package com.gifisan.database;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.likemessage.bean.T_USER;

public class FieldMapping {

	private Class				mappingClass	= null;

	private Map<String, Field>	fields		= new HashMap<String, Field>();

	protected FieldMapping(Class clazz) {
		this.mappingClass = clazz;
		analyse(clazz);
	}

	private void analyse(Class clazz) {

		Field[] fields = clazz.getDeclaredFields();

		for (Field f : fields) {
			this.fields.put(f.getName(), f);
		}

		Class c = clazz.getSuperclass();

		if (c != Object.class) {
			analyse(c);
		}
	}

	public Field getField(String fieldName) {

		return fields.get(fieldName);
	}

	public Class getMappingClass() {
		return mappingClass;
	}

	public static void main(String[] args) {

		FieldMapping mapping = new FieldMapping(T_USER.class);

		System.out.println(mapping.fields);
	}

}
