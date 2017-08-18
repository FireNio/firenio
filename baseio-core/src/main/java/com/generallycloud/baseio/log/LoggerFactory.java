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

import java.io.File;
import java.io.IOException;

public class LoggerFactory {

    private static boolean       enableSLF4JLogger = false;
    private static boolean       enableDebug       = false;
    private static File          internalLogFile;
    private static LoggerPrinter printer           = SysLoggerPrinter.get();

    static {
        configure();
    }

    public static void setEnableSLF4JLogger(boolean enable) {
        enableSLF4JLogger = enable;
    }

    public static Logger getLogger(Class<?> clazz) {
        if (!enableSLF4JLogger) {
            return new InternalLogger(printer, clazz);
        }
        return new SLF4JLogger(clazz);
    }

    public static void configure() {
        try {
            Class.forName("org.slf4j.LoggerFactory");
            enableSLF4JLogger = true;
        } catch (ClassNotFoundException e) {}
    }

    public static boolean isEnableSLF4JLogger() {
        return enableSLF4JLogger;
    }

    public static boolean isEnableDebug() {
        return enableDebug;
    }

    public static void setEnableDebug(boolean enableDebug) {
        LoggerFactory.enableDebug = enableDebug;
    }

    public static File getInternalLogFile() {
        return internalLogFile;
    }

    public static void setInternalLogFile(File internalLogFile) throws IOException {
        LoggerFactory.internalLogFile = internalLogFile;
        LoggerPrinter[] printers = new LoggerPrinter[2];
        printers[0] = SysLoggerPrinter.get();
        printers[1] = new FileLoggerPrinter(internalLogFile);
        printer = new CompoundLoggerPrinter(printers);
    }

}
