/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.firenio.baseio.common.Assert;
import com.firenio.baseio.common.FileUtil;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.log.DebugUtil;

/**
 * @author wangkai
 *
 */
public class Bootstrap {

    public static final String BOOT_CLASS    = "boot.class";
    public static final String BOOT_LIB_PATH = "boot.libPath";
    public static final String BOOT_MODE     = "boot.mode";
    public static final String RUNTIME_DEV   = "dev";
    public static final String RUNTIME_PROD  = "prod";

    public interface ClassPathScaner {
        void scanClassPaths(URLDynamicClassLoader classLoader, String mode, String rootPath)
                throws IOException;
    }

    static class DefaultClassPathScaner implements ClassPathScaner {

        @Override
        public void scanClassPaths(URLDynamicClassLoader classLoader, String mode, String rootPath)
                throws IOException {
            String path = null;
            if (isRuntimeDevMode(mode)) {
                classLoader.addExcludePath("/app");
                path = rootPath;
            } else {
                path = rootPath + "/conf";
            }
            DebugUtil.getLogger().info("CLS_PATH: {}", path);
            classLoader.scan(path);
        }
    }

    public static boolean isRuntimeDevMode(String mode) {
        return RUNTIME_DEV.equalsIgnoreCase(mode);
    }

    public static boolean isRuntimeProdMode(String mode) {
        return RUNTIME_PROD.equalsIgnoreCase(mode);
    }

    public static void main(String[] args) throws Exception {
        String libPath = Util.getStringProperty(BOOT_LIB_PATH, "/app");
        startup(System.getProperty(BOOT_CLASS), libPath);
    }

    private static URLDynamicClassLoader newClassLoader(ClassLoader parent, String mode,
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

    public static void startup(String className, List<ClassPathScaner> cpScaners) throws Exception {
        Assert.notNull(className, "className");
        Assert.notNull(cpScaners, "cpScaners");
        String mode = Util.getStringProperty(BOOT_MODE, "dev");
        String rootPath = FileUtil.getCurrentPath();
        DebugUtil.getLogger().info("RUNTIME_MODE: {}", mode);
        DebugUtil.getLogger().info("ROOT_PATH: {}", rootPath);
        boolean isDevMode = isRuntimeDevMode(mode);
        ClassLoader parent = Bootstrap.class.getClassLoader();
        ClassLoader classLoader = newClassLoader(parent, mode, isDevMode, rootPath, cpScaners);
        Class<?> bootClass = classLoader.loadClass(className);
        Thread.currentThread().setContextClassLoader(classLoader);
        BootstrapEngine engine = (BootstrapEngine) bootClass.newInstance();
        engine.bootstrap(rootPath, mode);
    }

    public static void startup(final String bootClass, final String libPath) throws Exception {
        startup(bootClass, withDefault(new ClassPathScaner() {

            @Override
            public void scanClassPaths(URLDynamicClassLoader classLoader, String mode,
                    String rootLocalAddress) throws IOException {
                if (!isRuntimeDevMode(mode)) {
                    String path = rootLocalAddress + libPath;
                    DebugUtil.getLogger().info("CLS_PATH: {}", path);
                    classLoader.scan(path);
                }
            }
        }));
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

}
