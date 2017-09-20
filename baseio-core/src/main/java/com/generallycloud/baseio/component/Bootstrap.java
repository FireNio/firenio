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

import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.log.DebugUtil;

/**
 * @author wangkai
 *
 */
public class Bootstrap {

    public static void startup(String className, boolean deployModel) throws Exception {

        String rootPath = FileUtil.getCurrentPath();

        DebugUtil.info(" ROOT_PATH: {}", rootPath);

        startup(className, rootPath, deployModel);
    }

    public static void startup(String className, String rootPath, boolean deployModel)
            throws Exception {

        URLDynamicClassLoader classLoader = newClassLoader(deployModel, rootPath);

        Class<?> bootClass = classLoader.loadClass(className);

        Thread.currentThread().setContextClassLoader(classLoader);

        BootstrapEngine engine = (BootstrapEngine) bootClass.newInstance();

        engine.bootstrap(rootPath, deployModel);
    }

    private static URLDynamicClassLoader newClassLoader(boolean deployModel,
            String rootLocalAddress) throws IOException {
        //这里需要设置优先委托自己加载class，因为到后面对象需要用该classloader去加载resources
        ClassLoader parent = Bootstrap.class.getClassLoader();
        URLDynamicClassLoader classLoader = new URLDynamicClassLoader(parent,false);
        classLoader.addMatchExtend(BootstrapEngine.class.getName());
        if (deployModel) {
            classLoader.scan(new File(rootLocalAddress + "/lib"));
            classLoader.scan(new File(rootLocalAddress + "/conf"));
        } else {
            classLoader.addExcludePath("/app");
            classLoader.scan(new File(rootLocalAddress));
        }
        return classLoader;
    }

}
