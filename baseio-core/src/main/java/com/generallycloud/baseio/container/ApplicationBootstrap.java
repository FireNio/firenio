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

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.log.DebugUtil;

public class ApplicationBootstrap {

    public static final String RUNTIME_DEV  = "dev";
    public static final String RUNTIME_PROD = "prod";

    public static void startup() throws Exception {
        startup(System.getProperty("container.class"));
    }

    public static void startup(Class<?> clazz) throws Exception {
        Assert.notNull(clazz, "clazz");
        startup(clazz.getName());
    }

    public static void startup(String className) throws Exception {
        startup(className, withDefault(new ClassPathScaner() {

            @Override
            public void scanClassPaths(URLDynamicClassLoader classLoader, String mode,
                    String rootLocalAddress) throws IOException {
                if (!isRuntimeDevMode(mode)) {
                    classLoader.scan(rootLocalAddress + "/lib");
                }
            }
        }));
    }

    public static void startup(String className, List<ClassPathScaner> classPathScaners)
            throws Exception {
        String rootPath = URLDecoder.decode(FileUtil.getCurrentPath(), "UTF-8");
        String runtime = System.getProperty("container.runtime");
        startup(className, runtime, rootPath, classPathScaners);
    }

    public static void startup(String className, String mode, String rootPath,
            List<ClassPathScaner> cpScaners) throws Exception {
        Assert.notNull(className, "className");
        Assert.notNull(rootPath, "rootPath");
        Assert.notNull(cpScaners, "cpScaners");
        LoggerUtil.prettyLog(DebugUtil.getLogger(), "RUNTIME_MODE: {}", mode);
        LoggerUtil.prettyLog(DebugUtil.getLogger(), "ROOT_PATH: {}", rootPath);
        boolean isDevMode = isRuntimeDevMode(mode);
        ClassLoader parent = ApplicationBootstrap.class.getClassLoader();
        ClassLoader classLoader = newClassLoader(parent, mode, isDevMode, rootPath, cpScaners);
        Class<?> bootClass = classLoader.loadClass(className);
        Thread.currentThread().setContextClassLoader(classLoader);
        BootstrapEngine engine = (BootstrapEngine) bootClass.newInstance();
        engine.bootstrap(rootPath, mode);
    }

    public static boolean isRuntimeProdMode(String mode) {
        return RUNTIME_PROD.equalsIgnoreCase(mode);
    }

    public static boolean isRuntimeDevMode(String mode) {
        return RUNTIME_DEV.equalsIgnoreCase(mode);
    }

    public static URLDynamicClassLoader newClassLoader(ClassLoader parent, String mode,
            boolean entrustFirst, String rootLocalAddress, List<ClassPathScaner> classPathScaners)
            throws IOException {
        //这里需要设置优先委托自己加载class，因为到后面对象需要用该classloader去加载resources
        URLDynamicClassLoader classLoader = new URLDynamicClassLoader(parent, entrustFirst);
        classLoader.addMatchExtend(BootstrapEngine.class.getName());
        if (classPathScaners == null || classPathScaners.size() == 0) {
            throw new IOException("null classPathScaners");
        }
        for (ClassPathScaner scaner : classPathScaners) {
            if (scaner == null) {
                continue;
            }
            scaner.scanClassPaths(classLoader, mode, rootLocalAddress);
        }
        return classLoader;
    }

    public static List<ClassPathScaner> withDefault() {
        return withDefault(new ClassPathScaner[0]);
    }

    public static List<ClassPathScaner> withDefault(ClassPathScaner... scaners) {
        List<ClassPathScaner> classPathScaners = new ArrayList<>();
        classPathScaners.add(new DefaultClassPathScaner());
        if (scaners != null) {
            for (ClassPathScaner scaner : scaners) {
                if (scaner == null) {
                    continue;
                }
                classPathScaners.add(scaner);
            }
        }
        return classPathScaners;
    }

    public interface ClassPathScaner {
        void scanClassPaths(URLDynamicClassLoader classLoader, String mode, String rootPath)
                throws IOException;
    }

    static class DefaultClassPathScaner implements ClassPathScaner {

        @Override
        public void scanClassPaths(URLDynamicClassLoader classLoader, String mode, String rootPath)
                throws IOException {
            if (isRuntimeDevMode(mode)) {
                classLoader.addExcludePath("/app");
                classLoader.scan(rootPath);
            } else {
                classLoader.scan(rootPath + "/conf");
            }
        }
    }

}
