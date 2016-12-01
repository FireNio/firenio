package com.generallycloud.nio.container.jms.decode;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.TextMessage;

public class TextMessageDecoder implements MessageDecoder{

	public Message decode(BaseReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		String text = param.getParameter("text");
		return new TextMessage(messageID,queueName,text);
	}
}
