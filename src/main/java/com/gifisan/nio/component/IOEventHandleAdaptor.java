package com.gifisan.nio.component;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.concurrent.ThreadPool;
import com.gifisan.nio.component.concurrent.Waiter;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.connector.IOConnector;
import com.gifisan.nio.extend.SessionActiveSEListener;

public abstract class IOEventHandleAdaptor extends AbstractLifeCycle implements IOEventHandle, LifeCycle {

	private Logger		logger	= LoggerFactory.getLogger(IOEventHandleAdaptor.class);

	private ThreadPool	threadPool;
	
	public void exceptionCaught(Session session, ReadFuture future, Exception cause, IOEventState state) {
		logger.info("exception, {}", cause);
	}

	public void futureSent(Session session, ReadFuture future) {
		
	}

	public void accept(final Session session, final ReadFuture future) throws Exception {

		if (SessionActiveSEListener.SESSION_ACTIVE_BEAT.equals(future.getServiceName())) {
			
			IOService service = session.getContext().getTCPService();
			
			if (service instanceof IOConnector) {
				
				String session_key = SessionActiveSEListener.SESSION_ACTIVE_WAITER;
				
				Waiter<ReadFuture> waiter = (Waiter<ReadFuture>) session.getAttribute(session_key);
				
				if (waiter != null) {
					
					waiter.setPayload(future);
				}
				return;
			}
		}
		
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
