package com.gifisan.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.TCPSelectorLoop;
import com.gifisan.nio.component.concurrent.TaskExecutor;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class TCPConnector extends AbstractIOConnector {

	private ClientTCPEndPoint	endPoint				;
	private TaskExecutor		taskExecutor			;
	private TCPSelectorLoop		selectorLoop			;
	private ClientEndPointWriter	endPointWriter			;
	private UniqueThread		endPointWriterThread	= new UniqueThread();
	private UniqueThread		selectorLoopThread		= new UniqueThread();
	private UniqueThread		taskExecutorThread		;
	private long				beatPacket			;

	public long getBeatPacket() {
		return beatPacket;
	}

	public void setBeatPacket(long beatPacket) {
		this.beatPacket = beatPacket;
	}

	protected TCPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

	protected EndPointWriter getEndPointWriter() {
		return endPointWriter;
	}

	private void finishConnect(Selector selector) throws IOException {
		Iterator<SelectionKey> iterator = select(0);
		finishConnect(iterator);
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

			this.endPointWriter.setEndPoint(endPoint);

			this.selectorLoop = new ClientSelectorManagerLoop(context, selector, endPointWriter);

			selectionKey.attach(endPoint);
		}
	}
	
	protected void setIOService(NIOContext context) {
		context.setTCPService(this);
	}

	private Iterator<SelectionKey> select(long timeout) throws IOException {
		selector.select(timeout);
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		return selectionKeys.iterator();
	}

	private void startTouchDistantJob(long checkInterval) throws Exception {
		TouchDistantJob job = new TouchDistantJob(endPointWriter, endPoint);
		this.taskExecutor = new TaskExecutor(job, checkInterval);
		this.taskExecutorThread = new UniqueThread();
		this.taskExecutorThread.start(taskExecutor, "touch-distant-task");
	}

	public String toString() {
		return "TCP:Connector@" + endPoint.toString();
	}

	protected void connect(InetSocketAddress address) throws IOException {

		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(false);

		selector = Selector.open();

		channel.register(selector, SelectionKey.OP_CONNECT);

		channel.connect(address);

		finishConnect(selector);
		
		this.session = this.endPoint.getSession();
	}

	protected void startComponent(NIOContext context, Selector selector) throws IOException {

		if (beatPacket > 0) {
			try {
				this.startTouchDistantJob(beatPacket);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		this.endPointWriterThread.start(endPointWriter, endPointWriter.toString());

		this.selectorLoopThread.start(selectorLoop, selectorLoop.toString());
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
}
