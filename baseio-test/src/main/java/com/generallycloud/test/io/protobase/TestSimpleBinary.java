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
package com.generallycloud.test.io.protobase;

import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.ProtobaseFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.protocol.Future;

public class TestSimpleBinary {

    public static void main(String[] args) throws Exception {
        String serviceKey = "/test-simple";
        IoEventHandle eventHandle = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Future future) throws Exception {
                ProtobaseFuture f = (ProtobaseFuture) future;
                System.out.println("________________________" + f.getReadText());
                if (f.getReadBinarySize() > 0) {
                    System.out.println("________________________" + new String(f.getReadBinary()));    
                }
            }
        };
        ChannelContext context = new ChannelContext(new Configuration(8300));
        ChannelConnector connector = new ChannelConnector(context);
        context.setProtocolCodec(new ProtobaseCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setIoEventHandle(eventHandle);
        NioSocketChannel channel = connector.connect();
        ProtobaseFuture f = new ProtobaseFutureImpl(serviceKey);
        f.write("text22222".getBytes());
        f.writeBinary("binary22222".getBytes());
        channel.flush(f);
        ThreadUtil.sleep(500);
        CloseUtil.close(connector);
    }

}
