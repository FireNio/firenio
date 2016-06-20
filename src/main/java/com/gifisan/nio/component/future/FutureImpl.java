package com.gifisan.nio.component.future;


public class FutureImpl implements Future {

	protected String	serviceName	= null;
	protected String	text			= null;
	protected Integer	futureID		= null;
	private Object		attachment	= null;

	public String getServiceName() {
		return serviceName;
	}

	public String getText() {
		return text;
	}

	public void attach(Object attachment) {
		this.attachment = attachment;
	}

	public Object attachment() {
		return attachment;
	}
	
	public Integer getFutureID() {
		return futureID;
	}

}
