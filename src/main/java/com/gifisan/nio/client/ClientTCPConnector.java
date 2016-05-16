package com.gifisan.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Connector;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.TCPSelectorLoop;
import com.gifisan.nio.concurrent.TaskExecutor;

public class ClientTCPConnector implements Connector {

	private AtomicBoolean		connected			= new AtomicBoolean(false);
	private ClientContext		context			= null;
	private ClientTCPEndPoint	endPoint			= null;
	private AtomicBoolean		keepAlive			= new AtomicBoolean(false);
	private Logger				logger			= LoggerFactory.getLogger(ClientTCPConnector.class);
	private Selector			selector			= null;
	private InetSocketAddress	serverAddress		= null;
	private TaskExecutor		taskExecutor		= null;
	private TCPSelectorLoop		selectorLoop		= null;
	private ClientEndPointWriter	endPointWriter		= null;
	private ClientUDPConnector	udpConnector		= null;

	protected TCPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

	protected EndPointWriter getEndPointWriter() {
		return endPointWriter;
	}

	public ClientTCPConnector(String host, int port) {
		this.context = new ClientContext(host, port);
	}

	public void close() throws IOException {

		Thread thread = Thread.currentThread();

		if (selectorLoop.isMonitor(thread)) {
			throw new IllegalStateException("not allow to close on future callback");
		}

		if (connected.compareAndSet(true, false)) {
			LifeCycleUtil.stop(context);
			LifeCycleUtil.stop(taskExecutor);
			LifeCycleUtil.stop(selectorLoop);
			LifeCycleUtil.stop(endPointWriter);
			CloseUtil.close(udpConnector);
			CloseUtil.close(endPoint);
		}
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {
			try {
				this.context.start();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			this.connect0();
			

			try {
				
				this.endPointWriter.start();
				
				this.selectorLoop.start();
			} catch (Exception e) {
				DebugUtil.debug(e);
			}
		}
	}

	private void finishConnect(Selector selector) throws IOException {
		Iterator<SelectionKey> iterator = select(0);
		finishConnect(iterator);
	}

	private void connect0() throws IOException {
		this.serverAddress = new InetSocketAddress(context.getServerHost(), context.getServerPort());
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_CONNECT);
		channel.connect(serverAddress);
		finishConnect(selector);
	}

	private void finishConnect(Iterator<SelectionKey> iterator) throws IOException {
		for (; iterator.hasNext();) {
			SelectionKey selectionKey = iterator.next();
			iterator.remove();
			finishConnect0(selectionKey);
		}
	}

	private void finishConnect0(SelectionKey selectionKey) throws IOException {
		SocketChannel channel = (SocketChannel) selectionKey.channel();
		// does it need connection pending ?
		if (selectionKey.isConnectable() && channel.isConnectionPending()) {
			channel.finishConnect();
			channel.register(selector, SelectionKey.OP_READ);
			this.endPointWriter = new ClientEndPointWriter();
			this.endPoint = new ClientTCPEndPoint(context, selectionKey, this);
			endPointWriter.setEndPoint(this.endPoint);
			this.selectorLoop = new ClientSelectorManagerLoop(context, selector,
					endPointWriter);
			selectionKey.attach(endPoint);
		}
	}

	public ClientSession getClientSession() throws IOException {
		return endPoint.getSession();
	}

	protected ClientContext getContext() {
		return context;
	}

	public String getServerHost() {
		return context.getServerHost();
	}

	public int getServerPort() {
		return context.getServerPort();
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

	/**
	 * 这个方法不一定按照你指定的时间间隔做心跳动作，但是它一定会努力去做的
	 * 
	 * @param checkInterval
	 */
	//FIXME 是否需要服务端反馈
	public void keepAlive(long checkInterval) {
		if (keepAlive.compareAndSet(false, true)) {
			try {
				this.startTouchDistantJob(checkInterval);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Iterator<SelectionKey> select(long timeout) throws IOException {
		selector.select(timeout);
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		return selectionKeys.iterator();
	}

	private void startTouchDistantJob(long checkInterval) throws Exception {
		TouchDistantJob job = new TouchDistantJob(endPointWriter, endPoint, this.getClientSession());
		this.taskExecutor = new TaskExecutor(job, "touch-distant-task", checkInterval);
		this.taskExecutor.start();
	}

	public String toString() {
		return "TCP:Connector@" + endPoint.toString();
	}

}
