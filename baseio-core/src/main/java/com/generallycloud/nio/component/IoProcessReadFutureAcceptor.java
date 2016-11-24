package com.generallycloud.nio.component;

import com.generallycloud.nio.component.IoEventHandle.IoEventState;
import com.generallycloud.nio.protocol.ChannelReadFuture;

public class IoProcessReadFutureAcceptor extends AbstractReadFutureAcceptor{

	protected void accept(IoEventHandle eventHandle, SocketSession session, ChannelReadFuture future) {
		
		try {

			eventHandle.accept(session, future);

		} catch (Exception e) {

			eventHandle.exceptionCaught(session, future, e, IoEventState.HANDLE);
		}
	}
	
}
