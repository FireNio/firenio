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
package com.generallycloud.nio.codec.protobase.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.generallycloud.nio.balance.AbstractBalanceReadFuture;
import com.generallycloud.nio.balance.BalanceReadFuture;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.JsonParameters;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;

/**
 *
 */
public class ProtobaseReadFutureImpl extends AbstractBalanceReadFuture implements ProtobaseReadFuture {

	private byte[]			binary;
	private int				binaryLength;
	private int				binaryLimit;
	private boolean			body_complete;
	private ByteBuf			buf;
	private Integer			futureID;
	private String				futureName;
	private int				hashCode;
	private boolean			header_complete;
	private Parameters			parameters;
	private int				future_name_length;
	private int				textLength;
	private boolean			translated;

	private BufferedOutputStream	writeBinaryBuffer;

	// for ping & pong
	public ProtobaseReadFutureImpl(SocketChannelContext context) {
		super(context);
		this.header_complete = true;
		this.body_complete = true;
	}

	public ProtobaseReadFutureImpl(SocketChannelContext context, Integer futureID, String futureName) {
		super(context);
		this.futureName = futureName;
		this.futureID = futureID;
	}

	public ProtobaseReadFutureImpl(SocketChannelContext context, String futureName) {
		super(context);
		this.futureName = futureName;
		this.futureID = 0;
	}

	public ProtobaseReadFutureImpl(SocketSession session, ByteBuf buf, int binaryLimit) throws IOException {
		super(session.getContext());
		this.buf = buf;
		this.binaryLimit = binaryLimit;
	}
	
	private void doBodyComplete(Session session, ByteBuf buf) {

		Charset charset = session.getEncoding();

		int offset = buf.offset();

		ByteBuffer memory = buf.nioBuffer();

		memory.limit(offset + future_name_length);

		futureName = StringUtil.decode(charset, memory);

		memory.limit(memory.position() + textLength);

		readText = StringUtil.decode(charset, memory);
		
		gainBinary(buf, offset);
	}

	private void doHeaderComplete(Session session, ByteBuf buf) throws IOException {
		
		this.future_name_length = buf.getUnsignedByte();

		this.futureID = buf.getInt();

		this.sessionID = buf.getInt();
		
		this.hashCode = buf.getInt();

		this.textLength = buf.getUnsignedShort();
		
		if (buf.hasRemaining()) {
			this.binaryLength = buf.getInt();
		}

		int all_length = future_name_length + textLength + binaryLength;
		
		buf.reallocate(all_length,binaryLimit);
	}

	private void gainBinary(ByteBuf buf, int offset) {

		if (!hasBinary()) {
			return;
		}

		buf.skipBytes(future_name_length + textLength);

		binary = buf.getBytes();
	}

	@Override
	public byte[] getBinary() {
		return binary;
	}

	@Override
	public int getBinaryLength() {
		return binaryLength;
	}

	@Override
	public Integer getFutureID() {
		if (futureID == null) {
			futureID = 0;
		}
		return futureID;
	}

	@Override
	public String getFutureName() {
		return futureName;
	}

	@Override
	public int getHashCode() {
		return hashCode;
	}

	@Override
	public Parameters getParameters() {
		if (parameters == null) {
			parameters = new JsonParameters(getReadText());
		}
		return parameters;
	}

	@Override
	public int getTextLength() {
		return textLength;
	}

	@Override
	public BufferedOutputStream getWriteBinaryBuffer() {
		return writeBinaryBuffer;
	}

	@Override
	public boolean hasBinary() {
		return binaryLength > 0;
	}

	@Override
	public boolean isBroadcast() {
		return getFutureID().intValue() == 0;
	}

	@Override
	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		ByteBuf buf = this.buf;

		if (!header_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}
			
			header_complete = true;
			
			doHeaderComplete(session, buf.flip());
		}

		if (!body_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			body_complete = true;

			doBodyComplete(session, buf.flip());
		}

		return true;
	}

	@Override
	public void release() {
		ReleaseUtil.release(buf);
	}

	@Override
	public void setFutureID(Object futureID) {
		this.futureID = (Integer) futureID;
	}

	@Override
	public void setHashCode(int hashCode) {
		this.hashCode = hashCode;
	}

	@Override
	public String toString() {
		return getFutureName() + "@" + getReadText();
	}

	@Override
	public BalanceReadFuture translate(){

		if (!translated) {
			translated = true;
			this.write(readText);
			this.writeBinary(binary);
		}

		return this;
	}

	@Override
	public void writeBinary(byte b) {

		if (writeBinaryBuffer == null) {
			writeBinaryBuffer = new BufferedOutputStream();
		}

		writeBinaryBuffer.write(b);
	}

	@Override
	public void writeBinary(byte[] bytes) {
		if (bytes == null) {
			return;
		}
		writeBinary(bytes, 0, bytes.length);
	}

	@Override
	public void writeBinary(byte[] bytes, int offset, int length) {

		if (writeBinaryBuffer == null) {
			writeBinaryBuffer = new BufferedOutputStream();
		}

		writeBinaryBuffer.write(bytes, offset, length);
	}

}
