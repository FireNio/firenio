package com.generallycloud.nio.component;

import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.protocol.ChannelReadFuture;

public class EventLoopReadFutureAcceptor extends AbstractReadFutureAcceptor{

	protected void accept(IOEventHandle eventHandle, Session session, ChannelReadFuture future) {
		
		EventLoop eventLoop = session.getEventLoop();

		eventLoop.dispatch(new Runnable() {

			public void run() {

				try {

					eventHandle.accept(session, future);

				} catch (Exception e) {

					eventHandle.exceptionCaught(session, future, e, IOEventState.HANDLE);
				}
			}
		});
	}
	
	
}
