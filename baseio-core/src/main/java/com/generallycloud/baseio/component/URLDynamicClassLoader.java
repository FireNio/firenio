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
package com.generallycloud.baseio.component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.log.Logger;

public class URLDynamicClassLoader extends URLClassLoader implements DynamicClassLoader {

    private static final ClassLoader PARENT_CLASSLOADER = URLDynamicClassLoader.class.getClassLoader();
    
    private Map<String, ClassEntry> clazzEntries   = new HashMap<>();
    private Logger                  logger;
    private Map<String, URL>        resourceMap    = new HashMap<>();
    private Map<String, List<URL>>  resourcesMap   = new HashMap<>();
    private Set<String>             excludePaths   = new HashSet<>();
    private Set<String>             matchExtend    = new HashSet<>();
    private List<String>            matchStartWith = new ArrayList<>();
    private Set<String>             systemMatch    = new HashSet<>();
    private boolean                entrustFirst   = true;

    // entrustFirst 是否优先委托父类加载class
    // 最外层classloader（PCL）一般设置优先委托自己加载class，因为到后面对象需要用PCL去加载resources
    // 如果PCL委托父类加载，则后面的class拿到的CL为AppCL，因为"-cp"参数指定了jar目录，
    // 热加载部分的classloader（HCL）设置优先委托父类加载class，因为一般情况下父类不会去加载HCL
    // 加载的class，HCL依然能够每次加载最新的class，而已加载的则使用PCL加载的class，因为原则上
    // HCL不允许覆盖PCL定义的class
    public URLDynamicClassLoader(ClassLoader parent) {
        this(parent, true);
    }

    public URLDynamicClassLoader(ClassLoader parent, boolean entrustFirst) {
        super(new URL[] {}, parent == null ? PARENT_CLASSLOADER : parent);
        this.entrustFirst = entrustFirst;
        this.initialize();
    }

    private void initialize() {
        systemMatch.add("java.");
        systemMatch.add("javax.");
        systemMatch.add("sun.");
        systemMatch.add("com.sun.");

        matchStartWith.addAll(systemMatch);
    }

    private void addResource(URL url, String pathName, String fileName)
            throws DuplicateClassException {

        //		if (resourceMap.containsKey(pathName) && !pathName.equals(".")) {
        //			throw new DuplicateClassException(pathName);
        //		}

        resourceMap.put(pathName, url);

        List<URL> urls = resourcesMap.get(fileName);

        if (urls == null) {
            urls = new ArrayList<>();
            resourcesMap.put(fileName, urls);
        }

        urls.add(url);
    }

    private synchronized Class<?> defineClass(ClassEntry entry) {

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

        Class<?> clazz = defineClass(name, cb, 0, cb.length,
                new CodeSource(entry.codeBase, entry.certificates));

        entry.loadedClass = clazz;

        entry.classBinary = null;

        LoggerUtil.prettyLog(logger, "define class [ {} ]", name);

        return clazz;
    }

