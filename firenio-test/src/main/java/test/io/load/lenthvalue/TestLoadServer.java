/*
 * Copyright 2015 The FireNio Project
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

import com.firenio.Options;
import com.firenio.buffer.ByteBuf;
import com.firenio.codec.lengthvalue.LengthValueCodec;
import com.firenio.component.ChannelAcceptor;
import com.firenio.component.Frame;
import com.firenio.component.IoEventHandle;
import com.firenio.component.LoggerChannelOpenListener;
import com.firenio.component.NioEventLoopGroup;
import com.firenio.component.Channel;
import com.firenio.concurrent.ThreadEventLoopGroup;

public class TestLoadServer {

    public static final boolean AUTO_EXPANSION         = true;
    public static final int     CLIENT_CORE_SIZE;
    public static final boolean ENABLE_POOL            = true;
    public static final boolean ENABLE_SSL             = false;
    public static final boolean ENABLE_WORK_EVENT_LOOP = false;
    public static final int     MEM_UNIT               = 1024;
    public static final int     SERVER_CORE_SIZE;
    public static final int     WRITE_BUFFERS          = 32;
    public static final boolean ENABLE_EPOLL           = true;
    public static final boolean ENABLE_UNSAFE_BUF      = false;
    public static final boolean BUFFERED_WRITE         = true;

    static final String WRITE_BUF = "write_buf";

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
                if (BUFFERED_WRITE) {

                    ByteBuf buf = ch.getAttribute(WRITE_BUF);
                    if (buf == null) {
                        buf = ch.allocate();
                        ByteBuf temp = buf;
                        ch.setAttribute(WRITE_BUF, buf);
                        ch.getEventLoop().submit(() -> {
                            ch.writeAndFlush(temp);
                            ch.setAttribute(WRITE_BUF, null);
                        });
                    }
                    byte[] data = text.getBytes(ch.getCharset());
                    buf.writeInt(data.length);
                    buf.writeBytes(data);
                } else {
                    f.setString(text, ch);
                    ch.writeAndFlush(f);
                }
            }
        };

        NioEventLoopGroup group = new NioEventLoopGroup(SERVER_CORE_SIZE);
        group.setMemoryCapacity(1024 * 512 * MEM_UNIT * SERVER_CORE_SIZE);
        group.setWriteBuffers(WRITE_BUFFERS);
        group.setMemoryUnit(MEM_UNIT);
        group.setEnableMemoryPool(ENABLE_POOL);
        ChannelAcceptor context = new ChannelAcceptor(group, 8300);
        context.addProtocolCodec(new LengthValueCodec());
        context.setIoEventHandle(eventHandle);
        if (ENABLE_SSL) {
//            context.setSslPem("localhost.key;localhost.crt");
        }
        context.addChannelEventListener(new LoggerChannelOpenListener());
        if (ENABLE_WORK_EVENT_LOOP) {
            context.setExecutorGroup(new ThreadEventLoopGroup("ep", 1024 * 256 * CLIENT_CORE_SIZE));
        }
        context.bind();

    }

}
