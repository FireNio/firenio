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
package com.generallycloud.test.io.balance;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.ProtobaseFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.protocol.Future;

public class TestBalanceClient {

    public static void main(String[] args) throws Exception {

        final AtomicInteger res = new AtomicInteger();

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(NioSocketChannel channel, Future future) throws Exception {
                ProtobaseFuture f = (ProtobaseFuture) future;
                if (f.hasReadBinary()) {
                    System.out.println(f.getReadText() + new String(f.getReadBinary()) + "______R:"
                            + System.currentTimeMillis());
                } else {
                    System.out.println(f.getReadText() + "______R:" + System.currentTimeMillis());
                }
                res.incrementAndGet();
            }
        };

        Configuration configuration = new Configuration(8600);
        ChannelContext context = new ChannelContext(configuration);
        ChannelConnector connector = new ChannelConnector(context);
        context.setProtocolCodec(new ProtobaseCodec());
        context.addChannelEventListener(new LoggerSocketSEListener());
        context.setIoEventHandle(eventHandleAdaptor);
        NioSocketChannel channel = (NioSocketChannel) connector.connect();

        for (int i = 0; i < 100; i++) {
            int fid = Math.abs(new Random().nextInt());
            ProtobaseFuture future = new ProtobaseFutureImpl("future-name");
            future.write("你好！", channel);
            future.setHashCode(fid);
            channel.flush(future);
        }

        ThreadUtil.sleep(300);
        System.out.println("==========" + res.get());
        ThreadUtil.sleep(500000000);
        CloseUtil.close(connector);
    }

}
