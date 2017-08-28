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

import com.generallycloud.baseio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.container.FixedSession;
import com.generallycloud.baseio.container.SimpleIoEventHandle;
import com.generallycloud.baseio.container.jms.MQException;
import com.generallycloud.baseio.container.jms.Message;
import com.generallycloud.baseio.container.jms.client.MessageConsumer;
import com.generallycloud.baseio.container.jms.client.OnMessage;
import com.generallycloud.baseio.container.jms.client.impl.DefaultMessageConsumer;
import com.generallycloud.baseio.log.LoggerFactory;

public class TestTransaction {

    public static void main(String[] args) throws Exception {

        LoggerFactory.configure();

        SimpleIoEventHandle eventHandle = new SimpleIoEventHandle();

        ServerConfiguration configuration = new ServerConfiguration(8300);

        SocketChannelContext context = new NioSocketChannelContext(configuration);

        SocketChannelConnector connector = new SocketChannelConnector(context);

        context.setIoEventHandleAdaptor(eventHandle);

        context.setProtocolFactory(new ProtobaseProtocolFactory());

        context.addSessionEventListener(new LoggerSocketSEListener());

        FixedSession session = new FixedSession(connector.connect());

        session.login("admin", "admin100");

        MessageConsumer consumer = new DefaultMessageConsumer(session);

        rollback(consumer);

        //		commit(consumer);

        connector.close();

    }

    static void commit(MessageConsumer consumer) throws MQException {
        long old = System.currentTimeMillis();

        consumer.beginTransaction();

        consumer.receive(new OnMessage() {

            @Override
            public void onReceive(Message message) {
                System.out.println(message);
            }
        });

        consumer.commit();

        System.out.println("Time:" + (System.currentTimeMillis() - old));

    }

    static void rollback(MessageConsumer consumer) throws MQException {
        long old = System.currentTimeMillis();

        consumer.beginTransaction();

        consumer.receive(new OnMessage() {

            @Override
            public void onReceive(Message message) {

                System.out.println(message);
            }
        });

        consumer.rollback();

        System.out.println("Time:" + (System.currentTimeMillis() - old));
    }

}
