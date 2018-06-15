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

public class TestBalanceBroadcast {

    public static void main(String[] args) throws Exception {

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(NioSocketChannel channel, Future future) throws Exception {
                ProtobaseFuture f = (ProtobaseFuture) future;
                if ("XXX".equals(f.getFutureName())) {
                    System.out.println("客户端已下线：" + f.getReadText());
                } else {
                    System.out.println("~~~~~~收到报文：" + future.toString());
                    String res = "(***" + f.getReadText() + "***)";
                    System.out.println("~~~~~~处理报文：" + res);
                    f.write(res, channel.getContext());
                    channel.flush(future);
                }
            }
        };

        Configuration configuration = new Configuration(8800);
        ChannelContext context = new ChannelContext(configuration);
        ChannelConnector connector = new ChannelConnector(context);
        context.setIoEventHandle(eventHandleAdaptor);
        context.setProtocolCodec(new ProtobaseCodec());
        context.addChannelEventListener(new LoggerSocketSEListener());
        NioSocketChannel channel = connector.connect();

        for (; channel.isOpened();) {
            ProtobaseFuture future = new ProtobaseFutureImpl("broadcast");
            future.setBroadcast(true);
            String msg = "broadcast msg___S:" + System.currentTimeMillis();
            future.write(msg, context);
            future.writeBinary("__^^^binary^^^__".getBytes());
            channel.flush(future);
            ThreadUtil.sleep(10);
        }

        CloseUtil.close(connector);

    }

}
