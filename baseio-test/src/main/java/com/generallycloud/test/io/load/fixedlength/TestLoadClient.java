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

import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFrame;
import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Frame;

public class TestLoadClient {

    final static int time = 400000;

    public static void main(String[] args) throws Exception {

        final Logger logger = LoggerFactory.getLogger(TestLoadClient.class);

        final CountDownLatch latch = new CountDownLatch(time);

        final AtomicInteger res = new AtomicInteger();
        final AtomicInteger req = new AtomicInteger();

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
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
        ChannelContext context = new ChannelContext(8300);
        ChannelConnector connector = new ChannelConnector(context);
        context.setIoEventHandle(eventHandleAdaptor);
        context.setProtocolCodec(new ProtobaseCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        connector.getContext().setProtocolCodec(new FixedLengthCodec());
        NioSocketChannel channel = connector.connect();
        System.out.println("################## Test start ####################");
        long old = System.currentTimeMillis();

        for (int i = 0; i < time; i++) {
            FixedLengthFrame frame = new FixedLengthFrame();
            frame.write("hello server!", channel);
            channel.flush(frame);
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
