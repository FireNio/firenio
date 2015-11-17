package com.yoocent.mtp.server;

public interface InnerRequest extends Request{

	public abstract boolean isCloseCommand();
	
	public abstract void setCloseCommand(boolean close);
	
}
