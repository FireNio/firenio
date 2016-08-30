package com.generallycloud.nio.extend.plugin.jms;

import com.alibaba.fastjson.JSONObject;

public class ErrorMessage extends BasicMessage implements Message {

	public static int				CODE_UNAUTH			;
	public static int				CODE_CMD_NOT_FOUND		= 1;
	public static int 				CODE_IOEXCEPTION 		= 2;
	private int					code					;
	public static final ErrorMessage	UNAUTH_MESSAGE		= new ErrorMessage(CODE_UNAUTH);
	public static final ErrorMessage	CMD_NOT_FOUND_MESSAGE	= new ErrorMessage(CODE_CMD_NOT_FOUND);
	public static final ErrorMessage	IOEXCEPTION			= new ErrorMessage(CODE_IOEXCEPTION);

	public ErrorMessage(int code) {
		super(null, null);
		this.code = code;
	}

	public int getMsgType() {
		return Message.TYPE_ERROR;
	}

	public int getCode() {
		return code;
	}

	public String toString() {
		return new StringBuilder(24)
			.append("{\"msgType\":0,\"code\":")
			.append(code)
			.append("}")
			.toString();
	}

	public static void main(String[] args) {
		
		
		
		ErrorMessage message = new ErrorMessage(CODE_CMD_NOT_FOUND);
		
		System.out.println(JSONObject.toJSON(message).toString());
		System.out.println(message.toString());
	}
	
	
	
}
