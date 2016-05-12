package com.gifisan.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Connector;
import com.gifisan.nio.component.DefaultUDPEndPoint;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.UDPSelectorLoop;

public class ClientUDPConnector implements Connector {

	private AtomicBoolean		connected			= new AtomicBoolean(false);
	private ClientContext		context			= null;
	private UDPEndPoint			endPoint			= null;
	private Logger				logger			= LoggerFactory.getLogger(ClientUDPConnector.class);
	private Selector			selector			= null;
	private UDPSelectorLoop		selectorLoop		= null;

	protected UDPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

	public ClientUDPConnector(ClientContext context) {
		this.context = context;
	}

	public void close() throws IOException {

		Thread thread = Thread.currentThread();

		if (selectorLoop.isMonitor(thread)) {
			throw new IllegalStateException("not allow to close on future callback");
		}

		if (connected.compareAndSet(true, false)) {
			LifeCycleUtil.stop(selectorLoop);
			CloseUtil.close(endPoint);
		}
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {

			this.connect0();

			try {

				this.selectorLoop.start();
			} catch (Exception e) {
				DebugUtil.debug(e);
			}
		}
	}

	private void connect0() throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.configureBlocking(false);
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
		channel.connect(getInetSocketAddress());
		this.endPoint = new DefaultUDPEndPoint(context, channel);
		this.selectorLoop = new UDPSelectorLoop(context, selector);
	}
	
	private InetSocketAddress getInetSocketAddress() {
		return new InetSocketAddress(context.getServerHost(), context.getServerPort()+1);
	}

	public ClientContext getContext() {
		return context;
	}

	public String getServerHost() {
		return context.getServerHost();
	}

	public int getServerPort() {
		return context.getServerPort();
	}

	public String toString() {
		return "UDP:Connector@" + endPoint.toString();
	}

}
