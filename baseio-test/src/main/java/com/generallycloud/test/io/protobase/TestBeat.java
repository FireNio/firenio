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

import com.generallycloud.baseio.codec.protobase.ParamedProtobaseFrame;
import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelActiveIdleEventListener;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.log.DebugUtil;
import com.generallycloud.baseio.protocol.Frame;

public class TestBeat {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                DebugUtil.debug("______________" + frame);
            }
        };

        String serviceKey = "TestSimpleServlet";
        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setIdleTime(10);
        ChannelContext context = new ChannelContext(8300);
        ChannelConnector connector = new ChannelConnector(context,group);
        context.addChannelIdleEventListener(new ChannelActiveIdleEventListener());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setProtocolCodec(new ProtobaseCodec());
        context.setIoEventHandle(eventHandleAdaptor);
        NioSocketChannel channel = connector.connect();
        String param = "tttt";
        long old = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            Frame frame = new ParamedProtobaseFrame(serviceKey);
            frame.write(param, context);
            channel.flush(frame);
            ThreadUtil.sleep(300);
        }
        System.out.println("Time:" + (System.currentTimeMillis() - old));
        Thread.sleep(2000);
        CloseUtil.close(connector);
    }

}
