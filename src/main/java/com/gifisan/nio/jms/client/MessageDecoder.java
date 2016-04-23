package com.gifisan.nio.jms.client;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.TextMessage;

public class MessageDecoder {
	
	public static Message decode(ReadFuture future) throws JMSException{
		int msgType = future.getParameters().getIntegerParameter("msgType");
		Message message = messageParsesFromJSON[msgType].decode(future);
		return message;
	}
	
	static interface MessageDecodeFromJSON {
		
		Message decode(ReadFuture future) throws JMSException;
	}
	
	private static MessageDecodeFromJSON[] messageParsesFromJSON = new MessageDecodeFromJSON[]{
		//ERROR Message
		new MessageDecodeFromJSON() {
			
			public Message decode(ReadFuture future) {
				Parameters param = future.getParameters();
				ErrorMessage message = new ErrorMessage(param.getIntegerParameter("code"));
				return message;
			}
		},
		//NULL Message
		new MessageDecodeFromJSON() {
			
			public Message decode(ReadFuture future) {
				return null;
			}
		},
		//Text Message
		new MessageDecodeFromJSON() {
			
			public Message decode(ReadFuture future) {
				Parameters param = future.getParameters();
				String messageID = param.getParameter("msgID");
				String queueName = param.getParameter("queueName");
				String text = param.getParameter("text");
				TextMessage message = new TextMessage(messageID,queueName,text);
				
				return message;
			}
		},
		new MessageDecodeFromJSON() {
			
			public Message decode(ReadFuture future) throws JMSException {
				Parameters param = future.getParameters();
				String messageID = param.getParameter("msgID");
				String queueName = param.getParameter("queueName");
				String text = param.getParameter("text");
				
				BufferedOutputStream outputStream = (BufferedOutputStream) future.getOutputStream();
				
				byte[] array = outputStream.toByteArray();
				
				return new ByteMessage(messageID,queueName,text,array);
					
			}
		}
	};
}
