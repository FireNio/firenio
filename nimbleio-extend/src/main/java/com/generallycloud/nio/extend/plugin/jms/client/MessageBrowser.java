package com.generallycloud.nio.extend.plugin.jms.client;

import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.Message;

public interface MessageBrowser {
	
	

	/**
	 * 1.消息中有重复的ID则browser到的消息为后者</BR>
	 * 2.不允许browser到处于事务中的消息</BR>
	 * 3.本方法用于检查服务器是否依然存在此ID的消息
	 * @param messageID
	 * @return
	 * @throws MQException
	 */
	public abstract Message browser(String messageID) throws MQException;
	
	public abstract int size() throws MQException;
	
	public abstract boolean isOnline(String queueName) throws MQException;
	
}
