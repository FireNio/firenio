package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.configuration.ServerConfiguration;

public abstract class AbstractChannelService implements ChannelService {

	protected InetSocketAddress		serverAddress		= null;
	protected boolean				active			= false;
	protected SelectableChannel		selectableChannel	= null;
	protected SelectorLoop[]		selectorLoops		= null;
	protected Waiter<IOException>	shutDownWaiter		= new Waiter<>();
	protected ReentrantLock			activeLock		= new ReentrantLock();

	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

	protected abstract void initselectableChannel() throws IOException;

	protected abstract SelectorLoop newSelectorLoop(SelectorLoop[] selectorLoops) throws IOException;

	protected void initSelectorLoops() throws IOException {

		ServerConfiguration configuration = getContext().getServerConfiguration();

		int core_size = configuration.getSERVER_CORE_SIZE();

		this.selectorLoops = new SelectorLoop[core_size];

		for (int i = 0; i < core_size; i++) {

			selectorLoops[i] = newSelectorLoop(selectorLoops);
		}

		for (int i = 0; i < core_size; i++) {
			try {
				selectorLoops[i].startup(getServiceDescription(i));
			} catch (Exception e) {
				if (e instanceof IOException) {
					throw (IOException) e;
				}
				throw new IOException(e.getMessage(), e);
			}
		}
	}

	protected void cancelService() {

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			// just close
			destroySelectorLoops();

			LifeCycleUtil.stop(getContext());

			shutDownWaiter.setPayload(null);

		} finally {

			active = false;

			lock.unlock();
		}

	}

	protected void service() throws IOException {

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			if (active) {
				return;
			}

			if (getContext() == null) {
				throw new IllegalArgumentException("null nio context");
			}

			getContext().setChannelService(this);

			ServerConfiguration configuration = getContext().getServerConfiguration();

			if (!(this instanceof SocketChannelAcceptor)) {
				configuration.setSERVER_CORE_SIZE(1);
			}

			LifeCycleUtil.start(getContext());

			this.initselectableChannel();

			this.initService(configuration);

			this.active = true;

		} finally {

			lock.unlock();
		}
	}

	public InetSocketAddress getServerSocketAddress() {
		return serverAddress;
	}

	protected abstract void initService(ServerConfiguration configuration) throws IOException;

	protected void destroySelectorLoops() {

		if (selectorLoops == null) {
			return;
		}

		for (int i = 0; i < selectorLoops.length; i++) {

			LifeCycleUtil.stop(selectorLoops[i]);
		}
	}

	private String getServiceDescription(int i) {
		return "io-process-" + i;
	}
}
