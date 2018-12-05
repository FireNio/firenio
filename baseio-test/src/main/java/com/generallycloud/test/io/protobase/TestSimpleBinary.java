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
package com.generallycloud.test.io.protobase;

import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ProtobaseFrame;
import com.generallycloud.baseio.common.Util;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;

public class TestSimpleBinary {

    public static void main(String[] args) throws Exception {
        IoEventHandle eventHandle = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                ProtobaseFrame f = (ProtobaseFrame) frame;
                System.out.println("________________________" + f.getReadText());
                if (f.getReadBinarySize() > 0) {
                    System.out.println("________________________" + new String(f.getReadBinary()));    
                }
            }
        };
        ChannelConnector context = new ChannelConnector(8300);
        context.setProtocolCodec(new ProtobaseCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setIoEventHandle(eventHandle);
        NioSocketChannel channel = context.connect();
        ProtobaseFrame f = new ProtobaseFrame();
        f.write("text22222".getBytes());
        f.writeBinary("binary22222".getBytes());
        channel.flush(f);
        Util.sleep(500);
        Util.close(context);
    }

}
