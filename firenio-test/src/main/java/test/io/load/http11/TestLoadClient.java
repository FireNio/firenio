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
package test.io.load.http11;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.firenio.buffer.ByteBuf;
import com.firenio.codec.http11.ClientHttpCodec;
import com.firenio.common.Util;
import com.firenio.component.Channel;
import com.firenio.component.ChannelConnector;
import com.firenio.component.Frame;
import com.firenio.component.IoEventHandle;
import com.firenio.component.NioEventLoopGroup;
import com.firenio.log.DebugUtil;
import com.firenio.log.LoggerFactory;

/**
 * @author wangkai
 */
public class TestLoadClient {

    public static void main(String[] args) throws Exception {
        LoggerFactory.setEnableSLF4JLogger(false);

        String request = "GET /plaintext HTTP/1.1\r\n" + "Host: localhost:8080\r\n" + "Connection: keep-alive\r\n" + "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36\r\n" + "Accept: text/plain,text/html;q=0.9,application/xhtml+xml;q=0.9,application/xml;q=0.8,*/*;q=0.7\r\n\r\n";

        //        request = "GET /plaintext HTTP/1.1\r\n" + "Host: localhost:8080\r\n" + "Connection: keep-alive\r\n" + "User-Agent: ApacheBench/2.3\r\n" + "Accept: */*\r\n\r\n";

        TestLoadRound round = new TestLoadRound();
        round.host = "127.0.0.1";
        round.port = 8080;
        round.pipes = 16;
        round.threads = 4;
        round.requests = 1024 * 1024 * 1;
        round.connections = 1024 * 8;
        round.request_buf = buildRequest(request, round.pipes);

        test(round);

    }

    private static ByteBuf buildRequest(String request, int pipes) {
        byte[]  bytes = request.getBytes();
        ByteBuf buf   = ByteBuf.direct(bytes.length * pipes);
        for (int i = 0; i < pipes; i++) {
            buf.writeBytes(bytes);
        }
        return buf;
    }

    public static void test(TestLoadRound round) throws Exception {
        final String  host        = round.host;
        final ByteBuf buf         = round.request_buf;
        final int     port        = round.port;
        final int     pipes       = round.pipes;
        final int     threads     = round.threads;
        final int     requests    = round.requests;
        final int     connections = round.connections;
        final int     batch       = requests / pipes;
        DebugUtil.info("requests:" + requests);
        DebugUtil.info("threads:" + threads);
        DebugUtil.info("connections:" + connections);
        DebugUtil.info("pipes:" + pipes);
        DebugUtil.info("batch:" + batch);
        NioEventLoopGroup g = new NioEventLoopGroup(true, threads, Integer.MAX_VALUE);
        g.start();
        Channel[] chs = new Channel[connections];
        DebugUtil.info("build connections...");
        CountDownLatch c_latch    = new CountDownLatch(connections);
        CountDownLatch b_latch    = new CountDownLatch(batch);
        AtomicInteger  c_complete = new AtomicInteger();
        long           last       = Util.now();
        for (int i = 0; i < connections; i++) {
            ChannelConnector context = new ChannelConnector(g.getNext(), host, port);
            context.setPrintConfig(false);
            context.addProtocolCodec(new ClientHttpCodec());
            context.setIoEventHandle(new IoEventHandle() {

                int c = 0;
                int b = batch / connections;

                @Override
                public void accept(Channel ch, Frame frame) {
                    if (++c == pipes) {
                        c = 0;
                        b_latch.countDown();
                        if (--b > 0) {
                            ch.writeAndFlush(buf.duplicate());
                        } else {
                            DebugUtil.info("c complate......" + c_complete.incrementAndGet());
                        }
                    }
                }
            });
            final int i_copy = i;
            context.connect((ch, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                }
                chs[i_copy] = ch;
                c_latch.countDown();
            }, 9000);
        }
        c_latch.await();
        DebugUtil.info("build connections cost:" + (Util.now() - last));

        DebugUtil.info("start request...");
        last = Util.now();
        for (int i = 0; i < chs.length; i++) {
            chs[i].writeAndFlush(buf.duplicate());
        }
        b_latch.await();
        long cost = (Util.now() - last);
        DebugUtil.info("request cost:" + cost);
        DebugUtil.info("request rps:" + (requests * 1000d / cost));
        for (int i = 0; i < chs.length; i++) {
            chs[i].close();
        }
        g.stop();
    }

    static class TestLoadRound {

        String  host;
        ByteBuf request_buf;
        int     port;
        int     threads;
        int     pipes;
        int     requests;
        int     connections;

    }

}
