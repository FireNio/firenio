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

import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ProtobaseFrame;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelAliveIdleEventListener;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.log.DebugUtil;
import com.generallycloud.baseio.protocol.Frame;

public class TestServer {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                ProtobaseFrame f = (ProtobaseFrame) frame;
                DebugUtil.debug("receive text:" + f.getReadText());
                frame.write("yes server already accept your text message:", channel);
                frame.write(f.getReadText(), channel);
                if (f.getReadBinarySize() > 0) {
                    DebugUtil.debug("receive binary:" + new String(f.getReadBinary()));
                    f.writeBinary("yes server already accept your binary message:".getBytes());
                    f.writeBinary(f.getReadBinary());
                }
                channel.flush(frame);
            }
        };

        ChannelContext context = new ChannelContext(8300);
        ChannelAcceptor acceptor = new ChannelAcceptor(context);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelIdleEventListener(new ChannelAliveIdleEventListener());
        context.setIoEventHandle(eventHandleAdaptor);
        context.setProtocolCodec(new ProtobaseCodec());
        acceptor.bind();
    }

}
