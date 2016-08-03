package com.gifisan.nio.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

public class PropertiesLoader {

	private static SharedBundle	bundle			= SharedBundle.instance();

	public static void load() throws IOException {
		
		bundle.loadLog4jProperties("conf/log4j.properties");
		
		storageProperties("server.properties");

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
		
		File _file = new File(bundle.getClassPath()+ "conf/" + file);

		if (_file.exists()) {
			return _file;
		}
		throw new FileNotFoundException("file not exist : " + _file.getAbsolutePath());
	}

}
