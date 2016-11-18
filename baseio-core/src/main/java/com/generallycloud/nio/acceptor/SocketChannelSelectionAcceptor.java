package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import com.generallycloud.nio.component.AbstractTCPSelectionAlpha;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.concurrent.FixedAtomicInteger;

public class SocketChannelSelectionAcceptor extends AbstractTCPSelectionAlpha {

	private SelectorLoop[]		selectorLoops;
	private FixedAtomicInteger	core_index;

	public SocketChannelSelectionAcceptor(BaseContext context, SelectorLoop[] loops) {

		super(context);
		
		this.selectorLoops = loops;

		this.core_index = new FixedAtomicInteger(0, loops.length - 1);
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		java.nio.channels.SocketChannel channel;

		int next_core_index = core_index.getAndIncrement();
		
		SelectorLoop selectorLoop = selectorLoops[next_core_index];

		ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();

		channel = server.accept();
		
		if (channel == null) {
			// Exception e = new Exception("core_index error");
			// logger.error(e.getMessage(), e);
			return;
		}
		
		// 配置为非阻塞
		channel.configureBlocking(false);
		// 注册到selector，等待连接
		SelectionKey sk = channel.register(selectorLoop.getSelector(), SelectionKey.OP_READ);
		// 绑定SocketChannel到SelectionKey
		SocketChannel socketChannel = attachSocketChannel(sk,selectorLoop);

		// fire session open event
		socketChannel.getSession().fireOpend();
		// logger.debug("__________________chanel____gen____{}", channel);
	}

}
