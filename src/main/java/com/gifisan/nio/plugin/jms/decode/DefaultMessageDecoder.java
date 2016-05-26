package com.gifisan.nio.plugin.jms.decode;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;

public class DefaultMessageDecoder implements MessageDecoder {
	
	private MessageDecoder[] decoders = new MessageDecoder[6];
	
	public DefaultMessageDecoder(){
		decoders[Message.TYPE_ERROR] = new ErrorMessageDecoder();
		decoders[Message.TYPE_NULL] = new EmptyMessageDecoder();
		decoders[Message.TYPE_TEXT] = new TextMessageDecoder();
		decoders[Message.TYPE_TEXT_BYTE] = new TextByteMessageDecoder();
		decoders[Message.TYPE_MAP] = new MapMessageDecoder();
		decoders[Message.TYPE_MAP_BYTE] = new MapMessageDecoder();
	}
	
	public Message decode(ReadFuture future) throws JMSException{
		int msgType = future.getParameters().getIntegerParameter("msgType");
		return decoders[msgType].decode(future);
	}
}
