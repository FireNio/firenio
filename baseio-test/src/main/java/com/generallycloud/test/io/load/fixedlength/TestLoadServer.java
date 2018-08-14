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

import java.util.ArrayList;
import java.util.List;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.ChannelEventListenerAdapter;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.TextFuture;

public class TestLoadServer {

    public static void main(String[] args) throws Exception {

        final boolean batchFlush = true;

        NioEventLoopGroup group = new NioEventLoopGroup(8);
        group.setMemoryPoolCapacity(1024 * 512);
        group.setBufRecycleSize(1024 * 64);
        group.setMemoryPoolUnit(256);
//        group.setEnableMemoryPool(false);
//        group.setEnableMemoryPoolDirect(false);
        ChannelContext context = new ChannelContext(8300);
        ChannelAcceptor acceptor = new ChannelAcceptor(context, group);
        context.setMaxWriteBacklog(Integer.MAX_VALUE);
        context.setProtocolCodec(new FixedLengthCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelEventListener(new ChannelEventListenerAdapter() {

            @Override
            public void channelOpened(NioSocketChannel channel) throws Exception {
//                channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
                channel.setIoEventHandle(new IoEventHandle() {
                    boolean      addTask = true;
                    List<ByteBuf> fs      = new ArrayList<>(1024 * 4);
                    @Override
                    public void accept(NioSocketChannel channel, Future future) throws Exception {
                        TextFuture f = (TextFuture) future;
                        f.write(f.getReadText(), channel);
                        if (batchFlush) {
                            fs.add(channel.getCodec().encode(channel, future));
                            if (addTask) {
                                addTask = false;
                                channel.getEventLoop().dispatchAfterLoop(() -> {
                                    channel.flush(fs);
                                    addTask = true;
                                    fs.clear();
                                });
                            }
                        } else {
                            channel.flush(future);
                        }
                    }
                });
            }
        });
        acceptor.bind();
    }

}
