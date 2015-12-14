package com.gifisan.mtp.common;

import java.io.IOException;
import java.util.Properties;

public class SharedBundle {

	private static Properties properties = null;
	
	static{
		try {
			properties = FileUtil.readProperties("core.properties");
		} catch (IOException e) {
			throw new RuntimeException("exist file core.properties");
		}
	}
	
	public static String getProperty(String key){
		return properties.getProperty(key);
	}
	
	public static boolean getBooleanProperty(String key){
		String temp = properties.getProperty(key);
		if (StringUtil.isBlankOrNull(temp)) {
			return false;
		}
		return Boolean.valueOf(temp);
		
	}
	
	public static int getIntegerProperty(String key){
		
		String temp = properties.getProperty(key);
		if (StringUtil.isBlankOrNull(temp)) {
			return 0;
		}
		return Integer.valueOf(temp);
	}
	
	public static long getLongProperty(String key){
		String temp = properties.getProperty(key);
		if (StringUtil.isBlankOrNull(temp)) {
			return 0;
		}
		return Long.valueOf(temp);
	}
	
	
	public static double getDoubleProperty(String key){
		String temp = properties.getProperty(key);
		if (StringUtil.isBlankOrNull(temp)) {
			return 0;
		}
		return Double.valueOf(temp);
	}
	
	public static String getPropertyNoBlank(String key) throws Exception{
		String value = properties.getProperty(key);
		if (StringUtil.isBlankOrNull(value)) {
			throw new Exception("property "+key+" is empty");
		}
		return value;
	}
}
