package com.gifisan.mtp.server.selector;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.server.ConnectorHandle;
import com.gifisan.mtp.server.NIOConnectorHandle;

public final class SelectorManager extends AbstractLifeCycle implements SelectionAccept{

	private ConnectorHandle connectorHandle 		 = null;
	private Selector selector 						 = null;
	private ServerSocketChannel serverSocketChannel = null;
	
	public void accept(long timeout) throws IOException {
		selector.select(timeout);
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectionKeys.iterator();
		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();
			iterator.remove();
			if (!selectionKey.isValid()) {
				continue;
			}
			
			if (selectionKey.isAcceptable()) {
				this.accept(selectionKey);
				continue;
			}
			
			try {
				this.connectorHandle.accept(selectionKey);
			} catch (Exception e) {
				e.printStackTrace();
				SelectableChannel channel = selectionKey.channel();
				CloseUtil.close(channel);
				selectionKey.cancel();
			}
		}
	}
	
	public void accept(SelectionKey selectionKey) throws IOException {
		// 返回为之创建此键的通道。
		ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
		// 此方法返回的套接字通道（如果有）将处于阻塞模式。
		SocketChannel client = server.accept();
		// 配置为非阻塞
		client.configureBlocking(false);
		// 注册到selector，等待连接
		client.register(selector, SelectionKey.OP_READ);
	}

	protected void doStart() throws Exception {
		this.selector = Selector.open();
		this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.connectorHandle = new NIOConnectorHandle();
		this.connectorHandle.start();
		
	}
	
	protected void doStop() throws Exception {
		LifeCycleUtil.stop(connectorHandle);
		//应该交给task去关
		//this.selector.close();
	}
	
	public Selector getSelector() {
		return selector;
	}
	
	public void register(ServerSocketChannel serverSocketChannel) throws ClosedChannelException{
		this.serverSocketChannel = serverSocketChannel;
	}
	
	
}
