package com.gifisan.nio.jms.client;

import java.io.IOException;

import com.gifisan.nio.client.ClientResponse;
import com.gifisan.nio.common.StreamUtil;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.TextMessage;

public class MessageDecoder {
	
	public static Message decode(ClientResponse response) throws JMSException{
		int msgType = response.getParameters().getIntegerParameter("msgType");
		Message message = messageParsesFromJSON[msgType].decode(response);
		return message;
	}
	
	static interface MessageDecodeFromJSON {
		
		Message decode(ClientResponse object) throws JMSException;
	}
	
	private static MessageDecodeFromJSON[] messageParsesFromJSON = new MessageDecodeFromJSON[]{
		//ERROR Message
		new MessageDecodeFromJSON() {
			
			public Message decode(ClientResponse response) {
				Parameters param = response.getParameters();
				ErrorMessage message = new ErrorMessage(param.getIntegerParameter("code"));
				return message;
			}
		},
		//NULL Message
		new MessageDecodeFromJSON() {
			
			public Message decode(ClientResponse object) {
				return null;
			}
		},
		//Text Message
		new MessageDecodeFromJSON() {
			
			public Message decode(ClientResponse response) {
				Parameters param = response.getParameters();
				String messageID = param.getParameter("msgID");
				String queueName = param.getParameter("queueName");
				String text = param.getParameter("text");
				TextMessage message = new TextMessage(messageID,queueName,text);
				
				
				return message;
			}
		},
		new MessageDecodeFromJSON() {
			
			public Message decode(ClientResponse response) throws JMSException {
				Parameters param = response.getParameters();
				String messageID = param.getParameter("msgID");
				String queueName = param.getParameter("queueName");
				String text = param.getParameter("text");
				try {
					byte[] array = StreamUtil.completeRead(response.getInputStream());
					
					return new ByteMessage(messageID,queueName,text,array);
					
				} catch (IOException e) {
					throw new JMSException(e.getMessage()+response.getText(),e);
				}
			}
		}
	};
}
