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
package com.firenio.baseio.log;

import org.slf4j.LoggerFactory;

public class SLF4JLogger implements Logger {

    private org.slf4j.Logger logger = null;

    private String           name;

    public SLF4JLogger(Class<?> clazz) {
        this(clazz.getSimpleName());
    }

    public SLF4JLogger(String name) {
        this.logger = LoggerFactory.getLogger(name);
        this.name = name;
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void debug(String msg, Object param) {
        logger.debug(msg, param);
    }

    @Override
    public void debug(String msg, Object... param) {
        logger.debug(msg, param);
    }

    @Override
    public void debug(String msg, Object param, Object param1) {
        logger.debug(msg, param, param1);
    }

    @Override
    public void debug(String msg, Throwable throwable) {
        logger.debug(msg, throwable);
    }

    @Override
    public void error(String object) {
        logger.error(object);
    }

    @Override
    public void error(String msg, Object param) {
        logger.error(msg, param);
    }

    @Override
    public void error(String msg, Object... params) {
        logger.error(msg, params);
    }

    @Override
    public void error(String msg, Object param, Object param1) {
        logger.error(msg, param, param1);
    }

    @Override
    public void error(String object, Throwable throwable) {
        logger.error(object, throwable);
    }

    @Override
    public void error(Throwable e) {
        logger.error(e.getMessage(), e);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String msg, Object param) {
        logger.info(msg, param);
    }

    @Override
    public void info(String msg, Object... param) {
        logger.info(msg, param);
    }

    @Override
    public void info(String msg, Object param, Object param1) {
        logger.info(msg, param, param1);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String msg, Object param) {
        logger.warn(msg, param);
    }

    @Override
    public void warn(String msg, Object... params) {
        logger.warn(msg, params);
    }

    @Override
    public void warn(String msg, Object param, Object param1) {
        logger.warn(msg, param, param1);
    }

    @Override
    public void warn(String msg, Throwable throwable) {
        logger.warn(msg, throwable);
    }

}
