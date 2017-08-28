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
package com.generallycloud.test.others;

import java.io.File;
import java.io.IOException;

import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class TestLog {

    public static void main(String[] args) throws Exception {

        testInternalLogger();

    }

    static void testInternalLogger() throws IOException {
        LoggerFactory.setEnableSLF4JLogger(false);
        LoggerFactory.setInternalLogFile(new File("D://test/main.log"));
        LoggerFactory.setEnableDebug(true);
        Logger logger = LoggerFactory.getLogger(TestLog.class);

        for (int i = 0; i < 1000; i++) {
            logger.info("logback info 成功了..............................................");
            logger.error("logback error 成功了..............................................");
            logger.debug("logback debug 成功了..............................................");
        }
    }

    static void testSl4J() {
        Logger logger = LoggerFactory.getLogger(TestLog.class);
        for (int i = 0; i < 1000000; i++) {
            logger.info("logback info 成功了..............................................");
            logger.error("logback error 成功了..............................................");
            logger.debug("logback debug 成功了..............................................");
        }
    }

}
