package com.generallycloud.nio.codec.protobuf.future;

import com.generallycloud.nio.protocol.ReadFuture;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

public interface ProtobufReadFuture extends ReadFuture {

	public abstract void writeProtobuf(String parserName, MessageLite messageLite) throws InvalidProtocolBufferException;
	
	public abstract void writeProtobuf(MessageLite messageLite) throws InvalidProtocolBufferException;

	public abstract MessageLite getMessage() throws InvalidProtocolBufferException;
	
	public abstract String getParserName();
}
