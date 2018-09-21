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

import static com.generallycloud.test.io.load.fixedlength.TestLoadServer.CLIENT_CORE_SIZE;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFrame;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.MathUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.SslContextBuilder;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.test.test.ITestThread;
import com.generallycloud.test.test.ITestThreadHandle;

public class TestLoadClient1 extends ITestThread {

    public static final boolean debug   = false;
    static final Object         lock    = new Object();
    static boolean              running = true;
    final AtomicInteger         count   = new AtomicInteger();

    private static final byte[] req;

    static {
        int len = debug ? 10 : 1;
        String s = "";
        for (int i = 0; i < len; i++) {
            s += "abcdefghij";
        }
        req = s.getBytes();
    }

    private ChannelConnector connector = null;

    @Override
    public void run() {
        int time1 = getTime();
        NioSocketChannel channel = connector.getChannel();
        for (int i = 0; i < time1; i++) {
            Frame frame = new FixedLengthFrame();
            if (debug) {
                byte[] bb = new byte[4];
                MathUtil.int2Byte(bb, i, 0);
                frame.write(bb);
            }
            frame.write(req);
            if (debug) {
                frame.write(String.valueOf(i).getBytes());
            }
            channel.flush(frame);
            if (debug) {
                count.incrementAndGet();
            }
        }
    }

    @Override
    public void prepare() throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                addCount(40000);
                if (debug) {
                    count.decrementAndGet();
                }
            }
        };

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setMemoryPoolCapacity(5120000 / CLIENT_CORE_SIZE);
        group.setMemoryPoolUnit(TestLoadServer.MEM_UNIT);
        group.setWriteBuffers(TestLoadServer.WRITE_BUFFERS);
        group.setEnableMemoryPool(TestLoadServer.ENABLE_POOL);
        group.setEnableMemoryPoolDirect(TestLoadServer.ENABLE_POOL_DIRECT);
        ChannelContext context = new ChannelContext(8300);
        connector = new ChannelConnector(context, group);
        context.setMaxWriteBacklog(Integer.MAX_VALUE);
        context.setIoEventHandle(eventHandleAdaptor);
        if (TestLoadServer.ENABLE_SSL) {
            context.setSslContext(SslContextBuilder.forClient(true).build());
        }
        context.setEnableWorkEventLoop(TestLoadServer.ENABLE_WORK_EVENT_LOOP);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setProtocolCodec(new FixedLengthCodec());
        connector.connect();
    }

    @Override
    public void stop() {
        CloseUtil.close(connector);
        running = false;
        synchronized (lock) {
            lock.notify();
        }
    }

    public static void main(String[] args) throws IOException {

        if (args != null && args.length == 999) {
            running = true;
            ThreadUtil.exec(() -> {
                ThreadUtil.sleep(7000);
                for (; running;) {
                    ThreadUtil.wait(lock, 3000);
                    for (ITestThread tt : ITestThreadHandle.ts) {
                        TestLoadClient1 t = (TestLoadClient1) tt;
                        if (t.count.get() > 0) {
                            System.out.println(
                                    "count:" + t.count.get() + "ch:" + t.connector.getChannel());
                        }
                    }
                }
            });
        }

        int time = 1024 * 1024 * 4;
        int threads = CLIENT_CORE_SIZE;
        int execTime = 19;
        ITestThreadHandle.doTest(TestLoadClient1.class, threads, time, execTime);
    }
}
