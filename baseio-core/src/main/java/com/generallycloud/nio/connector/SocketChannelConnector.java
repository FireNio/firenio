package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.MessageFormatter;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.UnsafeSocketSession;
import com.generallycloud.nio.component.concurrent.Waiter;

//FIXME 重连的时候不需要重新加载BaseContext
public final class SocketChannelConnector extends AbstractChannelConnector {

	private SocketChannelContext	context;

	private UnsafeSocketSession	session;

	private Waiter<Object> waiter;

	public SocketChannelConnector(SocketChannelContext context) {
		this.context = context;
	}

	@Override
	public SocketSession connect() throws IOException {

		this.waiter = new Waiter<Object>();
		
		this.session = null;
		
		this.service();

		return getSession();
	}

	@Override
	protected void connect(InetSocketAddress socketAddress) throws IOException {

		((SocketChannel) this.selectableChannel).connect(socketAddress);

		initSelectorLoops();

		if (waiter.await(getTimeout())) {

			CloseUtil.close(this);

			throw new TimeoutException("connect to " + socketAddress.toString() + " time out");
		}

		Object o = waiter.getPayload();

		if (o instanceof Exception) {
			
			CloseUtil.close(this);

			Exception t = (Exception) o;

			throw new TimeoutException(MessageFormatter.format("connect faild,connector:[{}],nested exception is {}",
					socketAddress, t.getMessage()), t);
		}
	}

	protected void finishConnect(UnsafeSocketSession session, Exception exception) {

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
	
	@Override
	protected boolean canSafeClose() {
		return session == null || (!session.inSelectorLoop() && !session.getEventLoop().inEventLoop());
	}

	@Override
	protected void fireSessionOpend() {
		session.fireOpend();
	}

	@Override
	public SocketChannelContext getContext() {
		return context;
	}

	@Override
	public SocketSession getSession() {
		return session;
	}

	@Override
	protected void initselectableChannel() throws IOException {

		this.selectableChannel = SocketChannel.open();

		this.selectableChannel.configureBlocking(false);
	}

	@Override
	protected SelectorLoop newSelectorLoop(SelectorLoop[] selectorLoops) throws IOException {
		return new ClientSocketChannelSelectorLoop(this, selectorLoops);
	}

}
