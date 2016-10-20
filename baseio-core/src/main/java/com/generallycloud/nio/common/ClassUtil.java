package com.generallycloud.nio.common;

public class ClassUtil {

	public static Object newInstance(Class clazz) {

		if (clazz == null) {
			return null;
		}

		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Class forName(String className){
		
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
		
	}
}
