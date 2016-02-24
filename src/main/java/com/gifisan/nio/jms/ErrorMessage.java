package com.gifisan.nio.jms;


public class ErrorMessage extends MessageImpl implements Message{
	
	public static final ErrorMessage UNAUTH_MESSAGE = new ErrorMessage(ErrorMessage.CODE_UNAUTH);
	
	public static final ErrorMessage CMD_NOT_FOUND_MESSAGE = new ErrorMessage(ErrorMessage.CODE_CMD_NOT_FOUND);
	
	public static int CODE_UNAUTH = 0;
	
	public static int CODE_CMD_NOT_FOUND = 1;
	
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
