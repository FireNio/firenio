package com.gifisan.nio.component;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.concurrent.ThreadPool;
import com.gifisan.nio.component.future.ReadFuture;

public class ReadFutureDispatcher implements ReadFutureAcceptor {
	
	private final Logger logger = LoggerFactory.getLogger(ReadFutureDispatcher.class);
	
	public void accept(final Session session, final ReadFuture future) {
		
		final NIOContext context = session.getContext();
		
		ThreadPool threadPool = context.getThreadPool();

		threadPool.dispatch(new Runnable() {
			
			public void run() {
				
				IOEventHandle eventHandle = context.getIOEventHandleAdaptor();
				
				try {
					eventHandle.accept(session, future);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
		});
	}

}
