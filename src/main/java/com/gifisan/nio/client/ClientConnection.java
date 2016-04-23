package com.gifisan.nio.client;

import java.io.Closeable;
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
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.NIOEndPoint;
import com.gifisan.nio.server.selector.SelectorManagerLoop;

public class ClientConnection implements Connectable, Closeable {

	private AtomicBoolean		connected			= new AtomicBoolean(false);
	private EndPoint			endPoint			= null;
	private Selector			selector			= null;
	private InetSocketAddress	serverAddress		= null;
	private SelectorManagerLoop	selectorManagerLoop	= null;
	private ClientContext		context			= null;

	public ClientConnection(ClientContext context) {
		this.context = context;
	}
	
	public ClientSession getClientSession(byte sessionID){
		return (ClientSession) endPoint.getSession(sessionID);
		
	}

	public void close() throws IOException {
		if (connected.compareAndSet(true, false)) {
			CloseUtil.close(endPoint);
			this.selector.close();
		}
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {
			this.serverAddress = new InetSocketAddress(context.getServerHost(), context.getServerPort());
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(false);
			selector = Selector.open();
			channel.register(selector, SelectionKey.OP_CONNECT);
			channel.connect(serverAddress);
			connect0(selector);
		}
	}

	private void connect0(Selector selector) throws IOException {
		Iterator<SelectionKey> iterator = select(0);
		finishConnect(iterator);
	}
	
	private void finishConnect(Iterator<SelectionKey> iterator) throws IOException{
		for (; iterator.hasNext();) {
			SelectionKey selectionKey = iterator.next();
			iterator.remove();
			finishConnect0(selectionKey);
		}
	}
	
	private void finishConnect0(SelectionKey selectionKey) throws IOException{
		SocketChannel channel = (SocketChannel) selectionKey.channel();
		if (selectionKey.isConnectable() && channel.isConnectionPending()) {
			channel.finishConnect();
			this.endPoint = new NIOEndPoint(selectionKey);
			this.selectorManagerLoop = new SelectorManagerLoop(context, selector);
			try {
				this.selectorManagerLoop.start();
			} catch (Exception e) {
				DebugUtil.debug(e);
			}
		}
	}

	private Iterator<SelectionKey> select(long timeout) throws IOException{
		selector.select(timeout);
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		return selectionKeys.iterator();
	}
	
	protected void wakeup() {
		this.selector.wakeup();
	}

	public String toString() {
		return context.getServerHost()+":"+context.getServerPort();
	}
	
}
