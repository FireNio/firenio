package com.generallycloud.nio.container.jms.decode;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Message;

public class DefaultMessageDecoder implements MessageDecoder {
	
	private MessageDecoder[] decoders = new MessageDecoder[6];
	
	public DefaultMessageDecoder(){
		decoders[Message.TYPE_ERROR] = new ErrorMessageDecoder();
		decoders[Message.TYPE_NULL] = new EmptyMessageDecoder();
		decoders[Message.TYPE_TEXT] = new TextMessageDecoder();
		decoders[Message.TYPE_TEXT_BYTE] = new TextByteMessageDecoder();
		decoders[Message.TYPE_MAP] = new MapMessageDecoder();
		decoders[Message.TYPE_MAP_BYTE] = new MapByteMessageDecoder();
	}
	
	public Message decode(ProtobaseReadFuture future) throws MQException{
		int msgType = future.getParameters().getIntegerParameter("msgType");
		return decoders[msgType].decode(future);
	}
}
