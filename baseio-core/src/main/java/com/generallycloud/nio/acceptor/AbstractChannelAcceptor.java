package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.AbstractChannelService;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.configuration.ServerConfiguration;

public abstract class AbstractChannelAcceptor extends AbstractChannelService  implements ChannelAcceptor{
	
	private Logger			logger		= LoggerFactory.getLogger(AbstractChannelAcceptor.class);
	
	public void bind() throws IOException {
		
		this.service();
	}
	
	protected void initService(ServerConfiguration configuration) throws IOException {
		
		this.serverAddress = new InetSocketAddress(configuration.getSERVER_PORT());

		this.bind(getServerSocketAddress());
		
		LoggerUtil.prettyNIOServerLog(logger, "监听已启动 @{}",getServerSocketAddress());
	}

	protected abstract void bind(InetSocketAddress socketAddress) throws IOException;

	public boolean isActive() {
		return active;
	}

	public void unbind() throws TimeoutException {
		
		Waiter<IOException> waiter = asynchronousUnbind();
		
		if (waiter.await()) {
			//FIXME never timeout
			throw new TimeoutException("timeout to unbind");
		}
	}
	
	public Waiter<IOException> asynchronousUnbind() {
		cancelService();
		return shutDownWaiter;
	}
	
	public int getManagedSessionSize() {
		return getContext().getSessionManager().getManagedSessionSize();
	}

}
