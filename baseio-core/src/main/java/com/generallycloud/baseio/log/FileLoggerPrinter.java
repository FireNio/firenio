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

import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.file.RAFOutputStream;

/**
 * @author wangkai
 *
 */
public class FileLoggerPrinter implements LoggerPrinter {

    private RAFOutputStream outputStream;

    public FileLoggerPrinter(File file) throws IOException {
        if (!file.exists()) {
            FileUtil.createDirectory(file.getParentFile());
        }
        this.outputStream = new RAFOutputStream(file);
    }

    @Override
    public synchronized void println(String msg) {
        try {
            outputStream.write((msg + "\n").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void printThrowable(Throwable t) {
        String msg = DebugUtil.exception2string(t);
        println(msg);
    }

    @Override
    public void errPrintln(String msg) {
        println(msg);
    }

    @Override
    public void errPrintThrowable(Throwable t) {
        printThrowable(t);
    }

}
