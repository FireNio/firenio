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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.generallycloud.nio.PropertiesException;

public class SharedBundle {

	private static SharedBundle bundle = new SharedBundle();

	public static SharedBundle instance() {
		return bundle;
	}

	private String				classPath		= null;
	private Map<String,String>	properties	= new HashMap<>();
	private Map<String, String>	fileMapping	= new HashMap<String, String>();

	public boolean getBooleanProperty(String key) {
		return getBooleanProperty(key, false);
	}

	private SharedBundle() {
	}

	public boolean getBooleanProperty(String key, boolean defaultValue) {
		String temp = properties.get(key);
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
		String temp = properties.get(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Double.valueOf(temp);
	}

	public int getIntegerProperty(String key) {
		return getIntegerProperty(key, 0);
	}

	public int getIntegerProperty(String key, int defaultValue) {
		String temp = properties.get(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Integer.valueOf(temp);
	}

	public long getLongProperty(String key) {
		return getLongProperty(key, 0);
	}

	public long getLongProperty(String key, long defaultValue) {
		String temp = properties.get(key);
		if (StringUtil.isNullOrBlank(temp)) {
			return defaultValue;
		}
		return Long.valueOf(temp);
	}

	public String getProperty(String key) {
		return getProperty(key, null);
	}

	public String getProperty(String key, String defaultValue) {
		String value = properties.get(key);
		if (StringUtil.isNullOrBlank(value)) {
			return defaultValue;
		}
		return value;
	}

	public String getPropertyNoBlank(String key) throws PropertiesException {
		String value = properties.get(key);
		if (StringUtil.isNullOrBlank(value)) {
			throw new PropertiesException("property " + key + " is empty");
		}
		return value;
	}

	public synchronized void loadAllProperties(String file) throws IOException {

		if (StringUtil.isNullOrBlank(file)) {
			file = ".";
		}

		URL url = getClass().getClassLoader().getResource(file);

		if (url == null) {
			throw new IOException("file not found " + file);
		}

		loadAllProperties(url);
	}

	public synchronized void loadAllProperties() throws Exception {
		loadAllProperties(getURL());
	}

	private URL getURL() throws IOException {
		URL url = getClass().getClassLoader().getResource(".");
		if (url == null) {
			url = getClass().getProtectionDomain().getCodeSource().getLocation();
			if (url == null) {
				throw new IOException("no class path set");
			}
		}
		return url;
	}

	private void loadAllProperties(URL url) throws IOException {

		File root = new File(url.getFile());

		if (root.isFile()) {
			root = root.getParentFile();
		}

		String classPath = URLDecoder.decode(root.getCanonicalPath(), "UTF-8");

		if (classPath.endsWith(".jar") || classPath.endsWith(".jar/")) {
			root = root.getParentFile();
			classPath = URLDecoder.decode(root.getCanonicalPath(), "UTF-8");
		} else if (classPath.endsWith("test-classes") || classPath.endsWith("test-classes/")) {
			classPath += "/../classes";
		}

		setClassPath(new File(classPath).getCanonicalPath() + "/");

		root = new File(getClassPath());

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
				Properties temp = FileUtil.readProperties(file);
				if ("log4j.properties".equals(file.getName())) {
					PropertyConfigurator.configure(temp);
					LoggerFactory.enableSLF4JLogger(true);
				}
				putAll(properties, temp);
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
			putAll(properties, temp);
		} finally {
			CloseUtil.close(inputStream);
		}
	}
	
	private void putAll(Map<String,String> target,Properties source){
		for(Entry<Object, Object> e : source.entrySet()){
			target.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
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

	public void clearProperties() {
		properties.clear();
	}

}
