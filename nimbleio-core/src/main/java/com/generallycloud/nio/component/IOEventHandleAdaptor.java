package com.generallycloud.nio.component;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.LifeCycle;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.ThreadPool;
import com.generallycloud.nio.component.protocol.ReadFuture;

public abstract class IOEventHandleAdaptor extends AbstractLifeCycle implements IOEventHandle, LifeCycle {

	private Logger		logger	= LoggerFactory.getLogger(IOEventHandleAdaptor.class);

	private ThreadPool	threadPool;
	
	public void exceptionCaught(Session session, ReadFuture future, Exception cause, IOEventState state) {
		logger.info("exception, {}", cause);
	}

	public void futureSent(Session session, ReadFuture future) {
		
	}

	public void accept(final Session session, final ReadFuture future) throws Exception {

		threadPool.dispatch(new Runnable() {

			public void run() {

				try {

					acceptAlong(session, future);

				} catch (Exception e) {

					logger.error(e.getMessage(), e);

					exceptionCaught(session, future, e, IOEventState.HANDLE);
				}
			}
		});
	}

	public void acceptAlong(Session session, ReadFuture future) throws Exception {
		logger.info("future accept,{}", future);
	}

	protected void doStart() throws Exception {

	}

	protected void doStop() throws Exception {

	}

	public void setContext(NIOContext context) {
		this.threadPool = context.getThreadPool();
	}
}
