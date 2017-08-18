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
package com.generallycloud.baseio.container;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.Parameters;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class FileReceiveUtil {

    public static final String FILE_NAME   = "file-name";

    public static final String IS_END      = "isEnd";

    private Logger             logger      = LoggerFactory.getLogger(FileReceiveUtil.class);

    private int                num;

    private String             prefix;

    private String             ACCEPT_FILE = "accept-file";

    public FileReceiveUtil(String prefix) {
        this.prefix = prefix;
    }

    public void accept(SocketSession session, ProtobaseFuture future, boolean callback)
            throws Exception {

        Parameters parameters = future.getParameters();

        OutputStream outputStream = (OutputStream) session.getAttribute(ACCEPT_FILE);

        if (outputStream == null) {

            String fileName = prefix + parameters.getParameter(FILE_NAME);

            outputStream = new FileOutputStream(new File(fileName));

            session.setAttribute(ACCEPT_FILE, outputStream);

            logger.info("accept...................open,file={}", fileName);
        }

        byte[] data = future.getBinary();

        outputStream.write(data, 0, future.getBinaryLength());

        logger.info("accept...................{},{}", future.getBinaryLength(), (num++));

        boolean isEnd = parameters.getBooleanParameter(IS_END);

        if (isEnd) {

            logger.info("accept...................close,stream={}", outputStream);

            CloseUtil.close(outputStream);

            session.removeAttribute(ACCEPT_FILE);

            if (callback) {

                future.write("传输成功！");

                session.flush(future);
            }
        }
    }
}
