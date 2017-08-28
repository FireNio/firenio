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

import com.generallycloud.baseio.balance.BalanceContext;
import com.generallycloud.baseio.codec.protobase.HashedProtobaseProtocolFactory;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.protocol.Future;

public class TestBalanceLoad {

    public static void main(String[] args) throws Exception {

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {

                ProtobaseFuture f = (ProtobaseFuture) future;

                if (BalanceContext.BALANCE_CHANNEL_LOST.equals(f.getFutureName())) {
                    System.out.println("客户端已下线：" + f.getReadText());
                } else {
                    System.out.println("收到报文：" + future.toString());
                    String res = "_____________" + f.getReadText();
                    System.out.println("处理报文：" + res);
                    f.write(res);
                    session.flush(future);
                }
            }
        };

        ServerConfiguration configuration = new ServerConfiguration(8800);

        SocketChannelContext context = new NioSocketChannelContext(configuration);

        SocketChannelConnector connector = new SocketChannelConnector(context);

        context.setIoEventHandleAdaptor(eventHandleAdaptor);

        context.setProtocolFactory(new HashedProtobaseProtocolFactory());

        context.addSessionEventListener(new LoggerSocketSEListener());

        connector.connect();

        System.in.read();

        CloseUtil.close(connector);
    }

}
