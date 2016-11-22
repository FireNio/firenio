package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import com.generallycloud.nio.common.DebugUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;
import com.generallycloud.nio.component.concurrent.FixedAtomicInteger;

public class ServerTCPSelectorLoop extends SocketChannelSelectorLoop {
	
	private SelectorLoop[]		selectorLoops;
	
	private FixedAtomicInteger	core_index;

	public ServerTCPSelectorLoop(BaseContext context, SelectorLoop[] loops, SelectableChannel selectableChannel) {

		super(context, selectableChannel);
		
		this.selectorLoops = loops;
	}

	public Selector buildSelector(SelectableChannel channel) throws IOException {
		
		// 打开selector
		Selector selector = Selector.open();
		// 注册监听事件到该selector

		if (selectorLoops[0] == this) {
			
			channel.register(selector, SelectionKey.OP_ACCEPT);
			
			setMainSelector(true);
			
			core_index = new FixedAtomicInteger(selectorLoops.length -1);
		}

		return selector;
	}

	protected void acceptPrepare(SelectionKey selectionKey) throws IOException {
		
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
		if (selectorLoop.isMainSelector()) {
			
			regist(channel, selectorLoop);
			
		}else{
			
			byte [] lock = selectorLoop.getIsWaitForRegistLock();
			
			synchronized (lock) {

				selectorLoop.setWaitForRegist(true);

				selectorLoop.wakeup();

				regist(channel, selectorLoop);
				
				selectorLoop.setWaitForRegist(false);
			}
		}
	}
	
	private void regist(java.nio.channels.SocketChannel channel,SelectorLoop selectorLoop) throws IOException{
		
		long last = System.currentTimeMillis();
		
		DebugUtil.info("before regist {}", last);
		
		SelectionKey sk = channel.register(selectorLoop.getSelector(), SelectionKey.OP_READ);
		
		DebugUtil.info("past regist {}", System.currentTimeMillis() - last);

		// 绑定SocketChannel到SelectionKey
		SocketChannel socketChannel = attachSocketChannel(sk,selectorLoop);

		// fire session open event
		socketChannel.getSession().fireOpend();
		// logger.debug("__________________chanel____gen____{}", channel);
		
	}

}
