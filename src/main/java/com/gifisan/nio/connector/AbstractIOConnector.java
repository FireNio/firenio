package com.gifisan.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.AbstractIOService;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public abstract class AbstractIOConnector extends AbstractIOService implements IOConnector {

	private AtomicBoolean		connected		= new AtomicBoolean(false);
	private String				machineType	= "M";
	protected InetSocketAddress	serverAddress	;
	protected Selector			selector		;
	protected Session			session		;

	protected abstract UniqueThread getSelectorLoopThread();

	public void close() throws IOException {

		Thread thread = Thread.currentThread();

		if (getSelectorLoopThread().isMonitor(thread)) {
			throw new IllegalStateException("not allow to close on future callback");
		}

		if (connected.compareAndSet(true, false)) {

			stopComponent(context, selector);
			
			CloseUtil.close(selector);
			
			LifeCycleUtil.stop(context);
		}
	}
	
	protected abstract InetSocketAddress getLocalSocketAddress();

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {

			if (context == null) {
				throw new IllegalArgumentException("null nio context");
			}

			LifeCycleUtil.start(context);
			
			ServerConfiguration configuration = context.getServerConfiguration();

			String SERVER_HOST = getSERVER_HOST(configuration);

			int SERVER_PORT = getSERVER_PORT(configuration);

			this.serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);

			this.connect(serverAddress);

			this.startComponent(context, selector);
			
			this.setIOService(context);
		}
	}
	
	protected abstract void connect(InetSocketAddress address) throws IOException;

	public Session getSession() {
		return session;
	}

	public String getMachineType() {
		return machineType;
	}

	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}
}
