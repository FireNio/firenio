package com.generallycloud.nio.component;

import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.protocol.ChannelReadFuture;

public class IoProcessReadFutureAcceptor extends AbstractReadFutureAcceptor{

	protected void accept(IOEventHandle eventHandle, Session session, ChannelReadFuture future) {
		
		try {

			eventHandle.accept(session, future);

		} catch (Exception e) {

			eventHandle.exceptionCaught(session, future, e, IOEventState.HANDLE);
		}
	}
	
}