    private Class<?> entrustLoadClass(ClassLoader classLoader, String name) {
        try {
            return classLoader.loadClass(name);
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        ClassEntry entry = clazzEntries.get(name);

        if (entry == null) {
            return null;
        }

        if (entry.loadedClass == null) {
            return defineClass(entry);
        }

        return entry.loadedClass;
    }

    @Override
    public URL findResource(String name) {

        URL url = resourceMap.get(name);

        if (url == null) {
            return super.findResource(name);
        }

        return url;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        @SuppressWarnings("unchecked")
        Enumeration<URL>[] temp = new Enumeration[2];
        temp[0] = findResources0(name);
        temp[1] = super.findResources(name);
        return new CompoundEnumeration<>(temp);
    }

    private Enumeration<URL> findResources0(String name) throws IOException {

        final List<URL> urls = resourcesMap.get(name);

        if (urls == null) {
            return java.util.Collections.emptyEnumeration();
        }

        return new Enumeration<URL>() {

            private int index = 0;

            @Override
            public boolean hasMoreElements() {
                return index < urls.size();
            }

            @Override
            public URL nextElement() {

                if (!hasMoreElements()) {
                    throw new NoSuchElementException();
                }

                return urls.get(index++);
            }
        };
    }

    private boolean matchExtend(String name) {
        return matchExtend.contains(name);
    }

    private boolean matchStartWith(String name) {
        List<String> temp = matchStartWith;
        for (String s : temp) {
            if (name.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchSystem(String name) {
        return matchExtend(name) || matchStartWith(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        if (entrustFirst) {

            Class<?> clazz = entrustLoadClass(getParent(), name);

            if (clazz != null) {
                return clazz;
            }

            clazz = super.loadClass(name, resolve);

            if (clazz != null) {
                return clazz;
            }

            clazz = findClass(name);

            if (clazz != null) {
                return clazz;
            }

            throw new ClassNotFoundException(name);
        }

        Class<?> clazz = findClass(name);

        if (clazz != null) {
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }

        clazz = entrustLoadClass(getParent(), name);

        if (clazz != null) {
            return clazz;
        }

        clazz = super.loadClass(name, resolve);

        if (clazz != null) {
            return clazz;
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    public synchronized void scan(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("null file");
        }
        scan0(file);
    }

    public void scan0(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        this.scanFile(file, "");
        this.addResource(file, "/.", ".");
        LoggerUtil.prettyLog(logger, "load class count [ {} ] from [ {} ]", clazzEntries.size(),
                file.getAbsolutePath());
    }

    @Override
    public synchronized void scan(File[] files) throws IOException {
        if (files == null) {
            throw new IllegalArgumentException("null files");
        }
        for (File file : files) {
            try {
                scan0(file);
            } catch (Exception e) {
                if (logger == null) {
                    return;
                }
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void scanFile(File file, String pathName) throws IOException {

        if (!file.exists()) {
            LoggerUtil.prettyLog(logger, "file or directory [ {} ] not found",
                    file.getAbsoluteFile());
            return;
        }

        if (excludePaths.contains(pathName)) {
            return;
        }

        if (file.isDirectory()) {

            File[] files = file.listFiles();

            for (File _file : files) {
                scanFile(_file, pathName + "/" + _file.getName());
            }

            return;
        }

        addResource(file, pathName);
    }

    private void addResource(File file, String pathName) throws IOException {

        String fileName = file.getName();

        addResource(file, pathName, fileName);
    }

    private void addResource(File file, String filePathName, String fileName) throws IOException {

        URL url = file.toURI().toURL();

        if (fileName.endsWith(".jar")) {
            scanZip(file, new JarFile(file));
            addURL(url);
            return;
        }

        filePathName = filePathName.substring(1);

        if (endWidthClass(filePathName)) {
            ClassEntry classEntry = storeClass(filePathName, url.openStream());
            if (classEntry == null) {
                return;
            }
            classEntry.codeBase = url;
            return;
        }

        addResource(url, filePathName, fileName);
    }

    private void scanZip(File realFile, JarFile file) throws IOException {

        try {

            LoggerUtil.prettyLog(logger, "load file [ {} ]", file.getName());

            Enumeration<JarEntry> entries = file.entries();

            for (; entries.hasMoreElements();) {

                JarEntry entry = entries.nextElement();

                if (entry.isDirectory()) {
                    continue;
                }

                String filePathName = entry.getName();

                if (endWidthClass(filePathName)) {
                    ClassEntry classEntry = storeClass(entry.getName(), file.getInputStream(entry));
                    if (classEntry == null) {
                        continue;
                    }
                    classEntry.certificates = entry.getCertificates();
                    classEntry.codeBase = realFile.toURI().toURL();
                }
            }
        } finally {

            CloseUtil.close(file);
        }
    }

    private boolean endWidthClass(String filePathName) {
        return filePathName.endsWith(".class");
    }

    private ClassEntry storeClass(String filePathName, InputStream inputStream) throws IOException {

        String className = filePathName.replace('/', '.').substring(0, filePathName.length() - 6);

        if (matchSystem(className)) {
            return null;
        }

        if (clazzEntries.containsKey(className)) {
            throw new DuplicateClassException(className);
        }

        //		try {
        //
        //			parentClassLoader.loadClass(className);
        //
        //			throw new DuplicateClassException(className);
        //		} catch (ClassNotFoundException e) {
        //		}

        byte[] binaryContent = FileUtil.inputStream2ByteArray(inputStream);

        ClassEntry classEntry = new ClassEntry();

        classEntry.classBinary = binaryContent;

        classEntry.className = className;

        clazzEntries.put(className, classEntry);

        return classEntry;
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
                if (logger == null) {
                    return;
                }
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void unloadClassLoader() {

        Collection<ClassEntry> es = clazzEntries.values();

        for (ClassEntry e : es) {
            unloadClass(e.loadedClass);
        }
    }

    class ClassEntry {

        private byte[]        classBinary;

        private String        className;

        private Class<?>      loadedClass;

        private URL           codeBase     = null;

        private Certificate[] certificates = null;

    }

    class ResourceEnumeration implements Enumeration<URL> {

        private int       index = 0;

        private List<URL> urls;

        ResourceEnumeration(List<URL> urls) {
            this.urls = urls;
        }

        @Override
        public boolean hasMoreElements() {
            return index < urls.size();
        }

        @Override
        public URL nextElement() {

            if (!hasMoreElements()) {
                throw new NoSuchElementException();
            }

            return urls.get(index++);
        }
    };

    class CompoundEnumeration<E> implements Enumeration<E> {
        private Enumeration<E>[] enums;
        private int              index = 0;

        public CompoundEnumeration(Enumeration<E>[] paramArrayOfEnumeration) {
            this.enums = paramArrayOfEnumeration;
        }

        @Override
        public boolean hasMoreElements() {
            while (this.index < this.enums.length) {
                if ((this.enums[this.index] != null)
                        && (this.enums[this.index].hasMoreElements())) {
                    return true;
                }
                this.index += 1;
            }
            return false;
        }

        @Override
        public E nextElement() {
            if (!hasMoreElements()) {
                throw new NoSuchElementException();
            }
            return this.enums[this.index].nextElement();
        }
    }

    @Override
    public void addExcludePath(String path) {
        if (StringUtil.isNullOrBlank(path)) {
            return;
        }
        excludePaths.add(path);
    }

    @Override
    public void removeExcludePath(String path) {
        excludePaths.remove(path);
    }

    public void addMatchExtend(String extend) {
        if (StringUtil.isNullOrBlank(extend)) {
            return;
        }
        matchExtend.add(extend);
    }

    public boolean removeMatchExtend(String extend) {
        return matchExtend.remove(extend);
    }

    public void addMatchStartWith(String extend) {
        if (StringUtil.isNullOrBlank(extend)) {
            return;
        }
        matchStartWith.add(extend);
    }

    public boolean removeMatchStartWith(String extend) {
        if (systemMatch.contains(extend)) {
            return false;
        }
        return matchStartWith.remove(extend);
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

}
