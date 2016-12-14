package com.generallycloud.nio.connector;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;

public class ReconnectableConnector implements Closeable {

	private Logger					logger				= LoggerFactory
															.getLogger(ReconnectableConnector.class);
	private SocketChannelConnector	connect2Front			= null;
	private long					retryTime				= 15000;
	private boolean				reconnect				= true;
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
			return;
		}

		if (isConnected()) {
			return;
		}

		// 启动本地服务端口
		logger.info("启动前端长连接端口服务...");

		for (;;) {

			try {

				connect2Front.connect();

				break;
			} catch (Throwable e) {
				
				CloseUtil.close(connect2Front);
				
				logger.error(e.getMessage(), e);
			}

			logger.error("连接失败，正在尝试重连。。。");

			ThreadUtil.sleep(retryTime);
		}
	}

	private SocketSEListenerAdapter getReconnectSEListener() {

		return new SocketSEListenerAdapter() {

			public void sessionClosed(SocketSession session) {

				ThreadUtil.execute(new Runnable() {

					public void run() {

						ThreadUtil.sleep(retryTime);

						reconnectableConnector.connect();
					}
				});
			}
		};
	}

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
