package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TCPSelectionAcceptor implements SelectionAcceptor {

	private Selector	selector	= null;

	public TCPSelectionAcceptor(Selector selector) {
		this.selector = selector;
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
