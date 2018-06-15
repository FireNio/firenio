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
package com.generallycloud.test.io.jms;

import com.generallycloud.baseio.codec.protobase.ParamedProtobaseCodec;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.container.protobase.SimpleIoEventHandle;

public class TestTeller {

    public static void main(String[] args) throws Exception {

        SimpleIoEventHandle eventHandle = new SimpleIoEventHandle();

        ChannelContext context = new ChannelContext(new Configuration(8300));

        ChannelConnector connector = new ChannelConnector(context);

        context.setIoEventHandle(eventHandle);

        context.setProtocolCodec(new ParamedProtobaseCodec());

        context.addChannelEventListener(new LoggerSocketSEListener());

        //        FixedChannel channel = new FixedChannel(connector.connect());
        //
        //        MessageProducer producer = new DefaultMessageProducer(channel);
        //
        //        TextMessage message = new TextMessage("msgId", "uuid", "你好！");
        //
        //        MapMessage mapMessage = new MapMessage("msgId", "uuid");
        //
        //        mapMessage.put("test", "test111111111111111111111");
        //
        //        long old = System.currentTimeMillis();
        //
        //        producer.offer(message);
        //
        //        producer.offer(mapMessage);
        //
        //        System.out.println("Time:" + (System.currentTimeMillis() - old));

        connector.close();

    }

}
