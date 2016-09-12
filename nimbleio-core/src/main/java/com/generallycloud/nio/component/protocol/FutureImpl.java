package com.generallycloud.nio.component.protocol;

public abstract class FutureImpl implements Future {
	
	private Object		attachment	;

	public void attach(Object attachment) {
		this.attachment = attachment;
	}

	public Object attachment() {
		return attachment;
	}
}
