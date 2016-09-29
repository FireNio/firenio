package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.MessageFormatter;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.configuration.ServerConfiguration;

//FIXME 重连的时候不需要重新加载NIOContext
public class SocketChannelConnector extends AbstractIOConnector {

	private SocketChannelSelectorLoop	selectorLoop;
	private EventLoopThread	selectorLoopThread;
	private Waiter<Object>	waiter	= new Waiter<Object>();

	protected void connect(NIOContext context, InetSocketAddress socketAddress) throws IOException {

		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(false);

		this.selectorLoop = new ClientTCPSelectorLoop(context, this);

		this.selectorLoop.register(context, channel);

		channel.connect(socketAddress);

		this.selectorLoopThread = new EventLoopThread(selectorLoop, getServiceDescription() + "(selector)");

		this.selectorLoopThread.start();

		if (waiter.await(getTimeout())) {

			active = true;
			
			CloseUtil.close(this);

			throw new TimeoutException("connect to "+this.getServiceDescription()+" time out");
		}

		Object o = waiter.getPayload();

		if (o instanceof Exception) {

			Exception t = (Exception) o;

			throw new TimeoutException(MessageFormatter.format(
					"connect faild,connector:[{}],nested exception is {}", this.getServiceDescription(),
					t.getMessage()), t);
		}
	}

	protected void finishConnect(Session session, IOException exception) {

		if (exception == null) {

			this.session = session;

			this.waiter.setPayload(null);

			if (waiter.isTimeouted()) {
				CloseUtil.close(this);
			}
		} else {

			this.waiter.setPayload(exception);
		}
	}

	public InetSocketAddress getServerSocketAddress() {
		return this.serverAddress;
	}

	protected EventLoopThread getSelectorLoopThread() {
		return selectorLoopThread;
	}

	protected int getSERVER_PORT(ServerConfiguration configuration) {
		return configuration.getSERVER_TCP_PORT();
	}

	protected void setIOService(NIOContext context) {
		context.setTCPService(this);
	}

	protected void doClose() {

		LifeCycleUtil.stop(selectorLoopThread);

		CloseUtil.close(session);
	}

	public String getServiceDescription() {
		return "TCP:" + serverAddress.toString();
	}

}
