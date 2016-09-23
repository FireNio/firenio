package com.generallycloud.nio.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.generallycloud.nio.PropertiesException;

public class SharedBundle {

	private static SharedBundle	bundle	= new SharedBundle();

	public static SharedBundle instance() {
		return bundle;
	}

	private String				classPath		= null;
	private Properties			properties	= new Properties();
	private Map<String, String>	fileMapping	= new HashMap<String, String>();

	public boolean getBooleanProperty(String key) {
		return getBooleanProperty(key, false);
	}

	private SharedBundle() {
		try {
			this.loadAllProperties0("");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean getBooleanProperty(String key, boolean defaultValue) {
		String temp = properties.getProperty(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Boolean.valueOf(temp);
	}

	public String getClassPath() {
		return classPath;
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

	public synchronized void loadAllProperties() throws Exception {
		loadAllProperties("");
	}

	public synchronized void loadAllProperties(String path) throws IOException {
		if (!StringUtil.isNullOrBlank(path)) {
			this.loadAllProperties0(path);
		}
	}

	private synchronized void loadAllProperties0(String path) throws IOException {
		URL url = this.getClass().getClassLoader().getResource(".");
		if (url == null) {
			return;
		}
		File root = new File(url.getFile());
		String classPath = root.getCanonicalPath();
		classPath = URLDecoder.decode(classPath, "UTF-8");

		if (classPath.endsWith("test-classes") || classPath.endsWith("test-classes/")) {
			classPath += "/../classes";
		}

		setClassPath(new File(classPath).getCanonicalPath() + "/");

		root = new File(getClassPath() + path);

		properties.clear();

		fileMapping.clear();

		loopLoadFile(root);
	}

	private void loopLoadFile(File file) throws IOException {
		if (file.isDirectory()) {

			File[] files = file.listFiles();

			if (files == null) {
				throw new IOException("empty folder:" + file.getCanonicalPath());
			}

			for (File f : files) {

				loopLoadFile(f);
			}
		} else {

			fileMapping.put(file.getName(), file.getCanonicalPath());

			if (file.getName().endsWith(".properties")) {
				try {
					Properties temp = FileUtil.readProperties(file);
					if ("log4j.properties".equals(file.getName())) {
						PropertyConfigurator.configure(temp);
						LoggerFactory.enableSLF4JLogger(true);
					}
					properties.putAll(temp);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String loadContent(String file, Charset charset) throws IOException {
		return FileUtil.readFileToString(loadFile(file), charset);
	}

	public File loadFile(String file) {
		return new File(classPath + file);
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

	public Properties loadProperties(InputStream inputStream) throws IOException {

		if (inputStream == null) {
			throw new IOException("null inputstream");
		}

		Properties temp = new Properties();
		try {
			temp.load(inputStream);
			return temp;
		} finally {
			CloseUtil.close(inputStream);
		}
	}

	public Properties loadProperties(String file) throws IOException {
		File _file = loadFile(file);
		if (_file.exists()) {
			return loadProperties(FileUtil.openInputStream(_file));
		}

		String filePath = fileMapping.get(file);

		if (filePath != null) {
			_file = new File(filePath);

			if (_file.exists()) {
				return loadProperties(FileUtil.openInputStream(_file));
			}
		}

		return loadProperties(this.getClass().getClassLoader().getResourceAsStream(file));
	}

	private void setClassPath(String classPath) {
		this.classPath = classPath.replace("\\", "/");
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

	public boolean storageProperties(String file) throws IOException {
		File _file = loadFile(file);
		if (_file.exists()) {
			storageProperties(FileUtil.openInputStream(_file));
			return true;
		}
		return false;
	}

}
