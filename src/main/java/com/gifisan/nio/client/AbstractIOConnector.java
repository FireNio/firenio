package com.gifisan.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.AbstractIOService;
import com.gifisan.nio.component.AbstractSelectorLoop;
import com.gifisan.nio.component.IOConnector;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.server.configuration.ServerConfiguration;

public abstract class AbstractIOConnector extends AbstractIOService implements IOConnector {

	private AtomicBoolean		connected		= new AtomicBoolean(false);
	private Selector			selector		= null;
	private InetSocketAddress	serverAddress	= null;
	private String				machineType	= "M";
	protected Session			session		= null;
	private long				beatPacket	= 0;

	protected abstract AbstractSelectorLoop getSelectorLoop();

	public void close() throws IOException {

		Thread thread = Thread.currentThread();

		if (getSelectorLoop().isMonitor(thread)) {
			throw new IllegalStateException("not allow to close on future callback");
		}

		if (connected.compareAndSet(true, false)) {
			LifeCycleUtil.stop(context);

			doClose();
		}
	}

	protected abstract void doClose();

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {

			if (context == null) {
				throw new IllegalArgumentException("null nio context");
			}

			try {
				context.start();
			} catch (Exception e) {
				throw new IOException(e);
			}

			ServerConfiguration configuration = context.getServerConfiguration();

			String SERVER_HOST = configuration.getSERVER_HOST();

			int SERVER_PORT = configuration.getSERVER_TCP_PORT();

			this.serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);

			this.connect(serverAddress);

			this.startComponent(context, selector);
		}
	}

	protected abstract void connect(InetSocketAddress address);

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
	
	public long getBeatPacket() {
		return beatPacket;
	}

	public void setBeatPacket(long beatPacket) {
		this.beatPacket = beatPacket;
	}

	public String getMachineType() {
		return machineType;
	}

	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}
}
