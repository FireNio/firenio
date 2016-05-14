package com.gifisan.nio.plugin.jms.decode;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;

public class DefaultMessageDecoder implements MessageDecoder {
	
	private MessageDecoder[] decoders = new MessageDecoder[4];
	
	public DefaultMessageDecoder(){
		decoders[0] = new ErrorMessageDecoder();
		decoders[1] = new EmptyMessageDecoder();
		decoders[2] = new TextMessageDecoder();
		decoders[3] = new ByteMessageDecoder();
	}
	
	public Message decode(ReadFuture future) throws JMSException{
		int msgType = future.getParameters().getIntegerParameter("msgType");
		return decoders[msgType].decode(future);
	}
}
