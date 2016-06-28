package com.gifisan.nio.extend.plugin.jms.decode;

import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.plugin.jms.Message;
import com.gifisan.nio.extend.plugin.jms.TextMessage;

public class TextMessageDecoder implements MessageDecoder{

	public Message decode(ReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		String text = param.getParameter("text");
		return new TextMessage(messageID,queueName,text);
	}
}
