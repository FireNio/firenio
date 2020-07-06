/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.boot;

import java.io.ByteArrayOutputStream;
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
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DynamicClassLoader extends URLClassLoader {

    static final ClassLoader PARENT_CL = DynamicClassLoader.class.getClassLoader();

    private final boolean                 checkDuplicate;
    private final List<String>            notEntrustPackageList;
    private       boolean                 unloaded     = false;
    private       Map<String, URL>        resourceMap  = new ConcurrentHashMap<>();
    private       Map<String, List<URL>>  resourcesMap = new ConcurrentHashMap<>();
    private       Map<String, ClassEntry> clazzEntries = new ConcurrentHashMap<>();

    public DynamicClassLoader() {
        this(true);
    }

    public DynamicClassLoader(boolean checkDuplicate) {
        this(null, null, checkDuplicate);
    }

    public DynamicClassLoader(List<String> notEntrustPackageList) {
        this(null, notEntrustPackageList, true);
    }

    public DynamicClassLoader(List<String> notEntrustPackageList, boolean checkDuplicate) {
        this(null, notEntrustPackageList, checkDuplicate);
    }

    public DynamicClassLoader(ClassLoader parent, List<String> notEntrustPackageList, boolean checkDuplicate) {
        super(new URL[]{}, parent == null ? PARENT_CL : parent);
        this.notEntrustPackageList = notEntrustPackageList;
        this.checkDuplicate = checkDuplicate;
    }

    private Class<?> defineClass(ClassEntry entry) {
        synchronized (entry) {
            if (entry.loadedClass != null) {
                return entry.loadedClass;
            }
            if (entry.classBinary == null) {
                // this class may be unloaded.
                return null;
            }
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                String name = entry.className;
                int    i    = name.lastIndexOf('.');
                if (i != -1) {
                    sm.checkPackageAccess(name.substring(0, i));
                }
            }
            String     name  = entry.className;
            byte[]     cb    = entry.classBinary;
            CodeSource cs    = new CodeSource(entry.codeBase, entry.certificates);
            Class<?>   clazz = defineClass(name, cb, 0, cb.length, cs);
            entry.loadedClass = clazz;
            entry.classBinary = null;
            return clazz;
        }
    }

    private boolean isClassFile(String fileName) {
        return fileName.endsWith(".class");
    }

    @Override
    protected Class<?> findClass(String name) {
        ClassEntry entry = clazzEntries.get(name);
        if (entry == null) {
            return null;
        }
        if (entry.loadedClass == null) {
            return defineClass(entry);
        }
        return entry.loadedClass;
    }

    private Class<?> getClass(String name) {
        ClassEntry entry = clazzEntries.get(name);
        if (entry == null) {
            return null;
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

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL>[] temp = new Enumeration[2];
        temp[0] = findResources0(name);
        temp[1] = super.findResources(name);
        return new CompoundEnumeration<>(temp);
    }

    private Enumeration<URL> findResources0(String name) {
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

    private Class entrustLoadClass(String name) {
        try {
            return getParent().loadClass(name);
        } catch (Throwable e) {
            return null;
        }
    }

    private void checkUnload() {
        if (unloaded) {
            throw new RuntimeException("Class loader unloaded.");
        }
    }

    private boolean isNotEntrust(String name) {
        if (notEntrustPackageList != null) {
            for (int i = 0; i < notEntrustPackageList.size(); i++) {
                if (name.startsWith(notEntrustPackageList.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (isNotEntrust(name)) {
            Class clazz = getClass(name);
            if (clazz == null) {
                clazz = findClass(name);
                if (clazz == null) {
                    throw new ClassNotFoundException(name);
                }
                if (resolve) {
                    resolveClass(clazz);
                }
            }
            return clazz;
        }
        Class<?> clazz = entrustLoadClass(name);
        if (clazz == null) {
            clazz = super.loadClass(name, resolve);
            if (clazz == null) {
                throw new ClassNotFoundException(name);
            }
            return clazz;
        }
        if (!clazz.isInterface()) {
            checkDuplicate(name);
        }
        return clazz;
    }

    private void checkDuplicate(String name) {
        if (checkDuplicate) {
            ClassEntry entry = clazzEntries.get(name);
            if (entry != null) {
                throw new RuntimeException("Duplicate class " + name + " in " + entry.codeBase);
            }
        }
    }

    public Map<String, ClassEntry> getClazzEntries() {
        return clazzEntries;
    }

    public void scan(String file) throws IOException {
        scan(new File(file));
    }

    public void scan(File file) throws IOException {
        if (file != null && file.exists()) {
            if (file.exists()) {
                this.scanFile(file, null);
                this.scanFile(file, ".", ".");
            }
        }
    }

    private void scanFile(File file, String pathName) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    if (pathName == null) {
                        scanFile(f, f.getName());
                    } else {
                        scanFile(f, pathName + "/" + f.getName());
                    }
                }
            } else {
                scanFile(file, pathName, file.getName());
            }
        }
    }

    private synchronized void scanFile(File file, String filePathName, String fileName) throws IOException {
        checkUnload();
        URL url = file.toURI().toURL();
        if (fileName.endsWith(".jar")) {
            try (JarFile jarFile = new JarFile(file)) {
                scanZip(file, jarFile);
            }
            addURL(url);
            return;
        }
        if (isClassFile(filePathName)) {
            try (InputStream in = url.openStream()) {
                ClassEntry classEntry = toClassEntry(filePathName, in);
                classEntry.codeBase = url;
                clazzEntries.put(classEntry.className, classEntry);
            }
        } else {
            storeResource(url, filePathName, fileName);
        }
    }

    private void scanZip(File realFile, JarFile file) throws IOException {
        Enumeration<JarEntry> entries = file.entries();
        for (; entries.hasMoreElements(); ) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            String filePathName = entry.getName();
            if (isClassFile(filePathName)) {
                try (InputStream in = file.getInputStream(entry)) {
                    ClassEntry classEntry = toClassEntry(entry.getName(), in);
                    classEntry.certificates = entry.getCertificates();
                    classEntry.codeBase = realFile.toURI().toURL();
                    clazzEntries.put(classEntry.className, classEntry);
                }
            }
        }
    }

    private ClassEntry toClassEntry(String filePathName, InputStream inputStream) throws IOException {
        String     className  = filePathName.replace('/', '.').substring(0, filePathName.length() - 6);
        ClassEntry classEntry = new ClassEntry();
        classEntry.className = className;
        classEntry.classBinary = inputStreamToBytes(inputStream);
        return classEntry;
    }

    private byte[] inputStreamToBytes(InputStream inputStream) throws IOException {
        byte[] data = new byte[inputStream.available()];
        int    read = inputStream.read(data);
        if (inputStream.available() == 0) {
            return data;
        }
        int outSize = data.length;
        if (outSize == read) {
            outSize <<= 1;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(outSize);
        out.write(data, 0, read);
        for (; inputStream.available() > 0; ) {
            read = inputStream.read(data);
            out.write(data, 0, read);
        }
        return out.toByteArray();
    }

    private void storeResource(URL url, String pathName, String fileName) {
        resourceMap.put(pathName, url);
        List<URL> urls = resourcesMap.get(fileName);
        if (urls == null) {
            urls = new ArrayList<>();
            resourcesMap.put(fileName, urls);
        }
        urls.add(url);
    }

    private void unloadClass(Class<?> clazz) {
        if (clazz != null) {
            try {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        try {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            field.set(null, null);
                        } catch (Throwable e) {
                        }
                    }
                }
            } catch (Throwable e) {
            }
        }
    }

    public synchronized void unloadClassLoader() {
        if (!unloaded) {
            unloaded = true;
            for (ClassEntry e : clazzEntries.values()) {
                synchronized (e) {
                    e.certificates = null;
                    e.classBinary = null;
                    e.codeBase = null;
                    unloadClass(e.loadedClass);
                }
            }
            this.clazzEntries.clear();
            this.resourceMap.clear();
            for (List<URL> list : resourcesMap.values()) {
                list.clear();
            }
            this.resourcesMap.clear();
            try {
                this.close();
            } catch (Throwable e) {
            }
        }
    }

    protected static class ClassEntry {
        URL           codeBase;
        String        className;
        byte[]        classBinary;
        Class<?>      loadedClass;
        Certificate[] certificates;
    }

    class CompoundEnumeration<E> implements Enumeration<E> {
        private Enumeration<E>[] enums;
        private int              index = 0;

        public CompoundEnumeration(Enumeration<E>[] paramArrayOfEnumeration) {
            this.enums = paramArrayOfEnumeration;
        }

        @Override
        public boolean hasMoreElements() {
            while (this.index < this.enums.length) {
                if ((this.enums[this.index] != null) && (this.enums[this.index].hasMoreElements())) {
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

}
