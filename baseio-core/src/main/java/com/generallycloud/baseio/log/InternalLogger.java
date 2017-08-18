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

import java.util.Date;

import com.generallycloud.baseio.common.DateUtil;
import com.generallycloud.baseio.common.MessageFormatter;

/**
 * @author wangkai
 *
 */
public class InternalLogger implements Logger {

    private Class<?>      loggerClass;

    private String        debugClassName;

    private String        errorClassName;

    private String        infoClassName;

    private LoggerPrinter printer;

    public InternalLogger(LoggerPrinter printer, Class<?> clazz) {
        String className = clazz.getSimpleName() + " -";
        this.printer = printer;
        this.debugClassName = " [DEBUG] " + className;
        this.infoClassName = " [INFO] " + className;
        this.errorClassName = " [ERROR] " + className;
        this.loggerClass = clazz;
    }

    @Override
    public void debug(String message) {
        if (isEnableDebug()) {
            info0(debugClassName, message);
        }
    }

    @Override
    public void debug(String message, Object param) {
        if (isEnableDebug()) {
            info0(debugClassName, message, param);
        }
    }

    @Override
    public void debug(String message, Object param, Object param1) {
        if (isEnableDebug()) {
            info0(debugClassName, message, param, param1);
        }
    }

    @Override
    public void debug(String message, Object[] param) {
        if (isEnableDebug()) {
            info0(debugClassName, message, param);
        }
    }

    @Override
    public void debug(Throwable e) {
        if (isEnableDebug()) {
            printStackTrace(e);
        }
    }

    @Override
    public void error(String message) {
        printer.errPrintln(getTimeFormat() + errorClassName + message);
    }

    @Override
    public void error(String message, Throwable e) {
        error(message);
        printStackTrace(e);
    }

    @Override
    public void error(Throwable e) {
        printStackTrace(e);
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

    private String getTimeFormat() {
        return DateUtil.formatYyyy_MM_dd_HH_mm_ss_SSS(new Date());
    }

    private void info0(String className, String message) {
        printer.println(getTimeFormat() + className + message);
    }

    private void info0(String className, String message, Object param) {
        printer.println(getTimeFormat() + className + MessageFormatter.format(message, param));
    }

    private void info0(String className, String message, Object param, Object param1) {
        printer.println(
                getTimeFormat() + className + MessageFormatter.format(message, param, param1));
    }

    private void info0(String className, String message, Object[] param) {
        printer.println(getTimeFormat() + className + MessageFormatter.arrayFormat(message, param));
    }

    @Override
    public void info(String message) {
        info0(infoClassName, message);
    }

    @Override
    public void info(String message, Object param) {
        info0(infoClassName, message, param);
    }

    @Override
    public void info(String message, Object param, Object param1) {
        info0(infoClassName, message, param, param1);
    }

    @Override
    public void info(String message, Object[] param) {
        info0(infoClassName, message, param);
    }

    public void printStackTrace(Throwable t) {
        printer.errPrintThrowable(t);
    }

    public void setEnableDebug(boolean enable) {
        LoggerFactory.setEnableDebug(enable);
    }

    @Override
    public boolean isEnableDebug() {
        return LoggerFactory.isEnableDebug();
    }

    @Override
    public Class<?> getLoggerClass() {
        return loggerClass;
    }

}
