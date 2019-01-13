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
package test.io.load.http11;

import java.io.IOException;
import java.util.Arrays;

import com.firenio.baseio.Options;
import com.firenio.baseio.codec.http11.HttpCodec;
import com.firenio.baseio.codec.http11.HttpCodecLite;
import com.firenio.baseio.codec.http11.HttpFrameLite;
import com.firenio.baseio.codec.http11.HttpStatic;
import com.firenio.baseio.codec.http11.HttpStatus;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.ChannelAcceptor;
import com.firenio.baseio.component.ChannelAliveListener;
import com.firenio.baseio.component.ChannelEventListenerAdapter;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.NioEventLoopGroup;
import com.firenio.baseio.component.ProtocolCodec;
import com.firenio.baseio.component.SocketOptions;
import com.firenio.baseio.log.DebugUtil;
import com.firenio.baseio.log.LoggerFactory;
import com.jsoniter.output.JsonStream;
import com.jsoniter.output.JsonStreamPool;
import com.jsoniter.spi.JsonException;

/**
 * @author wangkai
 *
 */
public class TestHttpLoadServerTFB {

    static final byte[] STATIC_PLAINTEXT = "Hello, World!".getBytes();

    static class Message {

        private final String message;

        public Message(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

    }

    public static void main(String[] args) throws Exception {
        boolean lite = Util.getBooleanProperty("lite");
        boolean read = Util.getBooleanProperty("read");
        boolean pool = true;
        boolean direct = true;
        int core = Util.getIntProperty("core", 1);
        int frame = Util.getIntProperty("frame", 16);
        int level = Util.getIntProperty("level", 1);
        int readBuf = Util.getIntProperty("readBuf", 16);
        LoggerFactory.setEnableSLF4JLogger(false);
        LoggerFactory.setLogLevel(LoggerFactory.LEVEL_INFO);
        Options.setDebugErrorLevel(level);
        Options.setChannelReadFirst(read);
        Options.setBufAutoExpansion(false);
        Options.setEnableEpoll(true);
        Options.setEnableUnsafeBuf(true);
        DebugUtil.info("lite: {}", lite);
        DebugUtil.info("read: {}", read);
        DebugUtil.info("pool: {}", pool);
        DebugUtil.info("core: {}", core);
        DebugUtil.info("frame: {}", frame);
        DebugUtil.info("level: {}", level);
        DebugUtil.info("direct: {}", direct);
        DebugUtil.info("readBuf: {}", readBuf);

        IoEventHandle eventHandle = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                HttpFrameLite f = (HttpFrameLite) frame;
                String action = f.getRequestURL();
                if ("/plaintext".equals(action)) {
                    f.setContent(STATIC_PLAINTEXT);
                    f.setContentType(HttpStatic.text_plain_bytes);
                } else if ("/json".equals(action)) {
                    f.setContent(serializeMsg(new Message("Hello, World!")));
                    f.setContentType(HttpStatic.application_json_bytes);
                } else {
                    System.err.println("404");
                    f.setContent("404,page not found!".getBytes());
                    f.setStatus(HttpStatus.C404);
                }
                ch.writeAndFlush(f);
                ch.release(f);
//                ch.close();
            }

        };

        ProtocolCodec codec;
        if (lite) {
            HttpCodecLite c = new HttpCodecLite("baseio", 1024 * 16);
            codec = c;
        } else {
            codec = new HttpCodec("baseio", 1024 * 16);
        }
        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setMemoryPoolCapacity(1024 * 128);
        group.setMemoryPoolUnit(256);
        group.setWriteBuffers(32);
        group.setEventLoopSize(Util.availableProcessors() * core);
//        group.setEventLoopSize(1);
        group.setConcurrentFrameStack(false);
        ChannelAcceptor context = new ChannelAcceptor(group, 8080);
        context.addProtocolCodec(codec);
//        context.addChannelIdleEventListener(new ChannelAliveListener());
//        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelEventListener(new ChannelEventListenerAdapter(){
            
            @Override
            public void channelOpened(Channel ch) throws Exception {
                ch.setOption(SocketOptions.TCP_NODELAY, 1);
                ch.setOption(SocketOptions.TCP_QUICKACK, 1);
                ch.setOption(SocketOptions.SO_KEEPALIVE, 0);
            }
        });
        context.setIoEventHandle(eventHandle);
        context.bind();
    }

    private static byte[] serializeMsg(Message obj) {
        JsonStream stream = JsonStreamPool.borrowJsonStream();
        try {
            stream.reset(null);
            stream.writeVal(Message.class, obj);
            return Arrays.copyOfRange(stream.buffer().data(), 0, stream.buffer().tail());
        } catch (IOException e) {
            throw new JsonException(e);
        } finally {
            JsonStreamPool.returnJsonStream(stream);
        }
    }

}
