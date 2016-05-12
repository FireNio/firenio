package com.gifisan.nio.component.protocol.udp;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

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

	public static final int	PACKET_HEADER		= 8 + 8 + 4;
	public static final int	PACKET_MAX		= 1500 - 20 - 8;
	
	private byte[]	data				= null;
	private int		sequenceNo		= 0;		// 4 byte
	private long		targetEndpointID	= 0;		// 8 byte
	private long		timestamp			= 0;		// 8 byte
	private byte [] 	source			= null;
	private String		dataString		= null;
	private int 		sourceLength		= 0;

	public DatagramPacket(ByteBuffer buffer) {
		
		this.source = buffer.array();
		this.sourceLength = buffer.position();
		this.timestamp = MathUtil.byte2Long(source, 0);
		this.sequenceNo = MathUtil.byte2Int(source, 8);
		this.targetEndpointID = MathUtil.byte2Long(source, 12);

//		System.arraycopy(data, 0, this.source, 0, buffer.position());
	}

	public DatagramPacket(long timestamp, int sequenceNO, long targetEndPointID,byte [] data) {
		this.timestamp = timestamp;
		this.sequenceNo = sequenceNO;
		this.targetEndpointID = targetEndPointID;
		this.data = data;
	}
	
	public DatagramPacket(byte [] data) {
		this.data = data;
	}

	public byte[] getData() {
		if (data == null) {
			
			int length = sourceLength - PACKET_HEADER;
			
			data = new byte[length];
			
			System.arraycopy(source, PACKET_HEADER, data, 0, length);
		}
		return data;
	}
	
	public String getDataString(Charset encoding){
		
		if (dataString == null) {
			
			int length = sourceLength - PACKET_HEADER;
			
			dataString = new String(source,PACKET_HEADER,length,encoding);
		}
		
		return dataString; 
	}

	public int getSequenceNo() {
		return sequenceNo;
	}

	public long getTargetEndpointID() {
		return targetEndpointID;
	}

	public long getTimestamp() {
		return timestamp;
	}

}
