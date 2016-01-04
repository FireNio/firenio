package com.gifisan.mtp.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.TaskExecutor;

public class NIOClient implements Closeable {

	private String			sessionID		= null;
	private ClientConnection	connection	= null;
	private AtomicBoolean	keepAlive		= new AtomicBoolean(false);
	private long			checkInterval	= 5 * 60 * 1000;
	private TaskExecutor	taskExecutor	= null;

	public NIOClient(String host, int port, String sessionID) {
		this.sessionID = sessionID;
		this.connection = new ClientConnection(host, port);
	}

	public void close() throws IOException {
		if (taskExecutor != null) {
			taskExecutor.stop();
		}
		connection.close();
	}

	public void connect() throws IOException {
		this.connection.connect();

	}

	public Response request(String serviceName, String content, InputStream inputStream, long timeout)
			throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			return null;
		}

		ClientConnection connection = this.connection;

		connection.write(this.sessionID, serviceName, content, inputStream);

		return connection.acceptResponse(timeout);
	}

	public Response request(String serviceName, String content, long timeout) throws IOException {
		return this.request(serviceName, content, null, timeout);
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
		if (keepAlive.compareAndSet(false, true)) {
			this.connection.keepAlive();
			this.startTouchDistantJob();
		}
	}

	public void keepAlive(long checkInterval) {
		if (keepAlive.compareAndSet(false, true)) {
			this.checkInterval = checkInterval;
			this.connection.keepAlive();
			this.startTouchDistantJob();
		}

	}

	private void startTouchDistantJob() {
		TouchDistantJob job = new TouchDistantJob(connection);
		this.taskExecutor = new TaskExecutor(job, "touch-distant-task", checkInterval);
		this.taskExecutor.start();
	}

}
