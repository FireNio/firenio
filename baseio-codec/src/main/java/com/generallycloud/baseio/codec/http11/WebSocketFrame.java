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
package com.generallycloud.baseio.codec.http11;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.AbstractFrame;
import com.generallycloud.baseio.protocol.Frame;

public class WebSocketFrame extends AbstractFrame implements HttpMessage {

    public static final int    OP_CONTINUATION_FRAME     = 0;
    public static final int    OP_TEXT_FRAME             = 1;
    public static final int    OP_BINARY_FRAME           = 2;
    public static final int    OP_CONNECTION_CLOSE_FRAME = 8;
    public static final int    OP_PING_FRAME             = 9;
    public static final int    OP_PONG_FRAME             = 10;
    public static final int    HEADER_LENGTH             = 2;
    public static final String CHANNEL_KEY_SERVICE_NAME  = "CHANNEL_KEY_SERVICE_NAME";

    private byte[]             byteArray;
    private boolean            eof;
    private int                limit;
    private String             readText;
    private String             serviceName;
    private byte               type;

    public WebSocketFrame() {
        this.type = WebSocketCodec.TYPE_TEXT;
    }

    public WebSocketFrame(NioSocketChannel ch,int limit) {
        this.limit = limit;
        this.setServiceName(ch);
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    @Override
    public String getFrameName() {
        return serviceName;
    }

    @Override
    public String getReadText() {
        if (readText == null) {
            if (byteArray == null) {
                return null;
            }
            readText = new String(byteArray, Encoding.UTF8);
        }
        return readText;
    }

    public int getType() {
        return type;
    }

    public boolean isCloseFrame() {
        return OP_CONNECTION_CLOSE_FRAME == type;
    }
    
    public boolean isContinuationFrame(){
        return OP_CONTINUATION_FRAME == type;
    }

    public boolean isEof() {
        return eof;
    }

    @Override
    public boolean read(NioSocketChannel ch, ByteBuf src) throws IOException {
        if (src.remaining() < 2) {
            return false;
        }
        src.markP();
        byte b0 = src.getByte();
        byte b1 = src.getByte();
        int dataLen = 0;
        boolean hasMask = (b1 & 0b10000000) > 0;
        if (hasMask) {
            dataLen += 4;
        }
        int payloadLen = (b1 & 0x7f);
        if (payloadLen < 126) {

        } else if (payloadLen == 126) {
            dataLen += 2;
            if (src.remaining() < dataLen) {
                src.resetP();
                return false;
            }
            payloadLen = src.getUnsignedShort();
        } else {
            dataLen += 8;
            if (src.remaining() < dataLen) {
                src.resetP();
                return false;
            }
            payloadLen = (int) src.getLong();
            if (payloadLen < 0) {
                throw new IOException("over limit:" + payloadLen);
            }
        }
        if (payloadLen > limit) {
            throw new IOException("over limit:" + payloadLen);
        }
        if (src.remaining() < payloadLen) {
            src.resetP();
            return false;
        }
        eof = (b0 & 0b10000000) > 0;
        byte type = (byte) (b0 & 0xF);
        if (type == WebSocketCodec.TYPE_PING) {
            setPing();
        } else if (type == WebSocketCodec.TYPE_PONG) {
            setPong();
        }else{
            this.type = type;
        }
        byte[] array = new byte[payloadLen];
        if (hasMask) {
            byte m0 = src.getByte();
            byte m1 = src.getByte();
            byte m2 = src.getByte();
            byte m3 = src.getByte();
            src.get(array);
            int length = array.length;
            int len = (length / 4) * 4;
            for (int i = 0; i < len;) {
                array[i++] ^= m0;
                array[i++] ^= m1;
                array[i++] ^= m2;
                array[i++] ^= m3;
            }
            if (len < length) {
                int i = len;
                int remain = length - len;
                if (remain == 1) {
                    array[i++] ^= m0;
                }else if(remain == 2) {
                    array[i++] ^= m0;
                    array[i++] ^= m1;
                }else {
                    array[i++] ^= m0;
                    array[i++] ^= m1;
                    array[i++] ^= m2;
                }
            }
        } else {
            src.get(array);
        }
        this.byteArray = array;
        if (type == WebSocketCodec.TYPE_BINARY) {
            // FIXME 处理binary
        }
        return true;
    }

    @Override
    public Frame setPing() {
        this.type = WebSocketCodec.TYPE_PING;
        return super.setPing();
    }

    @Override
    public Frame setPong() {
        this.type = WebSocketCodec.TYPE_PONG;
        return super.setPong();
    }

    protected void setServiceName(NioSocketChannel ch) {
        this.serviceName = (String) ch.getAttribute(CHANNEL_KEY_SERVICE_NAME);
    }

    protected void setWsType(byte type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return getReadText();
    }

    //    @Override
    //    public void release(NioEventLoop eventLoop) {
    //        super.release(eventLoop);
    //        //FIXME ..final statck is null or not null
    //        if (WebSocketCodec.WS_PROTOCOL_CODEC.getFrameStackSize() == 0) {
    //            return;
    //        }
    //        FixedThreadStack<WebSocketFrameImpl> stack = (FixedThreadStack<WebSocketFrameImpl>) eventLoop
    //                .getAttribute(WebSocketCodec.FRAME_STACK_KEY);
    //        if (stack != null) {
    //            stack.push(this);
    //        }
    //    }

    protected WebSocketFrame reset(NioSocketChannel ch, int limit) {
        this.byteArray = null;
        this.eof = false;
        this.readText = null;
        this.type = 0;
        this.limit = limit;
        this.setServiceName(ch);
        super.reset();
        return this;
    }

}
