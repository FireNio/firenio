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

public class TestTransaction {

    //    public static void main(String[] args) throws Exception {
    //
    //        LoggerFactory.configure();
    //
    //        SimpleIoEventHandle eventHandle = new SimpleIoEventHandle();
    //
    //        ServerConfiguration configuration = new ServerConfiguration(8300);
    //
    //        SocketChannelContext context = new NioSocketChannelContext(configuration);
    //
    //        ChannelConnector connector = new ChannelConnector(context);
    //
    //        context.setIoEventHandle(eventHandle);
    //
    //        context.setProtocolCodec(new ProtobaseCodec());
    //
    //        context.addSessionEventListener(new LoggerSocketSEListener());
    //
    //        FixedSession session = new FixedSession(connector.connect());
    //
    //        MessageConsumer consumer = new DefaultMessageConsumer(session);
    //
    //        rollback(consumer);
    //
    //        //		commit(consumer);
    //
    //        connector.close();
    //
    //    }
    //
    //    static void commit(MessageConsumer consumer) throws MQException {
    //        long old = System.currentTimeMillis();
    //
    //        consumer.beginTransaction();
    //
    //        consumer.receive(new OnMessage() {
    //
    //            @Override
    //            public void onReceive(Message message) {
    //                System.out.println(message);
    //            }
    //        });
    //
    //        consumer.commit();
    //
    //        System.out.println("Time:" + (System.currentTimeMillis() - old));
    //
    //    }
    //
    //    static void rollback(MessageConsumer consumer) throws MQException {
    //        long old = System.currentTimeMillis();
    //
    //        consumer.beginTransaction();
    //
    //        consumer.receive(new OnMessage() {
    //
    //            @Override
    //            public void onReceive(Message message) {
    //
    //                System.out.println(message);
    //            }
    //        });
    //
    //        consumer.rollback();
    //
    //        System.out.println("Time:" + (System.currentTimeMillis() - old));
    //    }

}
