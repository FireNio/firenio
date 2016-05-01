package com.gifisan.nio.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

public class PropertiesLoader {

	private static SharedBundle bundle = SharedBundle.instance();
	
	private static boolean releaseModel = false;
	
	static {
		
		try {
			
			releaseModel = bundle.loadLog4jProperties("conf/log4j.properties") ;
			
			if (!releaseModel && !bundle.loadLog4jProperties("log4j.properties") 
					&& !bundle.loadLog4jProperties("../classes/log4j.properties")) {
				
				throw new Error("config error");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void load() throws IOException{
		load("server.properties");
	}
	
	public static void load(String file) throws IOException{
		
		bundle.storageProperties(FileUtil.openInputStream(loadFile(file)));
	}
	
	public static String loadContent(String file,Charset charset) throws IOException{
		
		return FileUtil.readFileToString(loadFile(file), charset);
	}
	
	public static File loadFile(String file) throws FileNotFoundException{
		SharedBundle bundle = SharedBundle.instance();
		
		if (releaseModel) {
			return bundle.loadFile("conf/"+file);
		}else{
			
			File _file = bundle.loadFile(file);
			
			if (_file.exists()) {
				return _file;
			}
			
			_file = bundle.loadFile("../classes/" + file);
			
			if (_file.exists()) {
				return _file;
			}
			
			throw new FileNotFoundException("file not exist : "+file);
		}
	}
	
}
