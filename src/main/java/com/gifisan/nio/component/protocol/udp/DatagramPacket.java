package com.gifisan.nio.component.protocol.udp;

import com.gifisan.nio.common.MathUtil;

/**
 * 
 * <pre>
 * [0       ~              19]
 *  0  ~  7   = timestamp
 *  8  ~  11  = sequenceNo
 *  12 ~  19  = targetEndpoint
 * </pre>
 * 
 */
public class DatagramPacket {

	private int		base			= 8 + 8 + 4;
	private byte[]	data			= null;
	private int		length		= 0;
	private int		sequenceNo	= 0;		// 4 byte
	private long		targetEndpoint	= 0;		// 8 byte
	private long		timestamp		= 0;		// 8 byte

	public DatagramPacket(byte[] data, int length) {

		this.length = length - base;
		this.timestamp = MathUtil.byte2Long(data, 0);
		this.sequenceNo = MathUtil.byte2Int(data, 8);
		this.targetEndpoint = MathUtil.byte2Long(data, 12);

		System.arraycopy(data, base, this.data, 0, this.length);
	}

	public byte[] getData() {
		return data;
	}

	public int getLength() {
		return length;
	}

	public int getSequenceNo() {
		return sequenceNo;
	}

	public long getTargetEndpoint() {
		return targetEndpoint;
	}

	public long getTimestamp() {
		return timestamp;
	}

}
