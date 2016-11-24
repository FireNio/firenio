package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.AbstractChannelService;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.UnsafeSession;
import com.generallycloud.nio.configuration.ServerConfiguration;

public abstract class AbstractChannelConnector extends AbstractChannelService implements ChannelConnector {

	protected long			timeout		= 3000;
	protected UnsafeSession		session;
	
	private Logger 			logger 		= LoggerFactory.getLogger(AbstractChannelConnector.class);
	
	public AbstractChannelConnector(BaseContext context) {
		super(context);
	}

	public void close() throws IOException {
		if (session == null) {
			physicalClose();
			return;
		}
		CloseUtil.close(session);
	}
	
	public void physicalClose() throws IOException {
		
		//FIXME always true
		if (session.isInSelectorLoop()) {
			ThreadUtil.execute(new Runnable() {
				
				public void run() {
					doPhysicalClose();
				}
			});
			return;
		}
		
		doPhysicalClose();
	}

	private void doPhysicalClose(){
		cancelService();
	}
	
	protected void initService(ServerConfiguration configuration) throws IOException {
		
		String SERVER_HOST = configuration.getSERVER_HOST();
		
		int SERVER_PORT = configuration.getSERVER_PORT();
		
		this.serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);
		
		this.connect(context, getServerSocketAddress());
		
		LoggerUtil.prettyNIOServerLog(logger, "已连接到远程服务器 @{}",getServerSocketAddress());
		
		this.session.fireOpend();
	}

	public Session connect() throws IOException {
		
		this.service();
		
		return getSession();
	}

	protected abstract void connect(BaseContext context, InetSocketAddress socketAddress) throws IOException;

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
