package com.gifisan.mtp.component;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.mtp.common.DateUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;

public class NormalProtocolDecoder implements ProtocolDecoder {

	private MTPRequestInputStream 	inputStream 		= null;
	private String  				content 			= null;
	private String 				serviceName 		= null;
	private boolean 				beat 			= false;

	public MTPRequestInputStream getInputStream() {
		return inputStream;
	}
	
	public String getContent() {
		return content;
	}

	public String getServiceName() {
		return serviceName;
	}
	
	private void reset(){
		this.beat = false;
		this.content = null;
		this.inputStream = null;
	}
	
	public boolean decode(ServletContext context,ServerEndPoint endPoint) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocate(1);
		int length = endPoint.read(buffer);
		
//		if (length == -1) {
//			return false;
//		}
//		
//		if (length == 0) {
//			return true;
//		}
		
		if (length < 1) {
			endPoint.endConnect();
			return false;
		}

		byte type = buffer.get(0);
		
		if (type == 3) {
			System.out.println(">>read beat................."+DateUtil.now());
			this.beat = true;
			return true;
		}
		
		this.reset();
		
		buffer = ByteBuffer.allocate(8);
		length = endPoint.readHead(buffer);
		
		byte [] header = buffer.array();
		
		if (length < 8) {
			// 如果一次读取不到8个byte
			// 这样的连接持续下去也是无法进行业务操作
			// 还有一种情况是有人在恶意攻击服务器
			endPoint.endConnect();
			return false;
		}
		
		if (type < 3) {
			return headerDecoders[type].parse(context, this, endPoint, header);
		}else{
			endPoint.endConnect();
			return false;
		}
	}
	
	public boolean isBeat() {
		return this.beat;
	}

	private static abstract class Decoder{
		
		int getContentLength(byte [] header){
			int v0 = (header[1] & 0xff);
			int v1 = (header[2] & 0xff) << 8;  
			int v2 = (header[3] & 0xff) << 16;  
			return v0 | v1 | v2;
		}
		
		String readDataText(int contentLength,ServletContext context,ServerEndPoint endPoint) throws IOException{
			if (contentLength > 0) {
				ByteBuffer buffer = endPoint.completeRead(contentLength);
				byte [] bytes = buffer.array();
				String content = new String(bytes,context.getEncoding());
				return content;
			}
			return null;
		}
		
		int getStreamLength(byte [] header){
			int v0 = (header[4]  & 0xff);  
			int v1 = (header[5]  & 0xff) << 8;  
			int v2 = (header[6] & 0xff) << 16;  
			int v3 = (header[7] & 0xff) << 24; 
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
			int kLength = header[0];
			ByteBuffer buffer = endPoint.read(kLength);
			byte [] bytes = buffer.array();
			String serviceName = new String(bytes,0,kLength);
			
			if (StringUtil.isNullOrBlank(serviceName)) {
				throw new EOFException("service key is empty");
			}
			
			decoder.serviceName = serviceName;
		}
		
		abstract boolean parse(ServletContext context,NormalProtocolDecoder decoder, ServerEndPoint endPoint,byte [] header) throws IOException;
		
	}
	
	
	private static final Decoder [] headerDecoders = new Decoder[]{
		//TEXT
		new Decoder() {
			public boolean parse(ServletContext context, NormalProtocolDecoder decoder,
					ServerEndPoint endPoint, byte[] header) throws IOException {
				
				int contentLength = getContentLength(header);
				
				gainNecessary(decoder,endPoint,header);

				decoder.content = readDataText(contentLength, context,endPoint);
				
				return true;
				
			}
		},
		//STREAM
		new Decoder() {
			public boolean parse(ServletContext context, NormalProtocolDecoder decoder,
					ServerEndPoint endPoint, byte[] header) throws IOException {
				int dLength = getStreamLength(header);
				
				gainNecessary(decoder,endPoint,header);
				
				decoder.inputStream = readDataInputStream(dLength,endPoint);
				
				return true;
				
			}
		},
		//MULT
		new Decoder() {
			public boolean parse(ServletContext context, NormalProtocolDecoder decoder,
					ServerEndPoint endPoint, byte[] header) throws IOException {
				int contentLength = getContentLength(header);
				int streamLength = getStreamLength(header);
				
				gainNecessary(decoder,endPoint,header);
				
				decoder.content = readDataText(contentLength, context,endPoint);
				
				decoder.inputStream = readDataInputStream(streamLength,endPoint);
				
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
