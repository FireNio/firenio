package com.gifisan.nio.component;

public interface Message {

	public abstract String getServiceName();

	public abstract void setServiceName(String serviceName);

	public abstract String getText();

	public abstract void setText(String text);

	public abstract Parameters getParameters();

}
