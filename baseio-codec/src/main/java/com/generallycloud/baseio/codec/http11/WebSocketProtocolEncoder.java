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
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.codec.http11.future.WebSocketFuture;
import com.generallycloud.baseio.common.MathUtil;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolEncoder;

//WebSocket规定服务端不准向客户端发送mask过的数据
//A server MUST NOT mask any frames that it sends to the client.
public class WebSocketProtocolEncoder implements ProtocolEncoder {

    final int MAX_UNSIGNED_SHORT = (1 << 16) - 1;

    @Override
    public void encode(ByteBufAllocator allocator, ChannelFuture future) throws IOException {

        WebSocketFuture f = (WebSocketFuture) future;

        ByteArrayBuffer buffer = future.getWriteBuffer();

        byte[] header;

        byte[] data = buffer.array();

        int size = buffer.size();

        byte header0 = (byte) (0x8f & (f.getType() | 0xf0));

        if (size < 126) {
            header = new byte[2];
            header[0] = header0;
            header[1] = (byte) size;
        } else if (size <= MAX_UNSIGNED_SHORT) {
            header = new byte[4];
            header[0] = header0;
            header[1] = 126;
            MathUtil.unsignedShort2Byte(header, size, 2);
        } else {
            header = new byte[6];
            header[0] = header0;
            header[1] = 127;
            MathUtil.int2Byte(header, size, 2);
        }

        ByteBuf buf = allocator.allocate(header.length + size);

        buf.put(header);

        buf.put(data, 0, size);

        future.setByteBuf(buf.flip());
    }

    //	public IOWriteFuture encodeWithMask(BaseContext context, IOReadFuture readFuture) throws IOException {
    //		
    //		WebSocketReadFuture future = (WebSocketReadFuture) readFuture;
    //
    //		BufferedOutputStream o = future.getWriteBuffer();
    //
    //		byte [] header;
    //		
    //		int size = o.size();
    //		
    //		byte header0 = (byte) (0x8f & (future.getType() | 0xf0));
    //		
    //		if (size < 126) {
    //			header = new byte[2];
    //			header[0] = header0;
    //			header[1] = (byte)(size | 0x80);
    //		}else if(size < ((1 << 16) -1)){
    //			header = new byte[4];
    //			header[0] = header0;
    //			header[1] = (byte) (126 | 0xff);
    //			header[3] = (byte)(size & 0xff);
    //			header[2] = (byte)((size >> 8) & 0x80);
    //		}else{
    //			header = new byte[6];
    //			header[0] = header0;
    //			header[1] = (byte) (127 | 0x80);
    //			MathUtil.int2Byte(header, size, 2);
    //		}
    //		
    //		ByteBuf buffer = context.getHeapByteBufferPool().allocate(header.length + size + 4);
    //		
    //		buffer.put(header);
    //		
    //		byte [] array = o.array();
    //		
    //		byte [] mask = MathUtil.int2Byte(size);
    //		
    //		for (int i = 0; i < size; i++) {
    //			
    //			array[i] = (byte)(array[i] ^ mask[i % 4]);
    //		}
    //		
    //		buffer.put(mask);
    //		
    //		buffer.put(array,0,size);
    //		
    //		buffer.flip();
    //
    //		return new IOWriteFutureImpl(readFuture, buffer);
    //	}

}
