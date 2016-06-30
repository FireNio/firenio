package com.gifisan.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.TimeoutException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.DefaultEndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.TCPSelectorLoop;
import com.gifisan.nio.component.concurrent.TaskExecutor;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class TCPConnector extends AbstractIOConnector {

	private TaskExecutor		taskExecutor;
	private TCPSelectorLoop		selectorLoop;
	private DefaultEndPointWriter	endPointWriter;
	private TCPEndPoint			endPoint;
	private UniqueThread		endPointWriterThread	= new UniqueThread();
	private UniqueThread		selectorLoopThread		= new UniqueThread();
	private UniqueThread		taskExecutorThread;
	private long				beatPacket;
	private ReentrantLock		connectorLock			= new ReentrantLock();
	private Condition			connectorWaiter		= connectorLock.newCondition();
	private boolean			timeout;
	private boolean			connected;
	private IOException			connectException;

	public long getBeatPacket() {
		return beatPacket;
	}

	public void setBeatPacket(long beatPacket) {
		this.beatPacket = beatPacket;
	}

	protected UniqueThread getSelectorLoopThread() {
		return selectorLoopThread;
	}

	protected void setIOService(NIOContext context) {
		context.setTCPService(this);
	}

	public String toString() {
		return "TCP:Selector@server:" + serverAddress.toString();
	}

	protected InetSocketAddress getLocalSocketAddress() {
		return endPoint.getLocalSocketAddress();
	}

	protected void connect(InetSocketAddress address) throws IOException {

		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(false);

		this.selector = Selector.open();

		channel.register(selector, SelectionKey.OP_CONNECT);

		channel.connect(address);

		//FIXME capacity should not be so big
		this.endPointWriter = new DefaultEndPointWriter(1024 * 512);

		this.selectorLoop = new ClientTCPSelectorLoop(context, selector, this, endPointWriter);
	}

	protected void startComponent(NIOContext context, Selector selector) throws IOException {

		this.selectorLoopThread.start(selectorLoop, this.toString());

		ReentrantLock lock = this.connectorLock;

		lock.lock();

		try {
			
			if (!connected) {
				
				doConnect();
			}

		} finally {

			lock.unlock();
		}
	}
	
	private void doConnect() throws IOException{
		
		try {
			
			connectorWaiter.await(1000, TimeUnit.MILLISECONDS);
			
		} catch (InterruptedException e) {
			
			connectorWaiter.signal();
		}

		if (!connected) {
			
			timeout = true;
			
			CloseUtil.close(this);
			
			if (connectException == null) {
				
				throw new TimeoutException("time out");
			}
			
			throw new TimeoutException(connectException.getMessage(),connectException);
		}
	}

	protected void stopComponent(NIOContext context, Selector selector) {

		LifeCycleUtil.stop(selectorLoopThread);
		LifeCycleUtil.stop(endPointWriterThread);
		LifeCycleUtil.stop(taskExecutorThread);
		
		CloseUtil.close(endPoint);
	}

	protected int getSERVER_PORT(ServerConfiguration configuration) {
		return configuration.getSERVER_TCP_PORT();
	}

	private void startTouchDistantJob() {
		TouchDistantJob job = new TouchDistantJob(endPointWriter, endPoint);
		this.taskExecutor = new TaskExecutor(job, beatPacket);
		this.taskExecutorThread = new UniqueThread();
		this.taskExecutorThread.start(taskExecutor, "touch-distant-task");
	}

	protected void finishConnect(TCPEndPoint endPoint,IOException exception) {

		ReentrantLock lock = this.connectorLock;

		lock.lock();
		
		if (timeout) {
			
			lock.unlock();
			
			return;
		}
		
		if (exception == null) {
			
			connected = true;

			try {

				connectorWaiter.signal();

				this.endPoint = endPoint;

				this.session = endPoint.getSession();
				
//				this.endPointWriter.setEndPoint(endPoint);

				if (beatPacket > 0) {
					this.startTouchDistantJob();
				}

				this.endPointWriterThread.start(endPointWriter, endPointWriter.toString());

			} finally {

				lock.unlock();
			}
			
		}else{
			
			connectException = exception;
			
			connectorWaiter.signal();
			
			lock.unlock();
		}
	}
}
