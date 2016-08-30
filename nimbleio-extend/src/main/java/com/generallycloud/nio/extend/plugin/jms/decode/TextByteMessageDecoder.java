package com.generallycloud.nio.extend.plugin.jms.decode;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.TextByteMessage;

public class TextByteMessageDecoder implements MessageDecoder{

	public Message decode(NIOReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		String text = param.getParameter("text");
		
		BufferedOutputStream outputStream = (BufferedOutputStream) future.getOutputStream();
		
		byte[] array = outputStream.toByteArray();
		
		return new TextByteMessage(messageID,queueName,text,array);
	}
}
