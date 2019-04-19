/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.log;

import java.io.File;
import java.io.IOException;

public class LoggerFactory {

    public static final int           LEVEL_DEBUG       = 4;
    public static final int           LEVEL_ERROR       = 1;
    public static final int           LEVEL_INFO        = 3;
    public static final int           LEVEL_WARN        = 2;
    private static      boolean       enableSLF4JLogger = false;
    private static      File          internalLogFile;
    private static      int           logLevel          = Integer.MAX_VALUE;
    private static      LoggerPrinter printer           = SysLoggerPrinter.get();

    static {
        configure();
    }

    private static void configure() {
        try {
            Class.forName("org.slf4j.LoggerFactory");
            enableSLF4JLogger = true;
        } catch (ClassNotFoundException e) {
        }
    }

    public static File getInternalLogFile() {
        return internalLogFile;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    public static Logger getLogger(String name) {
        if (!enableSLF4JLogger) {
            return new InternalLogger(printer, name);
        }
        return new SLF4JLogger(name);
    }

    public static int getLogLevel() {
        return logLevel;
    }

    public static boolean isDebugEnabled() {
        return logLevel > LEVEL_DEBUG;
    }

    public static boolean isEnableSLF4JLogger() {
        return enableSLF4JLogger;
    }

    public static boolean isErrorEnabled() {
        return logLevel > LEVEL_ERROR;
    }

    public static boolean isInfoEnabled() {
        return logLevel > LEVEL_INFO;
    }

    public static boolean isWarnEnabled() {
        return logLevel > LEVEL_WARN;
    }

    public static void setEnableSLF4JLogger(boolean enable) {
        enableSLF4JLogger = enable;
    }

    public static void setInternalLogFile(File internalLogFile) throws IOException {
        LoggerFactory.internalLogFile = internalLogFile;
        LoggerPrinter[] printers = new LoggerPrinter[2];
        printers[0] = SysLoggerPrinter.get();
        printers[1] = new FileLoggerPrinter(internalLogFile);
        printer = new CompoundLoggerPrinter(printers);
    }

    public static void setLogLevel(int logLevel) {
        LoggerFactory.logLevel = logLevel + 1;
    }

}
