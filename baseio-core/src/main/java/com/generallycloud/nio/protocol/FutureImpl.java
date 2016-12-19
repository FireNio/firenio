package com.generallycloud.nio.protocol;

public abstract class FutureImpl implements Future {
	
	private Object		attachment	;

	@Override
	public void attach(Object attachment) {
		this.attachment = attachment;
	}

	@Override
	public Object attachment() {
		return attachment;
	}
}
