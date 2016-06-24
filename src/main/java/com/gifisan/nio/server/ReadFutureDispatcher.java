package com.gifisan.nio.server;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.concurrent.ThreadPool;

public class ReadFutureDispatcher implements ReadFutureAcceptor {
	
	private final Logger logger = LoggerFactory.getLogger(ReadFutureDispatcher.class);
	
	public void accept(final Session session, final ReadFuture future) {
		
		final NIOContext context = session.getContext();
		
		ThreadPool threadPool = context.getThreadPool();

		threadPool.dispatch(new Runnable() {
			
			public void run() {
				
				IOEventHandle eventHandle = context.getIOEventHandle();
				
				try {
					eventHandle.accept(session, future);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
		});
	}

}
