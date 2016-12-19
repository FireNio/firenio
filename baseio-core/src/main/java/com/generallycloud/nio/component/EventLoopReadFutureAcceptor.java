package com.generallycloud.nio.component;

import com.generallycloud.nio.component.IoEventHandle.IoEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.protocol.ChannelReadFuture;

public class EventLoopReadFutureAcceptor extends AbstractReadFutureAcceptor{

	@Override
	protected void accept(IoEventHandle eventHandle, SocketSession session, ChannelReadFuture future) {
		
		EventLoop eventLoop = session.getEventLoop();

		eventLoop.dispatch(new Runnable() {

			@Override
			public void run() {

				try {

					eventHandle.accept(session, future);

				} catch (Exception e) {

					eventHandle.exceptionCaught(session, future, e, IoEventState.HANDLE);
				}
			}
		});
	}
	
	
}
