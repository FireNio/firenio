package com.yoocent.mtp.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class FileUtil extends FileUtils{

	public static String readContentByCls(String file,String encoding) throws IOException{
		File file2 = readFileByCls(file);
		if (file2.exists()) {
			return readFileToString(file2, encoding);
		}
		return null;
	}
	
	public static byte[] readBytesByCls(String file) throws IOException{
		File file2 = readFileByCls(file);
		if (file2.exists()) {
			return readFileToByteArray(file2);
		}
		return null;
	}
	
	public static File readFileByCls(String file) throws UnsupportedEncodingException{
		ClassLoader classLoader = FileUtil.class.getClassLoader(); 
		String path = classLoader.getResource(".").getFile();
		return new File(URLDecoder.decode(path+ file,"UTF-8"));
	}
	
	public static void writeContentByCls(String file,String content,String encoding,boolean append) throws IOException{
		File realFile = readFileByCls(file);
		writeStringToFile(realFile, content, encoding, append);
	}
	
	public static void writeBytesByCls(String file,byte [] bytes,boolean append) throws IOException{
		File realFile = readFileByCls(file);
		writeByteArrayToFile(realFile, bytes, append);
	}
	
//	public static void writeBytesByCls(String file,byte [] bytes,boolean append) throws IOException{
//		File realFile = readFileByCls(file);
//		writeByteArrayToFile(realFile, bytes, append);
//		
//	}
	
	public static void deleteDirectoryOrFileByCls(String file) throws IOException{
		File realFile = readFileByCls(file);
		deleteDirectoryOrFile(realFile);
	}
	
	public static void deleteDirectoryOrFile(File file) throws IOException{
		if (file.isFile()) {
			file.delete();
		}else{
			deleteDirectory(file);
		}
	}
	
	public static void writeProperties(Properties properties,String path) throws IOException{
		File file = readFileByCls(path);
		FileOutputStream fos = new FileOutputStream(file);
		properties.store(fos, "# this is admin's properties");
	}
	
	public static Properties readProperties(String path) throws IOException{
		Properties properties = new Properties();
		InputStream inputStream = FileUtil.class.getClassLoader().getResourceAsStream(path);
		try {
			if (inputStream == null) {
				throw new IOException(path+" not found!");
			}
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			properties.load(inputStreamReader);
			return properties;
		}finally{
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}
	
}
