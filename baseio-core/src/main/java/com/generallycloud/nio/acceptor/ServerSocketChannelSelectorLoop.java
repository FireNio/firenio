package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import com.generallycloud.nio.component.ChannelService;
import com.generallycloud.nio.component.MinorSelectorLoopStrategy;
import com.generallycloud.nio.component.PrimarySelectorLoopStrategy;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;
import com.generallycloud.nio.component.concurrent.FixedAtomicInteger;

public class ServerSocketChannelSelectorLoop extends SocketChannelSelectorLoop {
	
	private FixedAtomicInteger	core_index;

	public ServerSocketChannelSelectorLoop(ChannelService service, SelectorLoop[] selectorLoops) {
		super(service,selectorLoops);
	}

	public Selector buildSelector(SelectableChannel channel) throws IOException {
		
		// 打开selector
		Selector selector = Selector.open();

		if (selectorLoops[0] == this) {
			
			// 注册监听事件到该selector
			channel.register(selector, SelectionKey.OP_ACCEPT);
			
			this.setMainSelector(true);
			
			this.core_index = new FixedAtomicInteger(selectorLoops.length -1);
			
			this.selectorLoopStrategy = new PrimarySelectorLoopStrategy(this);
			
			return selector;
		}
		
		this.selectorLoopStrategy = new MinorSelectorLoopStrategy(this);

		return selector;
	}

	protected void acceptPrepare(SelectionKey selectionKey) throws IOException {

		ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
		
		java.nio.channels.SocketChannel channel = server.accept();

		if (channel == null) {
			return;
		}

		int next_core_index = core_index.getAndIncrement();
		
		SelectorLoop selectorLoop = selectorLoops[next_core_index];

		// 配置为非阻塞
		channel.configureBlocking(false);
		
		// 注册到selector，等待连接
		selectorLoop.getSelectorLoopStrategy().regist(channel, selectorLoop);
		
	}

}
