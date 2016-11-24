package com.generallycloud.nio.protocol;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BaseContext;

/**
 * 
 * <pre>
 * [0       ~              11]
 *  0  ~  7   = timestamp
 *  8  ~  11  = sequenceNo
 * </pre>
 * 
 */
public class DatagramPacket extends AbstractReadFuture implements DatagramReadFuture{

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
	private int				sourceLength		;
	private InetSocketAddress	remoteSocketAddress	;

	public DatagramPacket(BaseContext context,ByteBuf buf,InetSocketAddress remoteSocketAddress) {
		super(context);
		this.source = buf;
		this.sourceLength = buf.position();
		this.remoteSocketAddress = remoteSocketAddress;
	}

	protected DatagramPacket(BaseContext context,long timestamp, int sequenceNO, byte[] data) {
		super(context);
		this.timestamp = timestamp;
		this.sequenceNo = sequenceNO;
		this.data = data;
	}

	public DatagramPacket(BaseContext context,byte[] data) {
		super(context);
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

	public ByteBuf getSource() {
		return source;
	}
	
	public void release() {
		
	}
	
	@Override
	public DatagramReadFuture newDatagramReadFuture() {
		// TODO Auto-generated method stub
		return null;
	}

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
