package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.AbstractLifeCycleListener;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.LifeCycleListener;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.ThreadUtil;

public abstract class AbstractSelectorLoop extends AbstractLifeCycle implements SelectorLoop {

	private Logger				logger			= LoggerFactory.getLogger(AbstractSelectorLoop.class);
	private boolean			working			= false;
	protected Thread			looper			= null;
	protected Selector			selector			= null;

	protected void acceptException(SelectionKey selectionKey, IOException exception) {

		SelectableChannel channel = selectionKey.channel();

		Object attachment = selectionKey.attachment();

		if (isEndPoint(attachment)) {

			EndPoint endPoint = (EndPoint) attachment;
			
			endPoint.endConnect();

			CloseUtil.close(endPoint);
		}

		CloseUtil.close(channel);

		selectionKey.cancel();

		logger.error(exception.getMessage(), exception);
	}

	private boolean isEndPoint(Object object) {
		return object != null && (object.getClass() == DefaultTCPEndPoint.class || object instanceof EndPoint);
	}

	public void run() {
		for (; isRunning();) {

			try {
				
				working = true;

				Selector selector = this.selector;

				int selected = selector.select(1000);

				if (selected < 1) {
					
					working = false;
					
					continue;
				}

				Set<SelectionKey> selectionKeys = selector.selectedKeys();

				Iterator<SelectionKey> iterator = selectionKeys.iterator();

				for (; iterator.hasNext();) {

					SelectionKey selectionKey = iterator.next();

					iterator.remove();

					accept(selectionKey);

				}
				
				working = false;

			} catch (Throwable e) {

				logger.error(e.getMessage(), e);
				
				working = false;
			}
		}
	}

	public abstract void accept(SelectionKey selectionKey) throws IOException ;

	protected void doStart() throws Exception {

		this.addLifeCycleListener(new EventListener());

		this.looper = getLooperThread();
	}
	
	protected abstract Thread getLooperThread();

	protected void doStop() throws Exception {

		this.selector.wakeup();
		
		for(;working;){
			
			ThreadUtil.sleep(8);
		}
		
		this.selector.close();
	}

	private class EventListener extends AbstractLifeCycleListener implements LifeCycleListener {

		public void lifeCycleStarted(LifeCycle lifeCycle) {
			looper.start();
		}

		public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {
			logger.error(exception.getMessage(), exception);
		}
	}
	
	public boolean isMonitor(Thread thread){
		return this.looper == thread;
	}
}
