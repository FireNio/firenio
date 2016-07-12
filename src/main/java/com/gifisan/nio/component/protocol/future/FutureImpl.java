package com.gifisan.nio.component.protocol.future;


public class FutureImpl implements Future {
	
	private Object		attachment	;

	public void attach(Object attachment) {
		this.attachment = attachment;
	}

	public Object attachment() {
		return attachment;
	}
}
