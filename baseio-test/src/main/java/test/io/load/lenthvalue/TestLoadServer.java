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
package test.io.load.lenthvalue;

import com.firenio.baseio.Options;
import com.firenio.baseio.codec.lengthvalue.LengthValueCodec;
import com.firenio.baseio.component.ChannelAcceptor;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.NioEventLoopGroup;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.concurrent.ThreadEventLoopGroup;

public class TestLoadServer {

    public static final boolean AUTO_EXPANSION         = false;
    public static final int     CLIENT_CORE_SIZE;
    public static final boolean ENABLE_POOL            = true;
    public static final boolean ENABLE_POOL_DIRECT     = true;
    public static final boolean ENABLE_SSL             = false;
    public static final boolean ENABLE_WORK_EVENT_LOOP = false;
    public static final int     MEM_UNIT               = 256;
    public static final int     SERVER_CORE_SIZE;
    public static final int     WRITE_BUFFERS          = 16;
    public static final boolean ENABLE_EPOLL           = true;
    public static final boolean ENABLE_UNSAFE_BUF      = true;

    static {
        SERVER_CORE_SIZE = 8;
        CLIENT_CORE_SIZE = SERVER_CORE_SIZE * 2;
    }

    public static void main(String[] args) throws Exception {
        Options.setBufAutoExpansion(AUTO_EXPANSION);
        Options.setEnableEpoll(ENABLE_EPOLL);
        Options.setEnableUnsafeBuf(ENABLE_UNSAFE_BUF);
        IoEventHandle eventHandle = new IoEventHandle() {
            @Override
            public void accept(Channel ch, Frame f) throws Exception {
                String text = f.getStringContent();
                f.setContent(ch.allocate());
                f.write(text, ch);
                ch.writeAndFlush(f);
            }
        };

        NioEventLoopGroup group = new NioEventLoopGroup(SERVER_CORE_SIZE);
        group.setMemoryPoolCapacity(1024 * 512);
        group.setWriteBuffers(WRITE_BUFFERS);
        group.setMemoryPoolUnit(MEM_UNIT);
        group.setEnableMemoryPool(ENABLE_POOL);
        group.setEnableMemoryPoolDirect(ENABLE_POOL_DIRECT);
        ChannelAcceptor context = new ChannelAcceptor(group, 8300);
        context.setMaxWriteBacklog(Integer.MAX_VALUE);
        context.addProtocolCodec(new LengthValueCodec());
        context.setIoEventHandle(eventHandle);
        if (ENABLE_SSL) {
            context.setEnableSsl(true);
            context.setSslPem("localhost.crt;localhost.key");
        }
        context.addChannelEventListener(new LoggerChannelOpenListener());
        if (ENABLE_WORK_EVENT_LOOP) {
            context.setExecutorEventLoopGroup(
                    new ThreadEventLoopGroup("ep", 1024 * 256 * CLIENT_CORE_SIZE));
        }
        context.bind();

    }

}
