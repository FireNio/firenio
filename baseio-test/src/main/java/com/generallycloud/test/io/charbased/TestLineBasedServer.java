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
package com.generallycloud.test.io.charbased;

import java.io.File;

import com.generallycloud.baseio.codec.charbased.CharBasedCodec;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.SslContext;
import com.generallycloud.baseio.component.SslContextBuilder;
import com.generallycloud.baseio.protocol.Frame;

public class TestLineBasedServer {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                String res = "yes server already accept your message:" + frame;
                frame.write(res, channel.getCharset());
                channel.flush(frame);
            }
        };

        File certificate = FileUtil.readFileByCls("generallycloud.com.crt");
        File privateKey = FileUtil.readFileByCls("generallycloud.com.key");
        SslContext sslContext = SslContextBuilder.forServer().keyManager(privateKey, certificate)
                .build();
        ChannelAcceptor context = new ChannelAcceptor(8300);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setIoEventHandle(eventHandleAdaptor);
        context.setProtocolCodec(new CharBasedCodec());
        context.setSslContext(sslContext);
        context.bind();

    }
}
