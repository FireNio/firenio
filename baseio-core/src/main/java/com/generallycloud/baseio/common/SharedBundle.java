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
package com.generallycloud.baseio.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class SharedBundle {

	private static SharedBundle bundle = new SharedBundle();

	public static SharedBundle instance() {
		return bundle;
	}

	private String				classPath		= null;
	private Map<String, String>	properties	= new HashMap<>();

	public boolean getBooleanProperty(String key) {
		return getBooleanProperty(key, false);
	}

	private SharedBundle() {
		initClassPath();
	}

	private void initClassPath() {
		ClassLoader classLoader = getClass().getClassLoader();
		URL url = classLoader.getResource(".");
		if (url == null) {
			url = classLoader.getResource("log4j.properties");
			if (url == null) {
				url = classLoader.getResource("META-INF");
			}
			return;
		}
		File file = new File(url.getFile());
		try {
			
			File directory = FileUtil.getParentDirectory(file);
			if (directory.getName().equals("META_INF")) {
				directory = directory.getParentFile();
			}
			setClassPath(decodeURL(directory.getAbsolutePath(), Encoding.UTF8));
		} catch (IOException e) {
		}
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

	public String loadContent(String file, Charset charset) throws IOException {
		return FileUtil.input2String(loadInputStream(file), charset);
	}

	public InputStream loadInputStream(String file) throws IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);
		if (inputStream == null) {
			throw new IOException("file not found: " + file);
		}
		return inputStream;
	}
	
	public File loadFile(String file) throws IOException {
		URL url = getClass().getClassLoader().getResource(file);
		if (url == null) {
			throw new IOException("file not found: " + file);
		}
		return new File(decodeURL(url.getFile(), Encoding.UTF8));
	}
	
	public String decodeURL(String url,Charset charset){
		try {
			return URLDecoder.decode(url, charset.name());
		} catch (UnsupportedEncodingException e) {
			return url;
		}
	}

	public Properties loadProperties(InputStream inputStream, Charset charset) throws IOException {
		return FileUtil.readProperties(inputStream, charset);
	}

	public Properties loadProperties(String file, Charset charset) throws IOException {
		return loadProperties(loadInputStream(file), charset);
	}

	private void setClassPath(String classPath) {
		this.classPath = classPath.replace("\\", "/");
	}

	public void storageProperties(InputStream inputStream, Charset charset) throws IOException {
		Properties temp = loadProperties(inputStream, charset);
		putAll(properties, temp);
	}

	private synchronized void putAll(Map<String, String> target, Properties source) {
		for (Entry<Object, Object> e : source.entrySet()) {
			target.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
		}
	}

	public void storageProperties(String file, Charset charset) throws IOException {
		Properties temp = loadProperties(file, charset);
		putAll(properties, temp);
	}

	public void clearProperties() {
		properties.clear();
	}

	class PropertiesException extends Exception {

		private static final long serialVersionUID = 1L;

		public PropertiesException() {
		}

		public PropertiesException(String message) {
			super(message);
		}

		public PropertiesException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
