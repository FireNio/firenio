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

import com.generallycloud.baseio.balance.BalanceClientSocketSession;
import com.generallycloud.baseio.balance.BalanceClientSocketSessionFactory;
import com.generallycloud.baseio.codec.protobase.HashedProtobaseProtocolFactory;
import com.generallycloud.baseio.codec.protobase.future.HashedProtobaseFuture;
import com.generallycloud.baseio.codec.protobase.future.HashedProtobaseFutureImpl;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.protocol.Future;

public class TestBalanceClient {

    public static void main(String[] args) throws Exception {

        final AtomicInteger res = new AtomicInteger();

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {

                ProtobaseFuture f = (ProtobaseFuture) future;

                //				if ("getToken".equals(f.getFutureName())) {
                //					synchronized (lock) {
                //						((BalanceClientSocketSession) session).setToken(f.getToken());
                //						lock.notify();
                //					}
                //					return;
                //				}

                System.out.println(f.getReadText() + "______R:" + System.currentTimeMillis());

                res.incrementAndGet();
            }
        };

        ServerConfiguration configuration = new ServerConfiguration(8600);

        SocketChannelContext context = new NioSocketChannelContext(configuration);

        SocketChannelConnector connector = new SocketChannelConnector(context);

        context.setProtocolFactory(new HashedProtobaseProtocolFactory());

        context.setSocketSessionFactory(new BalanceClientSocketSessionFactory());

        context.addSessionEventListener(new LoggerSocketSEListener());

        context.setIoEventHandleAdaptor(eventHandleAdaptor);

        BalanceClientSocketSession session = (BalanceClientSocketSession) connector.connect();

        for (int i = 0; i < 100; i++) {

            int fid = Math.abs(new Random().nextInt());

            HashedProtobaseFuture future = new HashedProtobaseFutureImpl(context, "future-name");

            future.write("你好！");

            future.setHashCode(fid);

            session.flush(future);
        }

        ThreadUtil.sleep(300);

        System.out.println("==========" + res.get());

        ThreadUtil.sleep(500000000);

        CloseUtil.close(connector);
    }

}
