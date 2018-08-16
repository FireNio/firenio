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

import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ParamedProtobaseFrame;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.protobase.FileSendUtil;
import com.generallycloud.baseio.protocol.Frame;

public class TestUpload {

    static ChannelConnector connector = null;

    public static void main(String[] args) throws Exception {

        String serviceName = "/test-upload";

        IoEventHandle eventHandle = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                ParamedProtobaseFrame f = (ParamedProtobaseFrame) frame;
                System.out.println();
                System.out.println(f.getReadText());
                System.out.println();

                CloseUtil.close(connector);
            }
        };

        ChannelContext context = new ChannelContext(8300);
        connector = new ChannelConnector(context);
        context.setIoEventHandle(eventHandle);
        context.setProtocolCodec(new ProtobaseCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        NioSocketChannel channel = connector.connect();
        String fileName = "lantern-installer-beta.exe";
        fileName = "content.rar";
        //		fileName = "jdk-8u102-windows-x64.exe";
        File file = new File("c:/ryms/" + fileName);
        FileSendUtil fileSendUtil = new FileSendUtil();
        fileSendUtil.sendFile(channel, serviceName, file, 1024 * 800);
        ThreadUtil.sleep(10000000);
    }

}
