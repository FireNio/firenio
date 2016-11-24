package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.AbstractChannelService;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SessionMEvent;
import com.generallycloud.nio.configuration.ServerConfiguration;

public abstract class AbstractChannelAcceptor extends AbstractChannelService  implements ChannelAcceptor{

	protected ServerSocket serverSocket;
	
	private Logger			logger		= LoggerFactory.getLogger(AbstractChannelAcceptor.class);
	
	public AbstractChannelAcceptor(BaseContext context) {
		super(context);
	}
	
	public void bind() throws IOException {
		
		this.service();
	}
	
	protected void initService(ServerConfiguration configuration) throws IOException {
		
		this.serverAddress = new InetSocketAddress(configuration.getSERVER_PORT());

		this.bind(context, getServerSocketAddress());
		
		LoggerUtil.prettyNIOServerLog(logger, "监听已启动 @{}",getServerSocketAddress());
	}

	protected abstract void bind(BaseContext context, InetSocketAddress socketAddress) throws IOException;

	public boolean isActive() {
		return active;
	}

	public void offerSessionMEvent(SessionMEvent event) {
		context.getSessionManager().offerSessionMEvent(event);
	}
	
	public int getManagedSessionSize() {
		return context.getSessionManager().getManagedSessionSize();
	}

	public void unbind() {
		cancelService();
	}

}
