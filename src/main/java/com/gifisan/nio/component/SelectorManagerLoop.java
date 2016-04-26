package com.gifisan.nio.component;

import java.nio.channels.Selector;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.AbstractLifeCycleListener;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.LifeCycleListener;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.server.NIOContext;

public class SelectorManagerLoop extends AbstractLifeCycle implements Runnable {

	private Logger			logger			= LoggerFactory.getLogger(SelectorManagerLoop.class);
	private SelectorManager	selectorManager	= null;
	private Thread			looper			= null;
	private Selector		selector			= null;

	public SelectorManagerLoop(NIOContext context,Selector selector) {
		this.selector = selector;
		this.selectorManager = new SelectorManager(context,selector);
	}

	public void run() {
		for (;isRunning();) {
			
			try {
				
				this.selectorManager.accept(1000);
				
			} catch (Throwable e) {
				
				logger.error(e.getMessage(),e);
			}
		}
	}

	protected void doStart() throws Exception {
		
		this.addLifeCycleListener(new EventListener());
		
		this.looper = new Thread(this, "Selector@" + this.selectorManager.toString());
	}

	protected void doStop() throws Exception {
		this.selector.wakeup();
		this.selector.close();
	}

	private class EventListener extends AbstractLifeCycleListener implements LifeCycleListener {

		public void lifeCycleStarted(LifeCycle lifeCycle) {
			looper.start();
		}

		public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {
			logger.error(exception.getMessage(),exception);
		}

	}

}
