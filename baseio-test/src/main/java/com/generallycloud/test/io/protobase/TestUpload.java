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
package com.generallycloud.test.io.protobase;

import java.io.File;

import com.generallycloud.baseio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.container.FileSendUtil;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

public class TestUpload {

    static SocketChannelConnector connector = null;

    public static void main(String[] args) throws Exception {

        String serviceName = "TestUploadServlet";

        IoEventHandleAdaptor eventHandle = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                ProtobaseFuture f = (ProtobaseFuture) future;
                System.out.println();
                System.out.println(f.getReadText());
                System.out.println();

                CloseUtil.close(connector);

            }

            @Override
            public void futureSent(SocketSession session, Future future) {
                ProtobaseFuture f = (ProtobaseFuture) future;
                System.out.println("报文已发送：" + f.getReadText());
            }
        };

        LoggerFactory.configure();

        ServerConfiguration configuration = new ServerConfiguration(8800);

        SocketChannelContext context = new NioSocketChannelContext(configuration);

        connector = new SocketChannelConnector(context);

        context.setIoEventHandleAdaptor(eventHandle);

        context.setProtocolFactory(new ProtobaseProtocolFactory());

        context.addSessionEventListener(new LoggerSocketSEListener());
        SocketSession session = connector.connect();

        String fileName = "lantern-installer-beta.exe";

        fileName = "flashmail-2.4.exe";

        //		fileName = "jdk-8u102-windows-x64.exe";

        File file = new File("D:/TEMP/" + fileName);

        FileSendUtil fileSendUtil = new FileSendUtil();

        fileSendUtil.sendFile(session, serviceName, file, 1024 * 800);

    }
}
