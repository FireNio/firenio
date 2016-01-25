package com.gifisan.mtp.server.selector;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.AbstractLifeCycleListener;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.LifeCycleListener;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.server.ServerContext;

public class SelectorManagerTask extends AbstractLifeCycle implements Runnable {

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
			} catch (IOException e) {
				e.printStackTrace();
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
			exception.printStackTrace();

		}

	}

}
