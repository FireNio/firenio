package com.yoocent.mtp.component;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.alibaba.fastjson.JSONObject;
import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.server.EndPoint;
import com.yoocent.mtp.server.InnerEndPoint;
import com.yoocent.mtp.server.context.ServletContext;

public class MTPParser {

	private InnerEndPoint endPoint = null;

	private MTPRequestInputStream inputStream = null;
	
	private JSONObject parameters = null;

	private boolean parseComplete;

	private String serviceKey = null;
	
	private String sessionID = null;
	
	private static final byte TYPE_STREAM = 2;

	private static final byte TYPE_MULT = 3;

	private static final byte TYPE_TEXT = 1;
	
	private static final byte TYPE_ZERO = 0;

	public MTPParser(InnerEndPoint endPoint) {
		this.endPoint = endPoint;
	}
	
	public EndPoint getEndPoint() {
		return endPoint;
	}

	public MTPRequestInputStream getInputStream() {
		return inputStream;
	}

	public JSONObject getParameters() {
		return parameters;
	}

	public String getServiceKey() {
		return serviceKey;
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
		switch (type) {
		case TYPE_TEXT:
			parseTYPE_TEXT(context, endPoint, header);
			break;
		case TYPE_MULT:
			parseTYPE_MULT(context, endPoint, header);
			break;
		case TYPE_STREAM:
			parseTYPE_DATA(context, endPoint, header);
			break;
		case TYPE_ZERO:
			parseTYPE_ZERO(context, endPoint, header);
			break;
		default:
			break;
		}
	}
	
	private int getDLength(byte [] header){
		int v0 = (header[8]  & 0xff);  
	    int v1 = (header[9]  & 0xff) << 8;  
	    int v2 = (header[10] & 0xff) << 16;  
	    int v3 = (header[11] & 0xff) << 24; 
	    return v0 | v1 | v2 | v3;
	}
	
	private int getPLength(byte [] header){
		int v0 = (header[5] & 0xff);
	    int v1 = (header[6] & 0xff) << 8;  
	    int v2 = (header[7] & 0xff) << 16;  
	    return v0 | v1 | v2;
	}
	
	private void parseTYPE_DATA(ServletContext context,EndPoint endPoint,byte [] header) throws IOException{
		
	    int sLength = header[1];
		int kLength = header[2];
		int dLength = getDLength(header);
		
		this.gainNecessary(kLength, sLength);
		
		this.inputStream = readDataInputStream(dLength);
		
		this.parseComplete = true;
		
	}
	
	private String parseTYPE_ZERO(ServletContext context,EndPoint endPoint,byte [] header){
		this.parseComplete = true;
		return null;
	}
	
	private void parseTYPE_MULT(ServletContext context,EndPoint endPoint,byte [] header) throws IOException{
		
		int sLength = header[1];
		int kLength = header[2];
		int pLength = getPLength(header);
		int dLength = getDLength(header);
		
		this.gainNecessary(kLength, sLength);
		
		this.parameters = readDataText(pLength, context);
		
		this.inputStream = readDataInputStream(dLength);
		
		this.parseComplete = true;
		
	}
	
	private void parseTYPE_TEXT(ServletContext context,EndPoint endPoint,byte [] header) throws IOException{
		
		int sLength = header[1];
		int kLength = header[2];
		int pLength = getPLength(header);
		
		this.gainNecessary(kLength, sLength);

		this.parameters = readDataText(pLength, context);
		
		this.parseComplete = true;
		
	}
	
	private MTPRequestInputStream readDataInputStream(int dLength) throws IOException{
		if (dLength == 0) {
			return null;
		}
		
		MTPRequestInputStream inputStream = new MTPRequestInputStream(endPoint,dLength);
		endPoint.setMTPRequestInputStream(inputStream);
		return inputStream;
	}
	
	//TODO 
	private JSONObject readDataText(int pLength,ServletContext context) throws IOException{
		if (pLength > 0) {
			ByteBuffer buffer = endPoint.completeRead(pLength);
			byte [] bytes = buffer.array();
			String content = new String(bytes,context.getEncoding());
			return JSONObject.parseObject(content);
		}
		return null;
	}

	private void gainNecessary(int kLength,int sLength) throws IOException{
		ByteBuffer buffer = endPoint.read(sLength+kLength);
		byte [] bytes = buffer.array();
		String sessionID = new String(bytes,0,sLength);
		String serviceKey = new String(bytes,sLength,kLength);
		
		if (StringUtil.isBlankOrNull(serviceKey)) {
			throw new EOFException("service key is empty");
		}
		
		if (StringUtil.isBlankOrNull(sessionID)) {
			throw new EOFException("sessionID is empty");
		}
		
		this.serviceKey = serviceKey;
		
		this.sessionID = sessionID;
	}
	
}
