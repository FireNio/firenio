package com.gifisan.nio.server.selector;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.AbstractLifeCycleListener;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.LifeCycleListener;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.server.ServerContext;

public class SelectorManagerTask extends AbstractLifeCycle implements Runnable {

	private Logger			logger			= LoggerFactory.getLogger(SelectorManagerTask.class);
	private SelectorManager	selectorManager	= null;
	private Thread			task				= null;
	private boolean		working			= false;

	public SelectorManagerTask(ServerContext context) {
		this.selectorManager = new SelectorManager(context);
	}

	public void run() {
		while (isRunning()) {
			try {
				this.working = true;
				selectorManager.accept(1000);
				this.working = false;
			} catch (Throwable e) {
				logger.error(e.getMessage(),e);
			}
		}
	}

	protected void doStart() throws Exception {
		this.selectorManager.start();
		this.addLifeCycleListener(new EventListener());
		this.task = new Thread(this, "Selector@" + this.selectorManager.getSelector());
	}

	public void register(ServerSocketChannel serverSocketChannel) throws ClosedChannelException {
		selectorManager.register(serverSocketChannel);
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(selectorManager);
		while (working) {
			Thread.sleep(1000);
		}
		Selector selector = selectorManager.getSelector();
		selector.close();
	}

	private class EventListener extends AbstractLifeCycleListener implements LifeCycleListener {

		public void lifeCycleStarted(LifeCycle lifeCycle) {
			task.start();
		}

		public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {
			logger.error(exception.getMessage(),exception);
		}

	}

}
