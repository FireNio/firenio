package com.generallycloud.nio.container.protobase.service;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class BaseFutureAcceptorService extends FutureAcceptorService {

	public void accept(SocketSession session, ReadFuture future) throws Exception {
		this.doAccept(session, (BaseReadFuture) future);
	}

	protected abstract void doAccept(SocketSession session, BaseReadFuture future) throws Exception;

	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {

		if (state == IoEventState.HANDLE) {

			BaseReadFuture f = (BaseReadFuture) future;

			f.write(cause.getClass() + ":" + cause.getMessage());

		}
	}
}
