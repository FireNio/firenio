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
package com.generallycloud.test.io.load.fixedlength;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthFuture;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFutureImpl;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.AioSocketChannelContext;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

public class TestLoadClient {

    final static int time = 400000;

    public static void main(String[] args) throws Exception {

        final Logger logger = LoggerFactory.getLogger(TestLoadClient.class);

        final CountDownLatch latch = new CountDownLatch(time);

        final AtomicInteger res = new AtomicInteger();
        final AtomicInteger req = new AtomicInteger();

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                //				latch.countDown();
                //				long count = latch.getCount();
                //				if (count % 10 == 0) {
                //					if (count < 50) {
                //						logger.info("************************================" + count);
                //					}
                //				}
                //				logger.info("res==========={}",res.getAndIncrement());
            }

        };

        Configuration configuration = new Configuration(8300);
        
//        SocketChannelContext context = new NioSocketChannelContext(configuration);
        SocketChannelContext context = new AioSocketChannelContext(configuration);
        SocketChannelConnector connector = new SocketChannelConnector(context);
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.setProtocolCodec(new ProtobaseCodec());
        context.addSessionEventListener(new LoggerSocketSEListener());
        connector.getContext().setProtocolCodec(new FixedLengthCodec());
        connector.getContext().getConfiguration().setCoreSize(1);
        SocketSession session = connector.connect();
        System.out.println("################## Test start ####################");
        long old = System.currentTimeMillis();

        for (int i = 0; i < time; i++) {
            FixedLengthFuture future = new FixedLengthFutureImpl();
            future.write("hello server!",session);
            session.flush(future);
        }

        latch.await();

        long spend = (System.currentTimeMillis() - old);
        System.out.println("## Execute Time:" + time);
        System.out.println("## OP/S:" + new BigDecimal(time * 1000).divide(new BigDecimal(spend), 2,
                BigDecimal.ROUND_HALF_UP));
        System.out.println("## Expend Time:" + spend);

        CloseUtil.close(connector);

    }
}
