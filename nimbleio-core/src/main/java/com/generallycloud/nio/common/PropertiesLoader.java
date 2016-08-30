package com.generallycloud.nio.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

public class PropertiesLoader {

	private static SharedBundle	bundle				= SharedBundle.instance();

	private static String		conf_path				= "conf/";

	private static String		app_path				= "app/";

	private static String		server_properties_name	= "server.properties";

	public static void load() throws IOException {

		bundle.loadLog4jProperties(conf_path + "log4j.properties");

		storageProperties(server_properties_name);

		DebugUtil.setEnableDebug(bundle.getBooleanProperty("SERVER.DEBUG"));
	}

	public static void storageProperties(String file) throws IOException {

		bundle.storageProperties(FileUtil.openInputStream(loadFile(file)));
	}

	public static Properties loadProperties(String file) throws IOException {

		return bundle.loadProperties(FileUtil.openInputStream(loadFile(file)));
	}

	public static String loadContent(String file, Charset charset) throws IOException {

		return FileUtil.readFileToString(loadFile(file), charset);
	}

	public static File loadFile(String file) throws FileNotFoundException {

		SharedBundle bundle = SharedBundle.instance();

		File _file = new File(bundle.getClassPath() + conf_path + file);

		if (_file.exists()) {
			return _file;
		}
		throw new FileNotFoundException("file not exist : " + _file.getAbsolutePath());
	}

	public static void setBasepath(String path) {

		String classPath = SharedBundle.instance().getClassPath();

		SharedBundle.instance().setClassPath(classPath + getPath(path));
	}

	private static String getPath(String path) {
		if (StringUtil.isNullOrBlank(path)) {
			throw new IllegalArgumentException("empty path");
		}

		if (!path.endsWith("/")) {
			path += "/";
		}

		return path;
	}

	public static void setAppPath(String path) {

		app_path = getPath(path);
	}

	public static void setConfPath(String path) {
		conf_path = getPath(path);
	}

	public static void setServerProperties(String properties) {

		if (StringUtil.isNullOrBlank(properties)) {
			throw new IllegalArgumentException("empty properties:");
		}

		server_properties_name = properties;
	}

	public static String getAppPath() {
		return app_path;
	}

}
