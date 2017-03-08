/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.baseio.codec.protobuf.future;

import java.io.IOException;
import java.nio.charset.Charset;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

public class ProtobufReadFutureImpl extends ProtobaseReadFutureImpl implements ProtobufReadFuture {

	private MessageLite	message;

	private boolean	writed;

	// for ping & pong
	public ProtobufReadFutureImpl(SocketChannelContext context) {
		super(context);
	}

	public ProtobufReadFutureImpl(SocketChannelContext context, String futureName) {
		super(context, futureName);
	}

	public ProtobufReadFutureImpl(SocketChannelContext context, Integer futureID, String futureName) {
		super(context, futureID, futureName);
	}

	public ProtobufReadFutureImpl(SocketSession session, ByteBuf buf, int limit) throws IOException {
		super(session, buf, limit);
	}

	@Override
	public MessageLite getMessage() throws InvalidProtocolBufferException {

		if (message == null) {

			ProtobufIOEventHandle handle = (ProtobufIOEventHandle) context.getIoEventHandleAdaptor();

			Parser<? extends MessageLite> parser = handle.getParser(getParserName());

			message = parser.parseFrom(getBinary());
		}

		return message;
	}

	@Override
	public void writeProtobuf(MessageLite messageLite) throws InvalidProtocolBufferException {
		writeProtobuf(messageLite.getClass().getName(), messageLite);
	}

	@Override
	public void writeProtobuf(String parserName, MessageLite messageLite) throws InvalidProtocolBufferException {

		if (writed) {
			throw new InvalidProtocolBufferException("writed");
		}

		super.write(parserName);

		// FIXME 判断array是否过大
		byte[] array = messageLite.toByteArray();

		super.writeBinary(array, 0, array.length);
	}

	public void write(byte[] bytes, int offset, int length) {
		throw new UnsupportedOperationException("use writeProtobuf instead");
	}

	public void write(String content, Charset encoding) {
		throw new UnsupportedOperationException("use writeProtobuf instead");
	}

	@Override
	public String getParserName() {
		return getReadText();
	}

}
