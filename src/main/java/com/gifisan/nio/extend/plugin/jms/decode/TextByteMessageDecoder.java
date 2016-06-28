package com.gifisan.nio.extend.plugin.jms.decode;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.plugin.jms.Message;
import com.gifisan.nio.extend.plugin.jms.TextByteMessage;

public class TextByteMessageDecoder implements MessageDecoder{

	public Message decode(ReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		String text = param.getParameter("text");
		
		BufferedOutputStream outputStream = (BufferedOutputStream) future.getOutputStream();
		
		byte[] array = outputStream.toByteArray();
		
		return new TextByteMessage(messageID,queueName,text,array);
	}
}
