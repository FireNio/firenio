package com.gifisan.mtp.component;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;

public class FixedProtocolDecoder implements ProtocolDecoder {

	private MTPRequestInputStream 	inputStream 		= null;
	private JSONObject 				parameters 		= null;
	private String 				serviceName 		= null;
	private String 				sessionID 		= null;
	private boolean 				beat 			= false;

	public MTPRequestInputStream getInputStream() {
		return inputStream;
	}
	
	public JSONObject getParameters() {
		return parameters;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public String getSessionID() {
		return sessionID;
	}
	
	public boolean decode(ServletContext context,ServerEndPoint endPoint) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocate(10);
		int length = endPoint.read(buffer);
		
		if (length < 10) {
			return false;
		}

		byte [] header = buffer.array();
		
		byte type = header[0];
		
		if (type < 4) {
			return headerDecoders[type].parse(context, this, endPoint, header);
		}else{
			return false;
		}
	}
	
	public boolean isBeat() {
		return this.beat;
	}

	private static abstract class Decoder{
		int getPLength(byte [] header){
			int v0 = (header[3] & 0xff);
			int v1 = (header[4] & 0xff) << 8;  
			int v2 = (header[5] & 0xff) << 16;  
			return v0 | v1 | v2;
		}
		
		JSONObject readDataText(int pLength,ServletContext context,ServerEndPoint endPoint) throws IOException{
			if (pLength > 0) {
				ByteBuffer buffer = endPoint.completeRead(pLength);
				byte [] bytes = buffer.array();
				String content = new String(bytes,context.getEncoding());
				return JSONObject.parseObject(content);
			}
			return null;
		}
		
		int getDLength(byte [] header){
			int v0 = (header[6]  & 0xff);  
			int v1 = (header[7]  & 0xff) << 8;  
			int v2 = (header[8] & 0xff) << 16;  
			int v3 = (header[9] & 0xff) << 24; 
			return v0 | v1 | v2 | v3;
		}
		
		MTPRequestInputStream readDataInputStream(int dLength,ServerEndPoint endPoint) throws IOException{
			if (dLength == 0) {
				return null;
			}
			
			MTPRequestInputStream inputStream = new MTPRequestInputStream(endPoint,dLength);
			endPoint.setMTPRequestInputStream(inputStream);
			return inputStream;
		}
		
		void gainNecessary(FixedProtocolDecoder decoder,ServerEndPoint endPoint,byte [] header) throws IOException{
			int sLength = header[1];
			int kLength = header[2];
			ByteBuffer buffer = endPoint.read(sLength+kLength);
			byte [] bytes = buffer.array();
			String sessionID = new String(bytes,0,sLength);
			String serviceName = new String(bytes,sLength,kLength);
			
			if (StringUtil.isNullOrBlank(serviceName)) {
				throw new EOFException("service key is empty");
			}
			
			if (StringUtil.isNullOrBlank(sessionID)) {
				throw new EOFException("sessionID is empty");
			}
			
			decoder.serviceName = serviceName;
			
			decoder.sessionID = sessionID;
		}
		
		abstract boolean parse(ServletContext context,FixedProtocolDecoder decoder, ServerEndPoint endPoint,byte [] header) throws IOException;
		
	}
	
	
	private static final Decoder [] headerDecoders = new Decoder[]{
		//TEXT
		new Decoder() {
			public boolean parse(ServletContext context, FixedProtocolDecoder decoder,
					ServerEndPoint endPoint, byte[] header) throws IOException {
				
				int pLength = getPLength(header);
				
				gainNecessary(decoder,endPoint,header);

				decoder.parameters = readDataText(pLength, context,endPoint);
				
				return true;
				
			}
		},
		//STREAM
		new Decoder() {
			public boolean parse(ServletContext context, FixedProtocolDecoder decoder,
					ServerEndPoint endPoint, byte[] header) throws IOException {
				int dLength = getDLength(header);
				
				gainNecessary(decoder,endPoint,header);
				
				decoder.inputStream = readDataInputStream(dLength,endPoint);
				
				return true;
				
			}
		},
		//MULT
		new Decoder() {
			public boolean parse(ServletContext context, FixedProtocolDecoder decoder,
					ServerEndPoint endPoint, byte[] header) throws IOException {
				int pLength = getPLength(header);
				int dLength = getDLength(header);
				
				gainNecessary(decoder,endPoint,header);
				
				decoder.parameters = readDataText(pLength, context,endPoint);
				
				decoder.inputStream = readDataInputStream(dLength,endPoint);
				
				return true;
				
			}
		},
		//BEAT
		new Decoder() {
			public boolean parse(ServletContext context, FixedProtocolDecoder decoder,
					ServerEndPoint endPoint, byte[] header) throws IOException {
				decoder.beat = true;
				return true;
				
			}
		}
		
	};
	
}
