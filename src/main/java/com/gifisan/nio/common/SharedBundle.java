package com.gifisan.nio.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.PropertyConfigurator;

import com.gifisan.nio.PropertiesException;

public class SharedBundle {

	private static SharedBundle	bundle	= new SharedBundle();
	
	private  String classPath;
	
	public static SharedBundle instance() {
		return bundle;
	}

	private AtomicBoolean	initialized	= new AtomicBoolean(false);
	private Properties		properties	= new Properties();

	private SharedBundle() {
		if (initialized.compareAndSet(false, true)) {
			try {
				initialize();
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

	public String getClassPath() {
		return classPath;
	}

	public boolean getBooleanProperty(String key) {
		return getBooleanProperty(key, false);

	}

	public boolean getBooleanProperty(String key, boolean defaultValue) {
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Boolean.valueOf(temp);
	}

	public double getDoubleProperty(String key) {
		return getDoubleProperty(key, 0);
	}

	public double getDoubleProperty(String key, double defaultValue) {
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Double.valueOf(temp);
	}

	public int getIntegerProperty(String key) {
		return getIntegerProperty(key, 0);
	}

	public int getIntegerProperty(String key, int defaultValue) {
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Integer.valueOf(temp);
	}

	public long getLongProperty(String key) {
		return getLongProperty(key, 0);
	}

	public long getLongProperty(String key, long defaultValue) {
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Long.valueOf(temp);
	}

	public String getProperty(String key) {
		return getProperty(key, null);
	}

	public String getProperty(String key, String defaultValue) {
		String value = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(value)) {
			return defaultValue;
		}
		return value;
	}

	public String getPropertyNoBlank(String key) throws PropertiesException {
		String value = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(value)) {
			throw new PropertiesException("property " + key + " is empty");
		}
		return value;
	}

	private void initialize() throws Exception {
		URL url = this.getClass().getClassLoader().getResource(".");
		if (url == null) {
			return;
		}
		File root = new File(url.getFile());
		String path = root.getCanonicalPath();
		path = URLDecoder.decode(path, "UTF-8");
		
		if (path.endsWith("test-classes") || path.endsWith("test-classes/")) {
			path += "/../classes";
		}
		
		setClassPath(new File(path).getCanonicalPath() + "/");
		
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().endsWith(".properties")) {
				try {
					Properties temp = FileUtil.readProperties(file);
					if ("log4j.properties".equals(file.getName())) {
						PropertyConfigurator.configure(temp);
						continue;
					}
					properties.putAll(temp);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean loadLog4jProperties(String file) throws IOException {
		File _file = loadFile(file);

		if (_file.exists()) {
			Properties log4j = loadProperties(FileUtil.openInputStream(_file));
			PropertyConfigurator.configure(log4j);
			LoggerFactory.enableSLF4JLogger(true);
			return true;
		}
		return false;
	}

	public Properties loadProperties(String file) throws IOException {
		File _file = loadFile(file);
		if (_file.exists()) {
			return loadProperties(FileUtil.openInputStream(_file));
		}
		return null;
	}

	public Properties loadProperties(InputStream inputStream) throws IOException {
		Properties temp = new Properties();
		try {
			temp.load(inputStream);
			return temp;
		} finally {
			CloseUtil.close(inputStream);
		}
	}

	public File loadFile(String file) {
		return new File(classPath + file);
	}

	public boolean storageProperties(String file) throws IOException {
		File _file = loadFile(file);
		if (_file.exists()) {
			storageProperties(FileUtil.openInputStream(_file));
			return true;
		}
		return false;
	}

	public void storageProperties(InputStream inputStream) throws IOException {
		Properties temp = new Properties();
		try {
			temp.load(inputStream);
			properties.putAll(temp);
		} finally {
			CloseUtil.close(inputStream);
		}
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath.replace("\\", "/");
	}

}
