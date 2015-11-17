/*
 * Copyright 2015 The Baseio Project
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
package test.io.protobase;

import com.firenio.baseio.codec.protobase.ProtobaseCodec;
import com.firenio.baseio.codec.protobase.ProtobaseFrame;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelActiveIdleEventListener;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.NioEventLoopGroup;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.log.DebugUtil;

public class TestProtobaseHeartBeat {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                DebugUtil.debug("______________" + frame);
            }
        };

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setIdleTime(20);
        ChannelConnector context = new ChannelConnector(group, "127.0.0.1", 8300);
        context.addChannelIdleEventListener(new ChannelActiveIdleEventListener());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new ProtobaseCodec());
        context.setIoEventHandle(eventHandleAdaptor);
        Channel ch = context.connect();
        String param = "tttt";
        long old = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            Frame frame = new ProtobaseFrame();
            frame.setContent(ch.allocate());
            frame.write(param, context);
            ch.writeAndFlush(frame);
            Util.sleep(300);
        }
        System.out.println("Time:" + (System.currentTimeMillis() - old));
        Thread.sleep(2000);
        Util.close(context);
    }

}
