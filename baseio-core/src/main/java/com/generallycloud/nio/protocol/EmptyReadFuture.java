package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.SocketChannelContext;

public class EmptyReadFuture extends AbstractReadFuture{

	private static EmptyReadFuture emptyReadFuture = null;
	
	public static EmptyReadFuture getEmptyReadFuture(SocketChannelContext context){
		
		if (emptyReadFuture == null) {
			synchronized (EmptyReadFuture.class) {
				if (emptyReadFuture == null) {
					emptyReadFuture = new EmptyReadFuture(context);
				}
			}
		}
		
		return emptyReadFuture;
	}
	
	protected EmptyReadFuture(SocketChannelContext context) {
		super(context);
	}

	public void release() {
		
	}
	
}
