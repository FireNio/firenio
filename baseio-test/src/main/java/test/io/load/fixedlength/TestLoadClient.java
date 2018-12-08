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
package test.io.load.fixedlength;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.firenio.baseio.codec.fixedlength.FixedLengthCodec;
import com.firenio.baseio.codec.fixedlength.FixedLengthFrame;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.NioSocketChannel;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;
import com.firenio.baseio.protocol.Frame;

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
        ChannelConnector context = new ChannelConnector(8300);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setProtocolCodec(new FixedLengthCodec());
        NioSocketChannel channel = context.connect();
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

        Util.close(context);

    }
}
