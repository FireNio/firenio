package com.gifisan.mtp.server;

public interface InnerRequest extends Request{

	public abstract boolean isCloseCommand();
	
	public abstract void setCloseCommand(boolean close);
	
}
