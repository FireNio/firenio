package com.likemessage.bean;

public class T_MESSAGE {

	private Integer	messageID		= 0;
	private Integer	toUserID		= 0;
	private Integer	fromUserID	= 0;
	private long		msgDate		= 0;
	private int		msgType		= 0;
	private String		message		= null;
	private boolean	isSend		= false;
	private boolean	deleted		= false;

	public Integer getMessageID() {
		return messageID;
	}

	public void setMessageID(Integer messageID) {
		this.messageID = messageID;
	}

	public Integer getToUserID() {
		return toUserID;
	}

	public void setToUserID(Integer toUserID) {
		this.toUserID = toUserID;
	}

	public Integer getFromUserID() {
		return fromUserID;
	}

	public void setFromUserID(Integer fromUserID) {
		this.fromUserID = fromUserID;
	}

	public long getMsgDate() {
		return msgDate;
	}

	public void setMsgDate(long msgDate) {
		this.msgDate = msgDate;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSend() {
		return isSend;
	}

	public void setSend(boolean isSend) {
		this.isSend = isSend;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

}
