package com.gifisan.mtp.common;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SharedBundle {

	private static Properties properties = new Properties();
	
	static{
			String path = SharedBundle.class.getClassLoader().getResource(".").getPath();
			File root = new File(path);
			File []files = root.listFiles();
			for(File file:files){
				if (file.isFile() && file.getName().endsWith(".properties")) {
					try {
						Properties temp = FileUtil.readProperties(file);
						properties.putAll(temp);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	}
	
	public static String getProperty(String key){
		return properties.getProperty(key);
	}
	
	public static boolean getBooleanProperty(String key){
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return false;
		}
		return Boolean.valueOf(temp);
		
	}
	
	public static int getIntegerProperty(String key){
		
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return 0;
		}
		return Integer.valueOf(temp);
	}
	
	public static long getLongProperty(String key){
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return 0;
		}
		return Long.valueOf(temp);
	}
	
	
	public static double getDoubleProperty(String key){
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return 0;
		}
		return Double.valueOf(temp);
	}
	
	public static String getPropertyNoBlank(String key) throws Exception{
		String value = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(value)) {
			throw new Exception("property "+key+" is empty");
		}
		return value;
	}
}
