package com.gifisan.mtp.component;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.common.DateUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;

public class NormalProtocolDecoder implements ProtocolDecoder {

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
		ByteBuffer buffer = ByteBuffer.allocate(1);
		int length = endPoint.read(buffer);
		
		if (length < 1) {
			return false;
		}

		byte type = buffer.get(0);
		
		if (type == 3) {
			System.out.println(">>read beat................."+DateUtil.now());
			this.beat = true;
			return true;
		}
		
		buffer = ByteBuffer.allocate(9);
		length = endPoint.readHead(buffer);
		
		byte [] header = buffer.array();
		
		if (length < 9) {
			// 如果一次读取不到9个byte
			// 这样的连接持续下去也是无法进行业务操作
			// 还有一种情况是有人在恶意攻击服务器
			return false;
		}
		
		if (type < 3) {
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
			int v0 = (header[2] & 0xff);
			int v1 = (header[3] & 0xff) << 8;  
			int v2 = (header[4] & 0xff) << 16;  
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
			int v0 = (header[5]  & 0xff);  
			int v1 = (header[6]  & 0xff) << 8;  
			int v2 = (header[7] & 0xff) << 16;  
			int v3 = (header[8] & 0xff) << 24; 
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
		
		void gainNecessary(NormalProtocolDecoder decoder,ServerEndPoint endPoint,byte [] header) throws IOException{
			int sLength = header[0];
			int kLength = header[1];
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
		
		abstract boolean parse(ServletContext context,NormalProtocolDecoder decoder, ServerEndPoint endPoint,byte [] header) throws IOException;
		
	}
	
	
	private static final Decoder [] headerDecoders = new Decoder[]{
		//TEXT
		new Decoder() {
			public boolean parse(ServletContext context, NormalProtocolDecoder decoder,
					ServerEndPoint endPoint, byte[] header) throws IOException {
				
				int pLength = getPLength(header);
				
				gainNecessary(decoder,endPoint,header);

				decoder.parameters = readDataText(pLength, context,endPoint);
				
				return true;
				
			}
		},
		//STREAM
		new Decoder() {
			public boolean parse(ServletContext context, NormalProtocolDecoder decoder,
					ServerEndPoint endPoint, byte[] header) throws IOException {
				int dLength = getDLength(header);
				
				gainNecessary(decoder,endPoint,header);
				
				decoder.inputStream = readDataInputStream(dLength,endPoint);
				
				return true;
				
			}
		},
		//MULT
		new Decoder() {
			public boolean parse(ServletContext context, NormalProtocolDecoder decoder,
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
			public boolean parse(ServletContext context, NormalProtocolDecoder decoder,
					ServerEndPoint endPoint, byte[] header) throws IOException {
				return true;
				
			}
		}
		
	};
	
}
