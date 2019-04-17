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
package com.firenio.container;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author wangkai
 */
public interface DynamicClassLoader extends Closeable {

    void addExcludePath(String path);

    void clearAssertionStatus();

    ClassLoader getParent();

    URL getResource(String name);

    InputStream getResourceAsStream(String name);

    Enumeration<URL> getResources(String name) throws IOException;

    Class<?> loadClass(String name) throws ClassNotFoundException;

    void removeExcludePath(String path);

    void scan(File file) throws IOException;

    void scan(File[] files) throws IOException;

    void scan(String file) throws IOException;

    void setClassAssertionStatus(String className, boolean enabled);

    void setDefaultAssertionStatus(boolean enabled);

    void setPackageAssertionStatus(String packageName, boolean enabled);

    void unloadClassLoader();

    class DuplicateClassException extends IOException {

        private static final long serialVersionUID = 1L;

        public DuplicateClassException(String msg) {
            super(msg);
        }

        public DuplicateClassException(String msg, Throwable throwable) {
            super(msg, throwable);
        }
    }

}
