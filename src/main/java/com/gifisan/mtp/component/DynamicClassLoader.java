package com.gifisan.mtp.component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicClassLoader extends ClassLoader {

	private Map<String, Class<?>>	clazzes	= new HashMap<String, Class<?>>();
	private Logger				logger	= LoggerFactory.getLogger(DynamicClassLoader.class);

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class clazz = clazzes.get(name);
		if (clazz == null) {
			return this.getParent().loadClass(name);
		}
		return clazz;
	}

	public void scan(String file) throws IOException {
		this.scan(new File(file));
	}

	public void scan(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File _file : files) {
					scan(_file);
				}
			} else {
				String fileName = file.getName();
				if (fileName.endsWith(".class")) {
					logger.warn("{} 已经被忽略", fileName);
				} else if (fileName.endsWith(".jar")) {
					scanZip(new ZipFile(file));
				}
			}
		}else{
			logger.info("文件 [ {} ] 不存在",file.getAbsoluteFile());
		}
	}

	private void scanZip(ZipFile file) throws IOException {
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
		for (; entries.hasMoreElements();) {
			ZipEntry entry = entries.nextElement();

			if (!entry.isDirectory()) {
				String name = entry.getName();
				if (name.endsWith(".class")) {
					name = name.replace('/', '.').replace(".class", "");

					if (!name.startsWith("com.gifisan.mtp.servlet.impl")) {
						continue;
					}

					InputStream inputStream = file.getInputStream(entry);

					byte[] bytes = new byte[inputStream.available()];

					inputStream.read(bytes);

					defineClass(name, bytes);

				}
			}
		}
	}

	private void defineClass(String name, byte[] bytes) {
		try {
			Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
			clazzes.put(name, clazz);
			logger.info("load class [ {} ]", name);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public Class<?> forName(String name) throws ClassNotFoundException {
		return this.findClass(name);
	}
}
