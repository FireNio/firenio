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
package com.generallycloud.test.io.load.fixedlength;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.TextFrame;

public class TestLoadServer {

    public static final int     SERVER_CORE_SIZE       = 8;
    public static final int     CLIENT_CORE_SIZE       = SERVER_CORE_SIZE * 2;
    public static final int     WRITE_BUFFERS          = 16;
    public static final int     MEM_UNIT               = 256;
    public static final boolean ENABLE_SSL             = false;
    public static final boolean ENABLE_POOL            = true;
    public static final boolean ENABLE_WORK_EVENT_LOOP = false;
    public static final boolean ENABLE_POOL_DIRECT     = true;

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandle = new IoEventHandle() {
            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                TextFrame f = (TextFrame) frame;
                f.write(f.getReadText(), channel);
                channel.flush(frame);
            }
        };

        NioEventLoopGroup group = new NioEventLoopGroup(SERVER_CORE_SIZE);
        group.setMemoryPoolCapacity(1024 * 512);
        group.setWriteBuffers(WRITE_BUFFERS);
        group.setMemoryPoolUnit(MEM_UNIT);
        group.setEnableMemoryPool(ENABLE_POOL);
        group.setEnableMemoryPoolDirect(ENABLE_POOL_DIRECT);
        ChannelContext context = new ChannelContext(8300);
        ChannelAcceptor acceptor = new ChannelAcceptor(context, group);
        context.setMaxWriteBacklog(Integer.MAX_VALUE);
        context.setProtocolCodec(new FixedLengthCodec());
        context.setIoEventHandle(eventHandle);
        if (ENABLE_SSL) {
            context.setEnableSsl(true);
            context.setCertCrt("localhost.crt");
            context.setCertKey("localhost.key");
        }
        context.setEnableWorkEventLoop(ENABLE_WORK_EVENT_LOOP);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        acceptor.bind();

    }

}
