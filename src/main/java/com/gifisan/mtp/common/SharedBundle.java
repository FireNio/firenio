package com.gifisan.mtp.common;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.Encoding;

public class SharedBundle {

	private Logger logger = LoggerFactory.getLogger(SharedBundle.class);
	
	private Properties properties = new Properties();
	
	private static SharedBundle bundle = new SharedBundle();
	
	public static SharedBundle instance(){
		return bundle;
	}
	
	private boolean initialized = false;
	
	private void initialize() throws UnsupportedEncodingException{
		String path = SharedBundle.class.getClassLoader().getResource(".").getPath();
		path = URLDecoder.decode(path, Encoding.UTF8.displayName());
		File root = new File(path);
		loadFile(root);
		System.out.println("工作目录[ "+path.substring(1)+" ]");
		logger.info("工作目录[ "+path.substring(1)+" ]");
//		File []files = root.listFiles();
//		for(File file:files){
//			if (file.isFile() && file.getName().endsWith(".properties")) {
//				
//			}
//		}
	}
	
	private void loadFile(File file){
		if (file.isFile()) {
			if (file.getName().endsWith(".properties")) {
				System.out.println("读取配置文件[ "+file.getAbsolutePath()+" ]");
				try {
					Properties temp = FileUtil.readProperties(file);
					if ("log4j.properties".equals(file.getName())) {
						PropertyConfigurator.configure(temp);
					}else{
						properties.putAll(temp);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			File []files = file.listFiles();
			for(File _file:files){
				loadFile(_file);
			}
		}
	}
	
	
	private SharedBundle(){
		if (!initialized) {
			synchronized (properties) {
				if (!initialized) {
					try {
						initialize();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	
	public String getProperty(String key){
		return properties.getProperty(key);
	}
	
	public boolean getBooleanProperty(String key){
		return getBooleanProperty(key, false);
		
	}
	
	public boolean getBooleanProperty(String key,boolean defaultValue){
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Boolean.valueOf(temp);
	}
	
	public int getIntegerProperty(String key){
		return getIntegerProperty(key, 0);
	}
	
	public int getIntegerProperty(String key,int defaultValue){
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Integer.valueOf(temp);
	}
	
	public long getLongProperty(String key){
		return getLongProperty(key, 0);
	}
	
	public long getLongProperty(String key,long defaultValue){
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Long.valueOf(temp);
	}
	
	
	public double getDoubleProperty(String key){
		return getDoubleProperty(key, 0);
	}
	
	public double getDoubleProperty(String key,double defaultValue){
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return 0;
		}
		return Double.valueOf(temp);
	}
	
	public String getPropertyNoBlank(String key) throws Exception{
		String value = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(value)) {
			throw new Exception("property "+key+" is empty");
		}
		return value;
	}
}
