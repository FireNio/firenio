package com.gifisan.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.gifisan.nio.acceptor.ServerTCPEndPoint;

public class TCPSelectionAcceptor implements SelectionAcceptor {

	private Selector		selector;
	private NIOContext		context		= null;
	private EndPointWriter	endPointWriter	= null;
//	private Logger			logger		= LoggerFactory.getLogger(TCPSelectionAcceptor.class);

	public TCPSelectionAcceptor(NIOContext context, EndPointWriter endPointWriter, Selector selector) {
		this.context = context;
		this.selector = selector;
		this.endPointWriter = endPointWriter;
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		// 返回为之创建此键的通道。
		ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
		// 此方法返回的套接字通道（如果有）将处于阻塞模式。
		SocketChannel channel = server.accept();
		// 配置为非阻塞
		channel.configureBlocking(false);
		// 注册到selector，等待连接
		SelectionKey sk = channel.register(selector, SelectionKey.OP_READ);
		// 绑定EndPoint到SelectionKey
		attachEndPoint(context, endPointWriter, sk);

//		logger.debug("__________________chanel____gen____{}", channel);

	}

	private void attachEndPoint(NIOContext context, EndPointWriter endPointWriter, SelectionKey selectionKey)
			throws SocketException {

		ServerTCPEndPoint endPoint = new ServerTCPEndPoint(context, selectionKey, endPointWriter);

		selectionKey.attach(endPoint);
	}
}
