package com.gifisan.nio.client;

import com.gifisan.nio.component.future.ReadFuture;

public class ListenOnReadFuture implements OnReadFuture {

	private MessageBus	messageBus	= null;

	public ListenOnReadFuture(ClientSession session) {
		this.messageBus = ((ProtectedClientSession)session).getMessageBus();
	}

	public void onResponse(ClientSession session, ReadFuture future) {
		messageBus.offer(future);
	}

}
