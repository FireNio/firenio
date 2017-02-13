/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.nio.connector;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSessionEventListenerAdapter;
import com.generallycloud.nio.component.SocketSession;

public class ReconnectableConnector implements Closeable {

	private Logger					logger				= LoggerFactory.getLogger(ReconnectableConnector.class);
	private SocketChannelConnector	connect2Front			= null;
	private long					retryTime				= 15000;
	private volatile boolean		reconnect				= true;
	private ReconnectableConnector	reconnectableConnector	= null;

	public ReconnectableConnector(SocketChannelContext context) {
		context.addSessionEventListener(getReconnectSEListener());
		this.connect2Front = new SocketChannelConnector(context);
		this.reconnectableConnector = this;
	}

	public boolean isConnected() {
		return connect2Front.isConnected();
	}

	public SocketSession getSession() {
		return connect2Front.getSession();
	}

	public synchronized void connect() {

		if (!reconnect) {
			logger.info("连接已经关闭，停止重连");
			return;
		}

		SocketSession session = connect2Front.getSession();

		if (session != null && session.isOpened() && !session.isClosing()) {
			logger.info("该session未关闭，取消连接");
			return;
		}

		ThreadUtil.sleep(300);
		
		logger.info("开始尝试建立连接");

		for (;;) {

			if (session != null && session.isClosing()) {

				logger.error("连接尚未完整关闭，稍后尝试重连");

				ThreadUtil.sleep(retryTime);

				continue;
			}

			try {

				connect2Front.connect();

				break;
			} catch (Throwable e) {

				CloseUtil.close(connect2Front);

				logger.error(e.getMessage(), e);
			}

			logger.error("连接失败，正在尝试重连");

			ThreadUtil.sleep(retryTime);
		}
	}

	private SocketSessionEventListenerAdapter getReconnectSEListener() {

		return new SocketSessionEventListenerAdapter() {

			@Override
			public void sessionClosed(SocketSession session) {
				reconnect(reconnectableConnector);
			}
		};
	}

	private void reconnect(ReconnectableConnector reconnectableConnector) {
		
		ThreadUtil.execute(new Runnable() {

			@Override
			public void run() {
				logger.info("开始尝试重连");
				reconnectableConnector.connect();
			}
		});
	}

	@Override
	public void close() {
		reconnect = false;
		synchronized (this) {
			CloseUtil.close(connect2Front);
		}
	}

	public long getRetryTime() {
		return retryTime;
	}

	public void setRetryTime(long retryTime) {
		this.retryTime = retryTime;
	}

}
