/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.nio.container.jms.server;

import java.util.Map;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.concurrent.ReentrantMap;
import com.generallycloud.nio.component.concurrent.ReentrantSet;
import com.generallycloud.nio.container.AbstractPluginContext;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.decode.DefaultMessageDecoder;
import com.generallycloud.nio.container.jms.decode.MessageDecoder;
import com.generallycloud.nio.container.service.FutureAcceptorService;

public class MQContext extends AbstractPluginContext implements MessageQueue {

	private long						dueTime;
	private ReentrantMap<String, Message>	messageIDs	= new ReentrantMap<String, Message>();
	private P2PProductLine				p2pProductLine	= new P2PProductLine(this);
	private SubscribeProductLine			subProductLine	= new SubscribeProductLine(this);
	private ReentrantSet<String>			receivers		= new ReentrantSet<String>();
	private MessageDecoder				messageDecoder	= new DefaultMessageDecoder();
	private static MQContext			instance;

	public static MQContext getInstance() {
		return instance;
	}

	public Message browser(String messageID) {
		return messageIDs.get(messageID);
	}

	public MQSessionAttachment getSessionAttachment(SocketSession session) {
		return (MQSessionAttachment) session.getAttachment(this.getPluginIndex());
	}

	@Override
	public void initialize(ApplicationContext context, Configuration config) throws Exception {

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

		messageIDs.put(message.getMsgID(), message);

		p2pProductLine.offerMessage(message);
	}

	public void publishMessage(Message message) {

		subProductLine.offerMessage(message);
	}

	public void consumerMessage(Message message) {

		messageIDs.remove(message.getMsgID());
	}

	public Message parse(ProtobaseReadFuture future) throws MQException {
		return messageDecoder.decode(future);
	}

	@Override
	public void pollMessage(SocketSession session, ProtobaseReadFuture future, MQSessionAttachment attachment) {

		p2pProductLine.pollMessage(session, future, attachment);
	}

	public void subscribeMessage(SocketSession session, ProtobaseReadFuture future, MQSessionAttachment attachment) {

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
		putServlet(acceptors,new MQConsumerServlet());
		putServlet(acceptors,new MQProducerServlet());
		putServlet(acceptors,new MQSubscribeServlet());
		putServlet(acceptors,new MQPublishServlet());
		putServlet(acceptors,new MQTransactionServlet());
		putServlet(acceptors,new MQBrowserServlet());
	}
	
	protected void putServlet(Map<String, FutureAcceptorService> acceptors,MQServlet servlet){
		String name = servlet.getClass().getSimpleName();
		acceptors.put(name, servlet);
	}

}
