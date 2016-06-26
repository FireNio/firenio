package com.gifisan.nio.component.future;


public class FutureImpl implements Future {

	protected String	serviceName	;
	protected String	text			;
	protected Integer	futureID		;
	private Object		attachment	;

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
