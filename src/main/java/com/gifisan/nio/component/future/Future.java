package com.gifisan.nio.component.future;


public interface Future {

	public abstract Integer getFutureID();
	
	public abstract String getServiceName() ;
	
	public abstract String getText();
	
	public abstract void attach(Object attachment);

	public abstract Object attachment();
}
