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

import java.io.IOException;
import java.net.StandardSocketOptions;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.ChannelEventListenerAdapter;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.test.test.ITestThread;
import com.generallycloud.test.test.ITestThreadHandle;

public class TestLoadClient1 extends ITestThread {
    
    static final int core_size = 16;
    
    private static final byte [] req;
    
    static {
        int len = 1;
        String s = "hello server!";
        for (int i = 0; i < len; i++) {
            s += "hello server!";
        }
        req = s.getBytes();
    }

    private ChannelConnector connector = null;

    @Override
    public void run() {
        int time1 = getTime();
        NioSocketChannel channel = connector.getChannel();
        for (int i = 0; i < time1; i++) {
            Future future = new FixedLengthFuture();
            future.write(req);
            channel.flush(future);
        }
    }

    @Override
    public void prepare() throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {
            
            @Override
            public void accept(NioSocketChannel channel, Future future) throws Exception {
                addCount(40000);
            }
        };

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setMemoryPoolCapacity(5120000 / core_size);
        group.setMemoryPoolUnit(512);
        group.setBufRecycleSize(1024 * 8);
//        group.setEnableMemoryPool(false);
//        group.setEnableMemoryPoolDirect(false);
        ChannelContext context = new ChannelContext(8300);
        connector = new ChannelConnector(context, group);
        context.setMaxWriteBacklog(Integer.MAX_VALUE);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelEventListener(new ChannelEventListenerAdapter(){
            @Override
            public void channelOpened(NioSocketChannel channel) throws Exception {
//                channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            }
        });
        context.setProtocolCodec(new FixedLengthCodec());
        connector.connect();
    }

    @Override
    public void stop() {
        CloseUtil.close(connector);
    }

    public static void main(String[] args) throws IOException {

        int time = 1024 * 1024 * 8;

        ITestThreadHandle.doTest(TestLoadClient1.class, core_size, time / core_size);
    }
}
