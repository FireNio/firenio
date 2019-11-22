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

import java.util.concurrent.atomic.AtomicInteger;

import test.test.ITestThread;
import test.test.ITestThreadHandle;

import com.firenio.Options;
import com.firenio.codec.lengthvalue.LengthValueCodec;
import com.firenio.codec.lengthvalue.LengthValueFrame;
import com.firenio.common.ByteUtil;
import com.firenio.common.Util;
import com.firenio.component.Channel;
import com.firenio.component.ChannelConnector;
import com.firenio.component.Frame;
import com.firenio.component.IoEventHandle;
import com.firenio.component.NioEventLoopGroup;
import com.firenio.component.SslContextBuilder;
import com.firenio.concurrent.ThreadEventLoopGroup;

import static test.io.load.lenthvalue.TestLoadServer.CLIENT_CORE_SIZE;

public class TestLoadClient1 extends ITestThread {

    public static final  boolean debug   = false;
    static final         Object  lock    = new Object();
    private static final byte[]  req;
    static               boolean running = true;

    static {
        int           len = debug ? 10 : 1;
        StringBuilder s   = new StringBuilder();
        for (int i = 0; i < len; i++) {
            s.append("abcdefghij");
        }
        req = s.toString().getBytes();
    }

    final   AtomicInteger    count   = new AtomicInteger();
    private ChannelConnector context = null;

    public static void main(String[] args) {
        Options.setBufAutoExpansion(TestLoadServer.AUTO_EXPANSION);
        Options.setEnableEpoll(TestLoadServer.ENABLE_EPOLL);
        Options.setEnableUnsafeBuf(TestLoadServer.ENABLE_UNSAFE_BUF);
        if (args != null && args.length == 999) {
            running = true;
            Util.exec(() -> {
                Util.sleep(7000);
                for (; running; ) {
                    synchronized (lock) {
                        Util.wait(lock, 3000);
                    }
                    for (ITestThread tt : ITestThreadHandle.ts) {
                        TestLoadClient1 t = (TestLoadClient1) tt;
                        if (t.count.get() > 0) {
                            System.out.println("count:" + t.count.get() + "ch:" + t.context.getChannel());
                        }
                    }
                }
            });
        }

        int time     = 1024 * 1024 * 4;
        int execTime = 15;
        ITestThreadHandle.doTest(TestLoadClient1.class, CLIENT_CORE_SIZE, time, execTime);
    }

    @Override
    public void prepare() throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) {
                addCount(80000);
                if (debug) {
                    count.decrementAndGet();
                }
            }
        };

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setMemoryCapacity(5120000 * 256 * CLIENT_CORE_SIZE);
        group.setMemoryUnit(256);
        group.setWriteBuffers(TestLoadServer.WRITE_BUFFERS);
        group.setEnableMemoryPool(TestLoadServer.ENABLE_POOL);
        context = new ChannelConnector(group, "127.0.0.1", 8300);
        context.setIoEventHandle(eventHandleAdaptor);
        if (TestLoadServer.ENABLE_SSL) {
            context.setSslContext(SslContextBuilder.forClient(true).build());
        }
        context.setPrintConfig(false);
        context.addProtocolCodec(new LengthValueCodec());
        if (TestLoadServer.ENABLE_WORK_EVENT_LOOP) {
            context.setExecutorGroup(new ThreadEventLoopGroup("ep", 1024 * 256));
        }
        context.connect(6000);
    }

    @Override
    public void run() {
        int     time1 = getTime();
        Channel ch    = context.getChannel();
        try {
            for (int i = 0; i < time1; i++) {
                Frame frame = new LengthValueFrame();
                frame.setContent(ch.allocateWithSkipHeader(1));
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
}
