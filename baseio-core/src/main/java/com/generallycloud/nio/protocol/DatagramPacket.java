/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.nio.protocol;

import java.nio.charset.Charset;

import com.generallycloud.nio.buffer.ByteBuf;

/**
 * 
 * <pre>
 * [0       ~              11]
 *  0  ~  7   = timestamp
 *  8  ~  11  = sequenceNo
 * </pre>
 * 
 */
public class DatagramPacket{

	public static final int		PACKET_HEADER		= 8 + 4;
	public static final int		IP_HEADER		= 20;
	public static final int		UDP_HEADER		= 8;
	public static final int		PACKET_MAX		= 1500 - IP_HEADER - UDP_HEADER - PACKET_HEADER;
	
//	public static final byte		TRANS			;
//	public static final byte		COMMAND			= 1;
//	public static final byte		HANDLE			= 2;

	private byte[]			data				;
//	private byte				protocolType		= TRANS;
	private int				sequenceNo		= -1;			// 4 byte
	private long				timestamp			= -1;			// 8 byte
	private ByteBuf			source			;
	private String				dataString		;

	protected DatagramPacket(long timestamp, int sequenceNO, byte[] data) {
		this.timestamp = timestamp;
		this.sequenceNo = sequenceNO;
		this.data = data;
	}

	public DatagramPacket(byte[] data) {
		this.data = data;
	}
	
	public DatagramPacket(ByteBuf source) {
		this.source = source;
	}

	public byte[] getData() {
		
		if (data == null) {

			int length = source.position() - PACKET_HEADER;
			
			int offset = PACKET_HEADER + source.offset();

			data = new byte[length];

			System.arraycopy(source.array(), offset, data, 0, length);
		}
		return data;
	}

	public String getDataString(Charset encoding) {

		if (dataString == null) {

			dataString = new String(getData(), encoding);
		}

		return dataString;
	}

	public int getSequenceNo() {
		
		if (sequenceNo == -1) {
			if (source == null) {
				return sequenceNo;
			}
			sequenceNo = source.getInt(8);
		}
		
		return sequenceNo;
	}

	public long getTimestamp() {
		
		if (timestamp == -1) {
			if (source == null) {
				return timestamp;
			}
			timestamp = source.getLong(0);
		}
		
		return timestamp;
	}

	public ByteBuf getSource() {
		return source;
	}
	
	@Override
	public String toString() {
		
		return new StringBuilder("[data:")
				.append(new String(getData()))
				.append(",seq:")
				.append(getSequenceNo())
				.append(",timestamp:")
				.append(getTimestamp())
				.append("]")
				.toString();
		
	}

}
