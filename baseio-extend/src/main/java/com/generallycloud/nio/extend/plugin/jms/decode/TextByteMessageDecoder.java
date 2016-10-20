package com.generallycloud.nio.extend.plugin.jms.decode;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.TextByteMessage;

public class TextByteMessageDecoder implements MessageDecoder{

	public Message decode(BaseReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		String text = param.getParameter("text");
		
		byte[] array = future.getBinary();
		
		return new TextByteMessage(messageID,queueName,text,array);
	}
}
