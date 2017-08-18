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
package com.generallycloud.baseio.log;

import org.slf4j.LoggerFactory;

public class SLF4JLogger implements Logger {

    private org.slf4j.Logger logger      = null;

    private Class<?>         loggerClass = null;

    public SLF4JLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.loggerClass = clazz;
    }

    @Override
    public Class<?> getLoggerClass() {
        return loggerClass;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, Object param) {
        logger.info(message, param);

    }

    @Override
    public void info(String message, Object param, Object param1) {
        logger.info(message, param, param1);

    }

    @Override
    public void info(String message, Object[] param) {
        logger.info(message, param);

    }

    @Override
    public void debug(String message) {
        logger.debug(message);

    }

    @Override
    public void debug(String message, Object param) {
        logger.debug(message, param);

    }

    @Override
    public void debug(String message, Object param, Object param1) {
        logger.debug(message, param, param1);
    }

    @Override
    public void debug(String message, Object[] param) {
        logger.debug(message, param);
    }

    @Override
    public void error(String object, Throwable throwable) {
        logger.error(object, throwable);
    }

    @Override
    public void error(String object) {
        logger.error(object);
    }

    @Override
    public void debug(Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.error(throwable.getMessage(), throwable);
        }
    }

    @Override
    public boolean isEnableDebug() {
        return logger.isDebugEnabled();
    }

    @Override
    public void error(Throwable e) {
        logger.info(e.getMessage(), e);
    }

}
