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
package com.generallycloud.test.io.fixedlength;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFrame;
import com.generallycloud.baseio.common.Util;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.SslContext;
import com.generallycloud.baseio.component.SslContextBuilder;
import com.generallycloud.baseio.log.DebugUtil;
import com.generallycloud.baseio.protocol.Frame;

public class TestFIxedLengthClient {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                System.out.println();
                System.out.println("____________________" + frame);
                System.out.println();
            }
        };

        SslContext sslContext = SslContextBuilder.forClient(true).build();
        ChannelConnector context = new ChannelConnector(8300);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        //		context.addChannelEventListener(new ChannelActiveSEListener());
        context.setProtocolCodec(new FixedLengthCodec());
        context.setSslContext(sslContext);
        NioSocketChannel channel = context.connect();
        FixedLengthFrame frame = new FixedLengthFrame();
        frame.write("hello server!", channel);
        channel.flush(frame);
        Util.sleep(100);
        Util.close(context);
        DebugUtil.debug("连接已关闭。。。");
    }

}
