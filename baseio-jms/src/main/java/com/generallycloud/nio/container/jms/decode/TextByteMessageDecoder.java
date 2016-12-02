package com.generallycloud.nio.container.jms.decode;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.TextByteMessage;

public class TextByteMessageDecoder implements MessageDecoder{

	public Message decode(ProtobaseReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		String text = param.getParameter("text");
		
		byte[] array = future.getBinary();
		
		return new TextByteMessage(messageID,queueName,text,array);
	}
}
