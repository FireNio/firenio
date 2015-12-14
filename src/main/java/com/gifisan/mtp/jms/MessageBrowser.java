package com.gifisan.mtp.jms;

import com.gifisan.mtp.jms.client.Connection;

public interface MessageBrowser extends Connection{
	
	

	/**
	 * 1.消息中有重复的ID则browser到的消息为后者</BR>
	 * 2.不允许browser到处于事务中的消息</BR>
	 * 3.本方法用于检查服务器是否依然存在此ID的消息
	 * @param messageID
	 * @return
	 * @throws JMSException
	 */
	public abstract Message browser(String messageID) throws JMSException;
	
	public abstract int size() throws JMSException;
	
}
