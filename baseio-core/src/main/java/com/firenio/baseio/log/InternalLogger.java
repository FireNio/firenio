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

import java.util.Date;

import com.firenio.baseio.common.DateUtil;

/**
 * @author wangkai
 *
 */
public class InternalLogger implements Logger {

    private String        debugClassName;
    private String        errorClassName;
    private String        infoClassName;
    private String        name;
    private LoggerPrinter printer;
    private String        warnClassName;

    public InternalLogger(LoggerPrinter printer, Class<?> clazz) {
        this(printer, clazz.getSimpleName());
    }

    public InternalLogger(LoggerPrinter printer, String name) {
        String className = name + " -";
        this.printer = printer;
        this.debugClassName = " [DEBUG] " + className;
        this.infoClassName = " [INFO] " + className;
        this.errorClassName = " [ERROR] " + className;
        this.warnClassName = " [WARN] " + className;
        this.name = name;
    }

    @Override
    public void debug(String msg) {
        if (isDebugEnabled()) {
            print(debugClassName, msg);
        }
    }

    @Override
    public void debug(String msg, Object param) {
        if (isDebugEnabled()) {
            print(debugClassName, msg, param);
        }
    }

    @Override
    public void debug(String msg, Object... param) {
        if (isDebugEnabled()) {
            print(debugClassName, msg, param);
        }
    }

    @Override
    public void debug(String msg, Object param, Object param1) {
        if (isDebugEnabled()) {
            print(debugClassName, msg, param, param1);
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (isDebugEnabled()) {
            print(debugClassName, msg);
            printer.printThrowable(t);
        }
    }

    @Override
    public void error(String msg) {
        if (isErrorEnabled()) {
            print(errorClassName, msg);
        }
    }

    @Override
    public void error(String msg, Object param) {
        if (isErrorEnabled()) {
            print(errorClassName, msg, param);
        }
    }

    @Override
    public void error(String msg, Object... params) {
        if (isErrorEnabled()) {
            print(errorClassName, msg, params);
        }
    }

    @Override
    public void error(String msg, Object param, Object param1) {
        if (isErrorEnabled()) {
            print(errorClassName, msg, param, param1);
        }
    }

    @Override
    public void error(String msg, Throwable e) {
        if (isErrorEnabled()) {
            error(msg);
            printer.printThrowable(e);
        }
    }

    @Override
    public void error(Throwable e) {
        if (isErrorEnabled()) {
            printer.printThrowable(e);
        }
    }

    public String exception2string(Throwable exception) {
        StackTraceElement[] es = exception.getStackTrace();
        StringBuilder builder = new StringBuilder();
        builder.append(exception.toString());
        for (StackTraceElement e : es) {
            builder.append("\n\tat ");
            builder.append(e.toString());
        }
        return builder.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    private String getTimeFormat() {
        return DateUtil.get().formatYyyy_MM_dd_HH_mm_ss_SSS(new Date());
    }

    @Override
    public void info(String msg) {
        if (isInfoEnabled()) {
            print(infoClassName, msg);
        }
    }

    @Override
    public void info(String msg, Object param) {
        if (isInfoEnabled()) {
            print(infoClassName, msg, param);
        }
    }

    @Override
    public void info(String msg, Object... params) {
        if (isInfoEnabled()) {
            print(infoClassName, msg, params);
        }
    }

    @Override
    public void info(String msg, Object param, Object param1) {
        if (isInfoEnabled()) {
            print(infoClassName, msg, param, param1);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return LoggerFactory.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return LoggerFactory.isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return LoggerFactory.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return LoggerFactory.isWarnEnabled();
    }

    private void print(String className, String msg) {
        printer.println(getTimeFormat() + className + msg);
    }

    private void print(String className, String msg, Object param) {
        printer.println(getTimeFormat() + className + Log.format(msg, param));
    }

    private void print(String className, String msg, Object... param) {
        printer.println(getTimeFormat() + className + Log.arrayFormat(msg, param));
    }

    private void print(String className, String msg, Object param, Object param1) {
        printer.println(getTimeFormat() + className + Log.format(msg, param, param1));
    }

    @Override
    public void warn(String msg) {
        if (isWarnEnabled()) {
            print(warnClassName, msg);
        }
    }

    @Override
    public void warn(String msg, Object param) {
        if (isWarnEnabled()) {
            print(warnClassName, msg, param);
        }
    }

    @Override
    public void warn(String msg, Object... params) {
        if (isWarnEnabled()) {
            print(warnClassName, msg, params);
        }
    }

    @Override
    public void warn(String msg, Object param, Object param1) {
        if (isWarnEnabled()) {
            print(warnClassName, msg, param, param1);
        }
    }

    @Override
    public void warn(String msg, Throwable throwable) {
        if (isWarnEnabled()) {
            print(warnClassName, msg);
            printer.printThrowable(throwable);
        }
    }

}
