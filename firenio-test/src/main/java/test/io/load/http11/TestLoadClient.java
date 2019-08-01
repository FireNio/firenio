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

        request = "GET /plaintext HTTP/1.1\r\n" + "Host: localhost:8080\r\n" + "Connection: keep-alive\r\n" + "User-Agent: ApacheBench/2.3\r\n" + "Accept: */*\r\n\r\n";
        final String  host = "127.0.0.1";
        final int     port = 8080;
        final int     rpc  = 1024 * 4 * 16;
        final int     ts   = 2;
        final int     cpt  = 32;
        final int     pipe = 64;
        final ByteBuf req  = buildReqest(request, pipe);

        test(host, port, req, rpc, ts, cpt, pipe);

    }

    private static ByteBuf buildReqest(String request, int pipe) {
        byte[]  bytes = request.getBytes();
        ByteBuf buf   = ByteBuf.direct(bytes.length * pipe);
        for (int i = 0; i < pipe; i++) {
            buf.writeBytes(bytes);
        }
        return buf;
    }

    public static void test(final String host, final int port, final ByteBuf req, final int rpc, final int ts, final int cpt, final int pipe) throws Exception {
        final int cs    = cpt * ts;
        final int count = rpc * cs;
        final int batch = count / pipe;
        DebugUtil.info("ts:" + ts);
        DebugUtil.info("cs:" + cs);
        DebugUtil.info("count:" + count);
        DebugUtil.info("pipe:" + pipe);
        DebugUtil.info("batch:" + batch);
        NioEventLoopGroup g = new NioEventLoopGroup(true, ts, Integer.MAX_VALUE);
        g.start();
        Channel[] chs = new Channel[cs];
        DebugUtil.info("build connections...");
        long           last    = Util.now();
        CountDownLatch c_latch = new CountDownLatch(cs);
        CountDownLatch b_latch = new CountDownLatch(batch);
        for (int i = 0; i < cs; i++) {
            ChannelConnector context = new ChannelConnector(g.getNext(), host, port);
            context.setPrintConfig(false);
            context.addProtocolCodec(new ClientHttpCodec());
            context.setIoEventHandle(new IoEventHandle() {

                int c = 0;
                int b = batch / cs;

                @Override
                public void accept(Channel ch, Frame frame) throws Exception {
                    if (++c == pipe) {
                        c = 0;
                        b_latch.countDown();
                        if (--b == 0) {
                            //                            DebugUtil.info("c complate......");
                        } else {
                            ch.writeAndFlush(req.duplicate());
                        }
                    }
                }
            });
            final int ii = i;
            //            context.addChannelEventListener(new LoggerChannelOpenListener());
            context.connect((ch, ex) -> {
                chs[ii] = ch;
                c_latch.countDown();
            });
        }
        c_latch.await();
        DebugUtil.info("build connections cost:" + (Util.now() - last));

        DebugUtil.info("start request...");
        last = Util.now();
        for (int i = 0; i < chs.length; i++) {
            chs[i].writeAndFlush(req.duplicate());
        }
        b_latch.await();
        long cost = (Util.now() - last);
        DebugUtil.info("request cost:" + cost);
        DebugUtil.info("request rps:" + (count * 1000d / cost));
        for (int i = 0; i < chs.length; i++) {
            chs[i].close();
        }
        g.stop();
    }

}
