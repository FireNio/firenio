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
package com.generallycloud.baseio.container;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.LoggerUtil;

public class URLDynamicClassLoader extends URLClassLoader implements DynamicClassLoader{

	private Logger					logger		= LoggerFactory.getLogger(getClass());
	private Map<String, ClassEntry>	clazzEntries	= new HashMap<>();
	private ClassLoader				parentClassLoader;
	private ClassLoader				systemClassLoader;
	private ClassLoader				appClassLoader;

	public URLDynamicClassLoader() {
		
		super(new URL[]{});
		
		this.appClassLoader = getClass().getClassLoader();

		this.parentClassLoader = getParent();

		this.systemClassLoader = getSystemClassLoader();
		
		if (parentClassLoader == null) {
			parentClassLoader = systemClassLoader;
		}

		if (appClassLoader == null) {
			appClassLoader = parentClassLoader;
		}
	}

	private Class<?> findLoadedClass0(String name) throws ClassNotFoundException {

		ClassEntry entry = clazzEntries.get(name);

		if (entry == null) {
			return null;
		}

		return entry.loadedClass;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {

		Class<?> clazz = findLoadedClass0(name);

		if (clazz == null) {
			return loadClass(name);
		}

		return clazz;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {

		Class<?> clazz = defineClass(name);

		if (clazz != null) {
			return clazz;
		}

		clazz = entrustLoadClass(appClassLoader, name);

		if (clazz != null) {
			return clazz;
		}

		clazz = entrustLoadClass(parentClassLoader, name);

		if (clazz != null) {
			return clazz;
		}

		clazz = entrustLoadClass(systemClassLoader, name);

		if (clazz != null) {
			return clazz;
		}

		throw new ClassNotFoundException(name);
	}

	private Class<?> entrustLoadClass(ClassLoader classLoader, String name) {
		try {
			return classLoader.loadClass(name);
		} catch (Throwable e) {
			return null;
		}
	}

	@Override
	public void scan(String file) throws IOException {
		this.scan(new File(file));
	}

	@Override
	public void scan(File file) throws IOException {
		this.scan0(file,"");
		LoggerUtil.prettyNIOServerLog(logger, "cache bianry class size [ {} ] ", clazzEntries.size());
	}

	private void scan0(File file,String path) throws IOException {

		if (!file.exists()) {
			LoggerUtil.prettyNIOServerLog(logger, "file or directory [ {} ] not found", file.getAbsoluteFile());
			return;
		}

		if (file.isDirectory()) {

			File[] files = file.listFiles();

			for (File _file : files) {
				scan0(_file,path+"/"+_file.getName());
			}

			return;
		}

		String fileName = file.getName();

		if (fileName.endsWith(".jar")) {
			scanZip(new JarFile(file));
		}
		
		addURL(file.toURI().toURL());
		
	}
	

	private void scanZip(JarFile file) throws IOException {

		try {

			LoggerUtil.prettyNIOServerLog(logger, "load file [ {} ]", file.getName());

			Enumeration<JarEntry> entries = file.entries();

			for (; entries.hasMoreElements();) {

				JarEntry entry = entries.nextElement();

				if (entry.isDirectory()) {
					continue;
				}

				String filePathName = entry.getName();

				if (filePathName.endsWith(".class") && !matchSystem(filePathName)) {
					storeClass(file, entry);
				}
				
			}
		} finally {

			CloseUtil.close(file);
		}
	}

	public boolean matchSystem(String name) {

		return name.startsWith("java") || name.startsWith("sun") || name.startsWith("com/sun") || matchExtend(name);
	}

	public boolean matchExtend(String name) {
		return false;
	}

	private void storeClass(JarFile file, JarEntry entry) throws IOException {

		String filePathName = entry.getName();
		
		String className = filePathName.replace('/', '.').replace(".class", "");

		if (clazzEntries.containsKey(className)) {
			throw new DuplicateClassException(className);
		}

		try {

			parentClassLoader.loadClass(className);
			
			throw new DuplicateClassException(className);
		} catch (ClassNotFoundException e) {
		}

		InputStream inputStream = file.getInputStream(entry);

		byte[] binaryContent = FileUtil.toByteArray(inputStream, entry.getSize());

		ClassEntry classEntry = new ClassEntry();

		classEntry.classBinary = binaryContent;

		classEntry.className = className;

		clazzEntries.put(className, classEntry);
	}

	private Class<?> defineClass(String name) throws ClassNotFoundException {

		ClassEntry entry = clazzEntries.get(name);

		if (entry == null) {
			return null;
		}

		return defineClass(entry);
	}

	private Class<?> defineClass(ClassEntry entry) {

		if (entry.loadedClass != null) {
			return entry.loadedClass;
		}

		SecurityManager sm = System.getSecurityManager();

		if (sm != null) {

			String name = entry.className;

			int i = name.lastIndexOf('.');
			
			if (i != -1) {
				sm.checkPackageAccess(name.substring(0, i));
			}
		}

		String name = entry.className;

		byte[] cb = entry.classBinary;

		Class<?> clazz = defineClass(name, cb, 0, cb.length);

		entry.loadedClass = clazz;

		LoggerUtil.prettyNIOServerLog(logger, "define class [ {} ]", name);

		return clazz;
	}

	@Override
	public Class<?> forName(String name) throws ClassNotFoundException {
		return this.findClass(name);
	}

	class ClassEntry {

		private String		className;

		private byte[]		classBinary;

		private Class<?>	loadedClass;

	}

	@Override
	public void unloadClassLoader() {

		Collection<ClassEntry> es = clazzEntries.values();

		for (ClassEntry e : es) {
			unloadClass(e.loadedClass);
		}
	}

	private void unloadClass(Class<?> clazz) {
		
		CloseUtil.close(this);

		if (clazz == null) {
			return;
		}

		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {

			if (!Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			try {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				field.set(null, null);
			} catch (Throwable e) {
				logger.debug(e);
			}
		}
	}

}
