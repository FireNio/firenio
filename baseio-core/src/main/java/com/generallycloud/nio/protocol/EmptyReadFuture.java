package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.BaseContext;

public class EmptyReadFuture extends AbstractReadFuture{

	private static EmptyReadFuture emptyReadFuture = null;
	
	public static EmptyReadFuture getEmptyReadFuture(BaseContext context){
		
		if (emptyReadFuture == null) {
			synchronized (EmptyReadFuture.class) {
				if (emptyReadFuture == null) {
					emptyReadFuture = new EmptyReadFuture(context);
				}
			}
		}
		
		return emptyReadFuture;
	}
	
	protected EmptyReadFuture(BaseContext context) {
		super(context);
	}

	public void release() {
		
	}
	
}
