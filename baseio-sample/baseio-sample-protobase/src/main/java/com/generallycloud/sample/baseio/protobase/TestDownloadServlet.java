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
package com.generallycloud.sample.baseio.protobase;

import java.io.File;
import java.io.IOException;

import com.generallycloud.baseio.codec.protobase.ParamedProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.ProtobaseFuture;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.protobase.FileReceiveUtil;
import com.generallycloud.baseio.container.protobase.FileSendUtil;
import com.generallycloud.baseio.container.protobase.ProtobaseFutureAcceptorService;

public class TestDownloadServlet extends ProtobaseFutureAcceptorService {

    public static final String SERVICE_NAME = TestDownloadServlet.class.getSimpleName();

    @Override
    protected void doAccept(NioSocketChannel channel, ParamedProtobaseFuture future) throws Exception {
        FileSendUtil fileSendUtil = new FileSendUtil();
        File file = new File(future.getParameters().getParameter(FileReceiveUtil.FILE_NAME));
        if (!file.exists()) {
            fileNotFound(channel, future, "file not found");
            return;
        }
        fileSendUtil.sendFile(channel, future.getFutureName(), file, 1024 * 800);
    }

    private void fileNotFound(NioSocketChannel channel, ParamedProtobaseFuture future, String msg)
            throws IOException {
        future.put("code", 404);
        channel.flush(future);
    }
}
