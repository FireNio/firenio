package com.gifisan.mtp.component;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.InnerEndPoint;
import com.gifisan.mtp.server.context.ServletContext;

public class MTPParser {

	private InnerEndPoint endPoint 				= null;
	private MTPRequestInputStream inputStream 	= null;
	private JSONObject parameters 				= null;
	private boolean parseComplete 				= false;
	private String serviceName 					= null;
	private String sessionID 					= null;

	public MTPParser(InnerEndPoint endPoint) {
		this.endPoint = endPoint;
	}
	
	public InnerEndPoint getEndPoint() {
		return endPoint;
	}

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
	
	public boolean isParseComplete() {
		return parseComplete;
	}
	
	public void parse(ServletContext context) throws IOException{
		this.parse(context, endPoint);
	}
	
	private void parse(ServletContext context,InnerEndPoint endPoint) throws IOException{
		byte [] header = endPoint.readHead();
		if (header == null) {
			this.parseComplete = true;
			return;
		}
		
		byte type = header[0];
		if (type < 5) {
			headerParsers[type].parse(context, this, endPoint, header);
		}else{
			this.parseComplete = true;
		}
	}
	
	
	private static abstract class Parser{
		
		int getPLength(byte [] header){
			int v0 = (header[5] & 0xff);
		    int v1 = (header[6] & 0xff) << 8;  
		    int v2 = (header[7] & 0xff) << 16;  
		    return v0 | v1 | v2;
		}
		
		JSONObject readDataText(int pLength,ServletContext context,InnerEndPoint endPoint) throws IOException{
			if (pLength > 0) {
				ByteBuffer buffer = endPoint.completeRead(pLength);
				byte [] bytes = buffer.array();
				String content = new String(bytes,context.getEncoding());
				return JSONObject.parseObject(content);
			}
			return null;
		}
		
		int getDLength(byte [] header){
			int v0 = (header[8]  & 0xff);  
		    int v1 = (header[9]  & 0xff) << 8;  
		    int v2 = (header[10] & 0xff) << 16;  
		    int v3 = (header[11] & 0xff) << 24; 
		    return v0 | v1 | v2 | v3;
		}
		
		
		MTPRequestInputStream readDataInputStream(int dLength,InnerEndPoint endPoint) throws IOException{
			if (dLength == 0) {
				return null;
			}
			
			MTPRequestInputStream inputStream = new MTPRequestInputStream(endPoint,dLength);
			endPoint.setMTPRequestInputStream(inputStream);
			return inputStream;
		}
		
		void gainNecessary(MTPParser parser,InnerEndPoint endPoint,int kLength,int sLength) throws IOException{
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
			
			parser.serviceName = serviceName;
			
			parser.sessionID = sessionID;
		}
		
		abstract void parse(ServletContext context,MTPParser parser, InnerEndPoint endPoint,byte [] header) throws IOException;
		
	}
	
	
	private static final Parser [] headerParsers = new Parser[]{
		//BEAT
		new Parser() {
			public void parse(ServletContext context, MTPParser parser,
					InnerEndPoint endPoint, byte[] header) throws IOException {
				parser.parseComplete = true;
				
			}
		},
		//TEXT
		new Parser() {
			public void parse(ServletContext context, MTPParser parser,
					InnerEndPoint endPoint, byte[] header) throws IOException {
				int sLength = header[1];
				int kLength = header[2];
				int pLength = getPLength(header);
				
				gainNecessary(parser,endPoint,kLength, sLength);

				parser.parameters = readDataText(pLength, context,endPoint);
				
				parser.parseComplete = true;
				
			}
		},
		//STREAM
		new Parser() {
			public void parse(ServletContext context, MTPParser parser,
					InnerEndPoint endPoint, byte[] header) throws IOException {
				int sLength = header[1];
				int kLength = header[2];
				int dLength = getDLength(header);
				
				gainNecessary(parser,endPoint,kLength, sLength);
				
				parser.inputStream = readDataInputStream(dLength,endPoint);
				
				parser.parseComplete = true;
				
			}
		},
		//MULT
		new Parser() {
			public void parse(ServletContext context, MTPParser parser,
					InnerEndPoint endPoint, byte[] header) throws IOException {
				int sLength = header[1];
				int kLength = header[2];
				int pLength = getPLength(header);
				int dLength = getDLength(header);
				
				gainNecessary(parser,endPoint,kLength, sLength);
				
				parser.parameters = readDataText(pLength, context,endPoint);
				
				parser.inputStream = readDataInputStream(dLength,endPoint);
				
				parser.parseComplete = true;
				
			}
		}
		
	};
	
}
