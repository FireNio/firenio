package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.server.NIOContext;

public final class SelectorManager implements SelectionAcceptor {

	private Logger				logger	= LoggerFactory.getLogger(SelectorManager.class);
	private SelectionAcceptor	acceptor	= null;
	private Selector			selector	= null;

	public SelectorManager(NIOContext context,Selector selector) {
		this.selector = selector;
		this.acceptor = context.getSelectionAcceptor();
	}

	public void accept(long timeout) throws IOException {

		Selector selector = this.selector;

		int selected = selector.select(timeout);
		
		if (selected < 1) {
			return;
		}

		Set<SelectionKey> selectionKeys = selector.selectedKeys();

		Iterator<SelectionKey> iterator = selectionKeys.iterator();

		for (; iterator.hasNext();) {

			SelectionKey selectionKey = iterator.next();

			iterator.remove();

			if (!selectionKey.isValid()) {
				continue;
			}

			if (selectionKey.isAcceptable()) {
				this.accept(selectionKey);
				continue;
			}

			SelectionAcceptor acceptor = this.acceptor;

			try {
				acceptor.accept(selectionKey);
			} catch (IOException e) {
				acceptException(selectionKey, e);
			}
		}
	}

	private void acceptException(SelectionKey selectionKey, IOException exception) {
		
		SelectableChannel channel = selectionKey.channel();

		Object attachment = selectionKey.attachment();

		if (isEndPoint(attachment)) {
			
			EndPoint endPoint = (EndPoint) attachment;
			
			CloseUtil.close(endPoint);
		}
		
		CloseUtil.close(channel);
		
		selectionKey.cancel();

		logger.error(exception.getMessage(), exception);
	}

	private boolean isEndPoint(Object object) {
		return object != null && (object.getClass() == NIOEndPoint.class || object instanceof EndPoint);
	}

	public void accept(SelectionKey selectionKey) throws IOException {
		// 返回为之创建此键的通道。
		ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
		// 此方法返回的套接字通道（如果有）将处于阻塞模式。
		SocketChannel channel = server.accept();
		// 配置为非阻塞
		channel.configureBlocking(false);
		// 注册到selector，等待连接
		channel.register(selector, SelectionKey.OP_READ);
		
	}

}
