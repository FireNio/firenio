package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.AbstractIOService;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.configuration.ServerConfiguration;

public abstract class AbstractIOConnector extends AbstractIOService implements IOConnector {

	protected boolean			active		= false;
	protected ReentrantLock		activeLock	= new ReentrantLock();
	protected InetSocketAddress	serverAddress;
	protected Session			session;
	protected long			timeout		= 3000;
	
	private Logger 			logger 		= LoggerFactory.getLogger(AbstractIOConnector.class);

	protected abstract EventLoopThread getSelectorLoopThread();

	public void close() throws IOException {

		Thread thread = Thread.currentThread();

		EventLoopThread loopThread = getSelectorLoopThread();

		if ((loopThread != null && loopThread.isMonitor(thread)) 
				|| session.getEventLoop().inEventLoop(thread)) {
			ThreadUtil.execute(new Runnable() {
				
				public void run() {
					close0();
				}
			});
			return;
		}
		
		close0();
	}
	
	protected void close0(){

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			doClose();

		} finally {

			active = false;

			LifeCycleUtil.stop(context);

			lock.unlock();
		}
	}

	protected abstract void doClose();

	public Session connect() throws IOException {

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			if (active) {
				return getSession();
			}

			if (context == null) {
				throw new IllegalArgumentException("null nio context");
			}

			LifeCycleUtil.start(context);

			ServerConfiguration configuration = context.getServerConfiguration();

			String SERVER_HOST = getSERVER_HOST(configuration);

			int SERVER_PORT = getSERVER_PORT(configuration);

			this.serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);

			this.setIOService(context);

			this.connect(context, serverAddress);
			
			LoggerUtil.prettyNIOServerLog(logger, "已连接到远程服务器 @{}",getServiceDescription());
			
			((IOSession)this.getSession()).fireOpend();

			active = true;
			
			return getSession();

		} finally {

			lock.unlock();
		}
	}

	protected abstract void connect(NIOContext context, InetSocketAddress socketAddress) throws IOException;

	public Session getSession() {
		return session;
	}

	public boolean isConnected() {
		return session != null && session.isOpened();
	}

	public boolean isActive() {
		return isConnected();
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
