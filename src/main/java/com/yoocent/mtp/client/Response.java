package com.yoocent.mtp.client;

import java.io.InputStream;

public class Response {
	
	public static final byte STREAM = 1;
	
	public static final byte ERROR = -1;
	
	public static final byte TEXT = 0;

	private String content = null;
	
	private InputStream inputStream = null;
	
	private byte type = 0;
	
	public Response(InputStream inputStream) {
		this.inputStream = inputStream;
		this.type = STREAM;
	}
	
	public Response(String content,byte type) {
		this.content = content;
		this.type = type;
	}
	
	public String getContent(){
		
		return content;
	}
	
	public InputStream getInputStream(){
		return inputStream;
	}
	
	public byte getType(){
		return type;
	}
	
}
