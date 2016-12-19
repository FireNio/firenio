package com.generallycloud.nio.front;

import java.io.Closeable;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.connector.ReconnectableConnector;

public class BalanceFacadeConnector implements Closeable{

	private ReconnectableConnector connector; 
	
	public synchronized void connect(SocketChannelContext context){
		
		if (connector != null) {
			return;
		}
		
		connector = new ReconnectableConnector(context);
		
		connector.connect();
		
		LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(BalanceFacadeConnector.class),
				"Balance Facade Connector 连接成功 ...");
	}
	
	@Override
	public synchronized void close(){
		CloseUtil.close(connector);
		connector = null;
	}
	
	public ReconnectableConnector getReconnectableConnector(){
		return connector;
	}
	
	public SocketSession getSession(){
		return connector.getSession();
	}
	
}
