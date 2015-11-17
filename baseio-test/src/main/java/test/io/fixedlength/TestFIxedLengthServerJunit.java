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
package test.io.fixedlength;

import java.net.StandardSocketOptions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.firenio.baseio.codec.fixedlength.FixedLengthCodec;
import com.firenio.baseio.codec.fixedlength.FixedLengthFrame;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelAcceptor;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.ChannelEventListenerAdapter;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.concurrent.Waiter;

import junit.framework.Assert;

public class TestFIxedLengthServerJunit {

    static final String hello   = "hello server!";
    static final String res     = "yes server already accept your message:";

    ChannelAcceptor     context = new ChannelAcceptor(8300);

    @Before
    public void server() throws Exception {
        IoEventHandle eventHandle = new IoEventHandle() {
            @Override
            public void accept(Channel ch, Frame f) throws Exception {
                String text = f.getStringContent();
                f.setContent(ch.allocate());
                f.write("yes server already accept your message:", ch);
                f.write(text, ch);
                ch.writeAndFlush(f);
            }
        };
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setIoEventHandle(eventHandle);
        context.addProtocolCodec(new FixedLengthCodec());
        context.addChannelEventListener(new ChannelEventListenerAdapter() {

            @Override
            public void channelOpened(Channel ch) throws Exception {
                ch.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
                ch.setOption(StandardSocketOptions.TCP_NODELAY, true);
            }

        });
        context.bind();
    }

    public void testClient() throws Exception {
        Waiter<String> w = new Waiter<>();
        ChannelConnector context = new ChannelConnector(8300);
        IoEventHandle eventHandle = new IoEventHandle() {
            @Override
            public void accept(Channel ch, Frame f) throws Exception {
                System.out.println();
                System.out.println("____________________" + f.getStringContent());
                System.out.println();
                context.close();
                w.call(f.getStringContent(), null);
            }
        };

        context.setIoEventHandle(eventHandle);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new FixedLengthCodec());
        Channel ch = context.connect();
        FixedLengthFrame frame = new FixedLengthFrame();
        frame.setContent(ch.allocate());
        frame.write(hello, ch);
        ch.writeAndFlush(frame);
        w.await(1000);
        v(w.getResponse());
    }

    @Test
    public void test() throws Exception {
        testClient();
        testClientAsync();
    }

    public void testClientAsync() throws Exception {
        Waiter<String> w = new Waiter<>();
        ChannelConnector context = new ChannelConnector(8300);
        IoEventHandle eventHandle = new IoEventHandle() {
            @Override
            public void accept(Channel ch, Frame f) throws Exception {
                System.out.println();
                System.out.println("____________________" + f.getStringContent());
                System.out.println();
                context.close();
                w.call(f.getStringContent(), null);
            }
        };

        context.setIoEventHandle(eventHandle);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new FixedLengthCodec());
        context.connect((ch, ex) -> {
            FixedLengthFrame frame = new FixedLengthFrame();
            frame.setContent(ch.allocate());
            frame.write(hello, ch);
            try {
                ch.writeAndFlush(frame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        w.await(1000);
        v(w.getResponse());
    }

    static void v(String r) {
        Assert.assertTrue(r.equals(res + hello));
    }

    @After
    public void clean() {
        Util.unbind(context);
    }

}
