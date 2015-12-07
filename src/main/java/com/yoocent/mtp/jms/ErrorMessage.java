package com.yoocent.mtp.jms;


public class ErrorMessage extends MessageImpl implements Message{
	
	public static final ErrorMessage UNAUTH_MESSAGE = new ErrorMessage(ErrorMessage.CODE_UNAUTH);
	
	public static int CODE_UNAUTH = 0;
	
	private int code;
	
	public ErrorMessage(int code) {
		super(null,null);
		this.code = code;
	}

	public int getMsgType() {
		return Message.TYPE_ERROR;
	}

	public int getCode() {
		return code;
	}
	
	
}
