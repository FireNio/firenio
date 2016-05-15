package com.gifisan.nio.component.protocol;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.common.MathUtil;

/**
 * 
 * <pre>
 * [0       ~              19]
 *  0  ~  7   = timestamp
 *  8  ~  11  = sequenceNo
 *  12 ~  13  = roomID
 * </pre>
 * 
 */
public class DatagramPacket {

	public static final int		PACKET_HEADER		= 8 + 4 + 2;
	public static final int		IP_HEADER		= 20;
	public static final int		UDP_HEADER		= 8;
	public static final int		PACKET_MAX		= 1500 - IP_HEADER - UDP_HEADER - PACKET_HEADER;
	
//	public static final byte		TRANS			= 0;
//	public static final byte		COMMAND			= 1;
//	public static final byte		HANDLE			= 2;

	private byte[]			data				= null;
//	private byte				protocolType		= TRANS;
	private int				sequenceNo		= -1;			// 4 byte
	private int				roomID			= -1;			// 8 byte
	private long				timestamp			= -1;			// 8 byte
	private ByteBuffer			source			= null;
	private String				dataString		= null;
	private int				sourceLength		= 0;
	private InetSocketAddress	remoteSocketAddress	= null;

	public DatagramPacket(ByteBuffer buffer,InetSocketAddress remoteSocketAddress) {

		this.source = buffer;
		this.sourceLength = buffer.position();
		this.remoteSocketAddress = remoteSocketAddress;
	}

	public DatagramPacket(long timestamp, int sequenceNO, int roomID, byte[] data) {
		this.timestamp = timestamp;
		this.sequenceNo = sequenceNO;
		this.roomID = roomID;
		this.data = data;
	}

	public DatagramPacket(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		if (data == null) {

			int length = sourceLength - PACKET_HEADER;

			data = new byte[length];

			System.arraycopy(source.array(), PACKET_HEADER, data, 0, length);
		}
		return data;
	}

	public String getDataString(Charset encoding) {

		if (dataString == null) {

			int length = sourceLength - PACKET_HEADER;

			dataString = new String(source.array(), PACKET_HEADER, length, encoding);
		}

		return dataString;
	}

	public int getSequenceNo() {
		
		if (sequenceNo == -1) {
			sequenceNo = MathUtil.byte2Int(source.array(), 8);
		}
		
		return sequenceNo;
	}

	public int getRoomID() {
		
		if (roomID == -1) {
			roomID = MathUtil.byte2Int(source.array(), 12);
		}
		
		return roomID;
	}

	public long getTimestamp() {
		
		if (timestamp == -1) {
			if (source == null) {
				return timestamp;
			}
			timestamp = MathUtil.byte2Long(source.array(), 0);
		}
		
		return timestamp;
	}

	protected InetSocketAddress getRemoteSocketAddress() {
		return remoteSocketAddress;
	}

	public ByteBuffer getSource() {
		return source;
	}

}
