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

import static test.io.load.lenthvalue.TestLoadServer.CLIENT_CORE_SIZE;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.firenio.baseio.Options;
import com.firenio.baseio.codec.lengthvalue.LengthValueCodec;
import com.firenio.baseio.codec.lengthvalue.LengthValueFrame;
import com.firenio.baseio.common.ByteUtil;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.NioEventLoopGroup;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.SslContextBuilder;
import com.firenio.baseio.concurrent.ThreadEventLoopGroup;

import test.test.ITestThread;
import test.test.ITestThreadHandle;

public class TestLoadClient1 extends ITestThread {

    public static final boolean debug   = false;
    static final Object         lock    = new Object();
    private static final byte[] req;
    static boolean              running = true;

    static {
        int len = debug ? 10 : 1;
        String s = "";
        for (int i = 0; i < len; i++) {
            s += "abcdefghij";
        }
        req = s.getBytes();
    }

    private ChannelConnector context = null;

    final AtomicInteger      count   = new AtomicInteger();

    @Override
    public void prepare() throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                addCount(80000);
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
        context = new ChannelConnector(group, "127.0.0.1", 8300);
        context.setMaxWriteBacklog(Integer.MAX_VALUE);
        context.setIoEventHandle(eventHandleAdaptor);
        if (TestLoadServer.ENABLE_SSL) {
            context.setSslContext(SslContextBuilder.forClient(true).build());
        }
        context.setPrintConfig(false);
        context.addProtocolCodec(new LengthValueCodec());
        if (TestLoadServer.ENABLE_WORK_EVENT_LOOP) {
            context.setExecutorEventLoopGroup(new ThreadEventLoopGroup("ep", 1024 * 256));
        }
        context.connect(6000);
    }

    @Override
    public void run() {
        int time1 = getTime();
        Channel ch = context.getChannel();
        try {
            for (int i = 0; i < time1; i++) {
                Frame frame = new LengthValueFrame();
                frame.setContent(ch.allocate());
                if (debug) {
                    byte[] bb = new byte[4];
                    ByteUtil.putInt(bb, i, 0);
                    frame.write(bb);
                }
                frame.write(req);
                if (debug) {
                    frame.write(String.valueOf(i).getBytes());
                }
                ch.writeAndFlush(frame);
                if (debug) {
                    count.incrementAndGet();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        Util.close(context);
        running = false;
        synchronized (lock) {
            lock.notify();
        }
    }

    public static void main(String[] args) throws IOException {
        Options.setBufAutoExpansion(TestLoadServer.AUTO_EXPANSION);
        Options.setEnableEpoll(TestLoadServer.ENABLE_EPOLL);
        Options.setEnableUnsafeBuf(TestLoadServer.ENABLE_UNSAFE_BUF);
        if (args != null && args.length == 999) {
            running = true;
            Util.exec(() -> {
                Util.sleep(7000);
                for (; running;) {
                    Util.wait(lock, 3000);
                    for (ITestThread tt : ITestThreadHandle.ts) {
                        TestLoadClient1 t = (TestLoadClient1) tt;
                        if (t.count.get() > 0) {
                            System.out.println(
                                    "count:" + t.count.get() + "ch:" + t.context.getChannel());
                        }
                    }
                }
            });
        }

        int time = 1024 * 1024 * 4;
        int threads = CLIENT_CORE_SIZE;
        int execTime = 15;
        ITestThreadHandle.doTest(TestLoadClient1.class, threads, time, execTime);
    }
}
