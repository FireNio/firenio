/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.baseio.front;

import java.io.Closeable;

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.connector.ReconnectableConnector;

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
