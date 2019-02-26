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
package test.others;

import com.firenio.baseio.container.Bootstrap;
import com.firenio.baseio.container.BootstrapEngine;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class TestBootstrap implements BootstrapEngine {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void bootstrap(String rootPath, String mode) throws Exception {
        logger.info("startup ............");
        logger.info("runtime mode {}............", mode);
        logger.info("this class loader: {}", getClass().getClassLoader());
        logger.info("logger class loader: {}", logger.getClass().getClassLoader());
        logger.info("TestBootstrap class loader: {}", TestBootstrap.class.getClassLoader());
        logger.info("startup end ............");
    }

    public static void main(String[] args) throws Exception {
        System.setProperty(Bootstrap.BOOT_MODE, "prod");
        Bootstrap.startup("test.others.TestBootstrap", "/");
    }

}
