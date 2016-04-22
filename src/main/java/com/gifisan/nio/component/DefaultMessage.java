package com.gifisan.nio.component;

public class DefaultMessage implements Message{

	private String	 serviceName;

	private String text;
	
	private Parameters parameters;
	
	public DefaultMessage(String serviceName, String text) {
		this.serviceName = serviceName;
		this.text = text;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Parameters getParameters() {
		if (parameters == null) {
			parameters = new DefaultParameters(text);
		}
		return parameters;
	}
	
}
