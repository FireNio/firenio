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
package com.generallycloud.baseio.container.jms.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.concurrent.ConcurrentSet;
import com.generallycloud.baseio.container.AbstractPluginContext;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.jms.MQException;
import com.generallycloud.baseio.container.jms.Message;
import com.generallycloud.baseio.container.jms.decode.DefaultMessageDecoder;
import com.generallycloud.baseio.container.jms.decode.MessageDecoder;
import com.generallycloud.baseio.container.service.FutureAcceptorService;

public class MQContext extends AbstractPluginContext implements MessageQueue {

    private long                           dueTime;
    private ConcurrentMap<String, Message> messageIds     = new ConcurrentHashMap<>();
    private P2PProductLine                 p2pProductLine = new P2PProductLine(this);
    private SubscribeProductLine           subProductLine = new SubscribeProductLine(this);
    private ConcurrentSet<String>          receivers      = new ConcurrentSet<>();
    private MessageDecoder                 messageDecoder = new DefaultMessageDecoder();
    private static MQContext               instance;

    public static MQContext getInstance() {
        return instance;
    }

    public Message browser(String messageId) {
        return messageIds.get(messageId);
    }

    public MQSessionAttachment getSessionAttachment(SocketSession session) {
        return (MQSessionAttachment) session.getAttribute(getPluginKey());
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {

        super.initialize(context, config);

        long dueTime = config.getLongParameter("due-time");

        setMessageDueTime(dueTime == 0 ? 1000 * 60 * 60 * 24 * 7 : dueTime);

        p2pProductLine.startup("MQ-P2P-ProductLine");
        subProductLine.startup("MQ-SUB-ProductLine");

        context.addSessionEventListener(new MQSessionEventListener());

        instance = this;
    }

    @Override
    public void destroy(ApplicationContext context, Configuration config) throws Exception {
        LifeCycleUtil.stop(p2pProductLine);
        LifeCycleUtil.stop(subProductLine);
        instance = null;
        super.destroy(context, config);
    }

    public long getMessageDueTime() {
        return this.dueTime;
    }

    public int messageSize() {
        return this.p2pProductLine.messageSize();
    }

    @Override
    public void offerMessage(Message message) {

        messageIds.put(message.getMsgId(), message);

        p2pProductLine.offerMessage(message);
    }

    public void publishMessage(Message message) {

        subProductLine.offerMessage(message);
    }

    public void consumerMessage(Message message) {

        messageIds.remove(message.getMsgId());
    }

    public Message parse(ProtobaseFuture future) throws MQException {
        return messageDecoder.decode(future);
    }

    @Override
    public void pollMessage(SocketSession session, ProtobaseFuture future,
            MQSessionAttachment attachment) {
        p2pProductLine.pollMessage(session, future, attachment);
    }

    public void subscribeMessage(SocketSession session, ProtobaseFuture future,
            MQSessionAttachment attachment) {

        subProductLine.pollMessage(session, future, attachment);
    }

    public void setMessageDueTime(long dueTime) {
        this.dueTime = dueTime;
        this.p2pProductLine.setDueTime(dueTime);
        this.subProductLine.setDueTime(dueTime);
    }

    public void addReceiver(String queueName) {
        receivers.add(queueName);
    }

    public boolean isOnLine(String queueName) {
        return receivers.contains(queueName);
    }

    public void removeReceiver(String queueName) {
        receivers.remove(queueName);
    }

    @Override
    public void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors) {
        putServlet(acceptors, new MQConsumerServlet());
        putServlet(acceptors, new MQProducerServlet());
        putServlet(acceptors, new MQSubscribeServlet());
        putServlet(acceptors, new MQPublishServlet());
        putServlet(acceptors, new MQTransactionServlet());
        putServlet(acceptors, new MQBrowserServlet());
    }

}
