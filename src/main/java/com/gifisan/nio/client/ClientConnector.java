package com.gifisan.nio.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.client.session.ClientSessionFactory;
import com.gifisan.nio.client.session.MultiClientSessionFactory;
import com.gifisan.nio.client.session.UniqueClientSessionFactory;
import com.gifisan.nio.client.session.UniqueSession;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.concurrent.TaskExecutor;

public class ClientConnector implements Connectable, Closeable {

	private ClientConnection		connection			= null;
	private ClientResponseTask	responseTask			= null;
	private ClientRequestTask	requestTask			= null;
	private AtomicBoolean		connected				= new AtomicBoolean(false);
	private AtomicInteger		sessionIndex			= new AtomicInteger(-1);
	private MessageBus[]		buses				= new MessageBus[4];
	private AtomicBoolean		keepAlive				= new AtomicBoolean(false);
	private TaskExecutor		taskExecutor			= null;
	private ClientSessionFactory	clientSessionFactory	= null;

	protected ClientSessionFactory getClientSessionFactory() {
		return clientSessionFactory;
	}

	public ClientSesssion getClientSession() throws IOException {
		if (connected.get()) {
			return clientSessionFactory.getClientSesssion();
		}
		throw new IOException("did not connected to server");
	}

	public ClientConnector(String host, int port) {
		this.connection = new ClientConnection(host, port, this);
	}

	public void close() throws IOException {
		if (connected.compareAndSet(true, false)) {
			ClientLifeCycleUtil.stop(taskExecutor);
			ClientLifeCycleUtil.stop(requestTask);
			ClientLifeCycleUtil.stop(responseTask);
			this.connection.close();
		}
	}

	public boolean canTransStream() {
		return sessionIndex.get() == 0;
	}

	public void connect() throws IOException {
		this.connect(false);
	}

	public void connect(boolean multi) throws IOException {
		if (connected.compareAndSet(false, true)) {
			
			this.requestTask = new ClientRequestTask(connection);
			
			this.connection.connect(multi);
			
			if (multi) {
				
				this.responseTask = new ClientResponseTask(connection, buses);
				
				try {
					
					this.responseTask.start();
					
				} catch (Exception e) {
					
					throw new IOException(e.getMessage(), e);
					
				}
				
				this.clientSessionFactory = new MultiClientSessionFactory(buses, requestTask);
				
			} else {
				
				UniqueSession  uniqueSession = new UniqueSession(this.getClientConnection(), requestTask);
				
				this.clientSessionFactory = new UniqueClientSessionFactory(uniqueSession);
			}

			try {
				
				this.requestTask.start();
				
			} catch (Exception e) {
				
				DebugUtil.debug(e);
			}
		}
	}

	protected ClientConnection getClientConnection() {
		return this.connection;
	}

	/**
	 * 暂时还不清楚为什么要发送心跳包，但是目前实现连这样的心跳包，</BR> 当客户端设置成维持长连接时（这里要说一下为什么要设置成长连接，</BR>
	 * 我知道的有两种情况，一是因为连接创建连接太耗时，频繁的创建关闭连接太耗费资源了；</BR>
	 * 二是服务端推送模式时，客户端不知道服务端什么时候发消息过来，</BR>
	 * 所以要保持和服务端时刻处于连接中的状态，这两种情况中的第一种是不需要心跳包的，</BR>
	 * 以下所说的情况均是针对于第二种情况），会产生一个task在一定间隔时间向服务器心跳包，</BR>
	 * 服务端不用回复，为什么不用回复呢，因为这个时候客户端在等待服务端回复的业务消息，</BR>
	 * 没有两个线程收一个消息的道理吧，其实也不需要服务端往客户端回复，</BR>
	 * 因为如果没有业务消息需要给客户端的话，断了就断了，如果有消息需要传给客户端的话，</BR>
	 * 服务端会发现噢原来连接断开了，现在需要做的是一定要让客户端知道自己和服务端还连接着，</BR> 这样可以确保自己能够收到服务端发来的消息。
	 */
	public void keepAlive() {

		this.keepAlive(5 * 60 * 1000);
	}

	private void startTouchDistantJob(long checkInterval) throws Exception {
		TouchDistantJob job = new TouchDistantJob(requestTask);
		this.taskExecutor = new TaskExecutor(job, "touch-distant-task", checkInterval);
		this.taskExecutor.start();
	}

	/**
	 * 这个方法不一定按照你指定的时间间隔做心跳动作，但是它一定会努力去做的
	 * 
	 * @param checkInterval
	 */
	public void keepAlive(long checkInterval) {
		if (keepAlive.compareAndSet(false, true)) {
			try {
				this.startTouchDistantJob(checkInterval);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
