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

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.log.DebugUtil;

/**
 * @author wangkai
 *
 */
public final class Bootstrap {

    public static void startup(String className, boolean deployModel,
            List<ClassPathScaner> classPathScaners) throws Exception {
        String rootPath = URLDecoder.decode(FileUtil.getCurrentPath(), "UTF-8");
        DebugUtil.info(" ROOT_PATH: {}", rootPath);
        startup(className, rootPath, deployModel, classPathScaners);
    }

    public static void startup(String className, String rootPath,
            boolean deployModel, List<ClassPathScaner> classPathScaners)
            throws Exception {
        ClassLoader parent = Bootstrap.class.getClassLoader();
        URLDynamicClassLoader classLoader = newClassLoader(parent, deployModel,
                !deployModel, rootPath, classPathScaners);
        Class<?> bootClass = classLoader.loadClass(className);
        Thread.currentThread().setContextClassLoader(classLoader);
        BootstrapEngine engine = (BootstrapEngine) bootClass.newInstance();
        engine.bootstrap(rootPath, deployModel);
    }

    public static URLDynamicClassLoader newClassLoader(ClassLoader parent,
            boolean deployModel, boolean entrustFirst, String rootLocalAddress,
            List<ClassPathScaner> classPathScaners) throws IOException {
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
            scaner.scanClassPaths(classLoader, deployModel, rootLocalAddress);
        }
        return classLoader;
    }

    public static List<ClassPathScaner> withDefault() {
        return withDefault(new ClassPathScaner[0]);
    }

    public static List<ClassPathScaner> withDefault(
            ClassPathScaner... scaners) {
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
        void scanClassPaths(URLDynamicClassLoader classLoader,
                boolean deployModel, String rootLocalAddress)
                throws IOException;
    }

    static class DefaultClassPathScaner implements ClassPathScaner {

        @Override
        public void scanClassPaths(URLDynamicClassLoader classLoader,
                boolean deployModel, String rootLocalAddress)
                throws IOException {
            if (deployModel) {
                classLoader.scan(rootLocalAddress + "/conf");
            } else {
                classLoader.addExcludePath("/app");
                classLoader.scan(rootLocalAddress);
            }
        }

    }

}
