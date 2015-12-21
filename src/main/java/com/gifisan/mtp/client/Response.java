package com.gifisan.mtp.client;

import java.io.InputStream;

public class Response {
	
	public static final int STREAM 	= 1;
	public static final int TEXT  	= 0;
	private String content 				= null;
	private InputStream inputStream 	= null;
	private byte type 					= TEXT;
	
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
	
	public int getType(){
		return type;
	}
	
}
