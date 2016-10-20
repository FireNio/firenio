package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.ChannelFlusher;
import com.generallycloud.nio.component.ChannelFlusherImpl;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;
import com.generallycloud.nio.component.concurrent.EventLoopThread;

public class ServerTCPSelectorLoop extends SocketChannelSelectorLoop {

	private ChannelFlusher	channelFlusher			= null;

	private EventLoopThread	channelFlushThread		= null;

	public ServerTCPSelectorLoop(NIOContext context, CoreProcessors processors) {

		super(context);

		this._alpha_acceptor = new SocketChannelSelectionAcceptor(context, processors);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {
		// 打开selector
		this.selector = Selector.open();

		this.channelFlusher = new ChannelFlusherImpl(context);

		this.channelFlushThread = new EventLoopThread(channelFlusher, channelFlusher.toString());
		
		this._alpha_acceptor.setChannelFlusher(channelFlusher);

		this.channelFlushThread.start();

		SocketChannelSelectionAcceptor selectionAcceptor = (SocketChannelSelectionAcceptor) this._alpha_acceptor;

		selectionAcceptor.setSelector(selector);

		// 注册监听事件到该selector
		channel.register(selector, SelectionKey.OP_ACCEPT);
	}

	public void stop() {

		super.stop();

		LifeCycleUtil.stop(channelFlushThread);
	}
}
