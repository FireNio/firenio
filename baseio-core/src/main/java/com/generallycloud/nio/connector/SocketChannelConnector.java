package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.MessageFormatter;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.UnsafeSocketSession;
import com.generallycloud.nio.component.concurrent.Waiter;

//FIXME 重连的时候不需要重新加载BaseContext
public class SocketChannelConnector extends AbstractChannelConnector {
	
	private UnsafeSocketSession			session;

	private Waiter<Object>			waiter			= new Waiter<Object>();
	
	public SocketChannelConnector(BaseContext context) {
		super(context);
	}
	
	protected void initselectableChannel() throws IOException {
		
		this.selectableChannel = SocketChannel.open();

		this.selectableChannel.configureBlocking(false);
	}
	
	protected SelectorLoop newSelectorLoop(SelectorLoop[] selectorLoops) throws IOException {
		return new ClientSocketChannelSelectorLoop(this, selectorLoops);
	}

	protected void connect(BaseContext context, InetSocketAddress socketAddress) throws IOException {
		
		((SocketChannel) this.selectableChannel).connect(socketAddress);
		
		initSelectorLoops();
		
		if (waiter.await(getTimeout())) {

			active = true;

			CloseUtil.close(this);

			throw new TimeoutException("connect to " + socketAddress.toString() + " time out");
		}

		Object o = waiter.getPayload();

		if (o instanceof Exception) {

			Exception t = (Exception) o;

			throw new TimeoutException(MessageFormatter.format(
					"connect faild,connector:[{}],nested exception is {}", socketAddress,
					t.getMessage()), t);
		}
	}
	
	public SocketSession connect() throws IOException {
		
		this.service();
		
		return getSession();
	}
	
	public SocketSession getSession() {
		return session;
	}

	protected void fireSessionOpend() {
		session.fireOpend();
	}

	protected void finishConnect(UnsafeSocketSession session, IOException exception) {

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
	
	public String getServiceDescription(int i) {
		return "tcp-io-process-" + i;
	}

}
