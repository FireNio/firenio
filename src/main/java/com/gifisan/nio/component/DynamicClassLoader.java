package com.gifisan.nio.component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicClassLoader extends ClassLoader {

	private Logger					logger		= LoggerFactory.getLogger(DynamicClassLoader.class);
	private Map<String, ClassEntry>	clazzEntries	= new HashMap<String, ClassEntry>();
	private ClassLoader				parent		= null;

	public void setParent(ClassLoader parent) {
		this.parent = parent;
	}

	public DynamicClassLoader() {
		ClassLoader parent = getParent();
		if (parent == null) {
			parent = getSystemClassLoader();
		}
		this.setParent(parent);

	}

	private Class<?> findLoadedClass0(String name) throws ClassNotFoundException {

		ClassEntry entry = clazzEntries.get(name);

		if (entry == null) {
			return null;
		}
		
		if (entry.loadedClass == null) {
			return defineClass(entry);
		}

		return entry.loadedClass;
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {

		Class<?> clazz = findLoadedClass0(name);

		if (clazz == null) {

			clazz = defineClass(name);

			if (clazz == null) {
				return this.parent.loadClass(name);
			}
		}

		return clazz;
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {

		Class<?> clazz = findLoadedClass0(name);

		if (clazz == null) {
			return this.parent.loadClass(name);
		}

		return clazz;

	}

	public void scan(String file) throws IOException {
		this.scan(new File(file));
	}
	
	public void scan(File file) throws IOException {
		this.scan0(file);
		logger.info("预加载Class字节码到缓存[ {} ]个 " , clazzEntries.size());
	}

	private void scan0(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File _file : files) {
					scan0(_file);
				}
			} else {
				String fileName = file.getName();
				if (fileName.endsWith(".class")) {
					logger.warn("{} 已经被忽略", fileName);
				} else if (fileName.endsWith(".jar")) {
					scanZip(new JarFile(file));
				}
			}
		} else {
			logger.info("文件 [ {} ] 不存在", file.getAbsoluteFile());
		}
		
		
	}

	private void scanZip(JarFile file) throws IOException {

		logger.info("加载文件 [ {} ]", file.getName());

		Enumeration<JarEntry> entries = (Enumeration<JarEntry>) file.entries();
		for (; entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();
			if (!entry.isDirectory()) {
				String name = entry.getName();
				if (name.endsWith(".class") && !matchSystem(name)) {

					storeClass(file, name, entry);

				}
			}
		}
		
	}

	public boolean matchSystem(String name) {

		return name.startsWith("java") 
				|| name.startsWith("sun") 
				|| name.startsWith("com/sun")
				|| matchExtend(name);

	}
	
	public boolean matchExtend(String name) {

		return name.startsWith("com/gifisan");

	}

	private void storeClass(JarFile file, String name, JarEntry entry) throws IOException {
		name = name.replace('/', '.').replace(".class", "");

		InputStream inputStream = file.getInputStream(entry);

		byte[] binaryContent = new byte[(int) entry.getSize()];

		int pos = 0;
		try {
			while (true) {
				int n = inputStream.read(binaryContent, pos, binaryContent.length - pos);
				if (n <= 0)
					break;
				pos += n;
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		ClassEntry classEntry = new ClassEntry();

		classEntry.binaryContent = binaryContent;

		classEntry.lastModified = entry.getTime();
		
		classEntry.className = name;

		clazzEntries.put(name, classEntry);

	}

	private Class<?> defineClass(String name) throws ClassNotFoundException {
		ClassEntry entry = clazzEntries.get(name);

		if (entry == null) {
			return null;
		}

		return defineClass(entry);
	}
	
	private Class<?> defineClass(ClassEntry entry) throws ClassNotFoundException {

		String name = entry.className;
		
		try {
			Class<?> clazz = defineClass(name, entry.binaryContent, 0, entry.binaryContent.length);

			entry.loadedClass = clazz;

			logger.debug("define class [ {} ]", name);

			return clazz;
		} catch (Throwable e) {
			throw new ClassNotFoundException(e.getMessage(), e);
		}
	}

	public Class<?> forName(String name) throws ClassNotFoundException {
		return this.findClass(name);
	}

	class ClassEntry {
		
		private String		className		= null;

		private long		lastModified	= -1;

		private byte[]	binaryContent	= null;

		private Class<?>	loadedClass	= null;

	}
}
