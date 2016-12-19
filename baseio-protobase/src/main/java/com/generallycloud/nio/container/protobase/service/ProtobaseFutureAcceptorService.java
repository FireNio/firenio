package com.generallycloud.nio.container.protobase.service;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class ProtobaseFutureAcceptorService extends FutureAcceptorService {

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {
		this.doAccept(session, (ProtobaseReadFuture) future);
	}

	protected abstract void doAccept(SocketSession session, ProtobaseReadFuture future) throws Exception;

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {

		if (state == IoEventState.HANDLE) {

			ProtobaseReadFuture f = (ProtobaseReadFuture) future;

			f.write(cause.getClass().getName() + ":" + cause.getMessage());
			
			session.flush(future);
		}
	}
}
