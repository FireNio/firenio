package com.gifisan.nio.component;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.concurrent.ThreadPool;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.WriteFuture;

public abstract class IOEventHandleAdaptor extends AbstractLifeCycle implements IOEventHandle, LifeCycle {

	private Logger		logger	= LoggerFactory.getLogger(IOEventHandleAdaptor.class);

	private ThreadPool	threadPool;

	public void exceptionCaughtOnRead(Session session, ReadFuture future, Exception cause) {
		logger.info("exception,{}", cause);
	}

	public void exceptionCaughtOnWrite(Session session, ReadFuture readFuture, WriteFuture writeFuture, Exception cause) {
		logger.info("exception,{}", cause);
	}

	public void futureSent(Session session, WriteFuture future) {
		logger.info("future sent,{}", future);
	}

	public void accept(final Session session, final ReadFuture future) throws Exception {

		threadPool.dispatch(new Runnable() {

			public void run() {

				try {

					acceptAlong(session, future);

				} catch (Exception e) {

					logger.error(e.getMessage(), e);

					exceptionCaughtOnWrite(session, future, null, e);
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
