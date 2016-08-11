package com.gifisan.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import com.gifisan.nio.TimeoutException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.MessageFormatter;
import com.gifisan.nio.component.DefaultEndPointWriter;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.TCPSelectorLoop;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.component.concurrent.Waiter;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class TCPConnector extends AbstractIOConnector {

	private SocketChannel	channel;
	private IOException		connectException;
	private TCPEndPoint		endPoint;
	private EndPointWriter	endPointWriter;
	private UniqueThread	endPointWriterThread;
	private TCPSelectorLoop	selectorLoop;
	private UniqueThread	selectorLoopThread;
	private Waiter			waiter	= new Waiter();

	protected void connect(NIOContext context,InetSocketAddress socketAddress) throws IOException {
		
		this.channel = SocketChannel.open();

		this.channel.configureBlocking(false);

		ServerConfiguration configuration = context.getServerConfiguration();

		this.endPointWriter = new DefaultEndPointWriter(configuration.getSERVER_WRITE_QUEUE_SIZE());

		this.selectorLoop = new ClientTCPSelectorLoop(context, this, endPointWriter);
		
		this.selectorLoop.register(context, channel);
		
		this.channel.connect(socketAddress);
		
		this.selectorLoopThread = new UniqueThread(selectorLoop, this.toString());
		
		this.selectorLoopThread.start();

		if (!waiter.await(30000)) {

			CloseUtil.close(this);

			if (connectException == null) {

				throw new TimeoutException("time out");
			}

			throw new TimeoutException(MessageFormatter.format("connect faild,connector:{},nested exception is {}",
					this, connectException.getMessage()), connectException);
		}

		if (waiter.isSuccess()) {
			return;
		}

		this.connected.compareAndSet(true, false);

		throw new TimeoutException(connectException.getMessage(), connectException);
	}

	protected void finishConnect(TCPEndPoint endPoint, IOException exception) {

		if (exception == null) {

			this.endPoint = endPoint;

			this.session = endPoint.getSession();

			this.waiter.setPayload(null);

			if (waiter.isSuccess()) {
				
				this.endPointWriterThread = new UniqueThread(endPointWriter, endPointWriter.toString());

				this.endPointWriterThread.start();
			}
		} else {

			this.connectException = exception;

			this.waiter.setPayload(null, false);
		}
	}

	protected InetSocketAddress getLocalSocketAddress() {
		return endPoint.getLocalSocketAddress();
	}

	protected UniqueThread getSelectorLoopThread() {
		return selectorLoopThread;
	}

	protected int getSERVER_PORT(ServerConfiguration configuration) {
		return configuration.getSERVER_TCP_PORT();
	}

	protected void setIOService(NIOContext context) {
		context.setTCPService(this);
	}

	protected void close(NIOContext context) {

		LifeCycleUtil.stop(selectorLoopThread);
		LifeCycleUtil.stop(endPointWriterThread);

		CloseUtil.close(endPoint.getSession());
	}

	public String toString() {
		return "TCP:Selector@server:" + serverAddress.toString();
	}

}
