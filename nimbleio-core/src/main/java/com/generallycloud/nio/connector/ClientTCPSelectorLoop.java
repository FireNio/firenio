package com.generallycloud.nio.connector;

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

public class ClientTCPSelectorLoop extends SocketChannelSelectorLoop {

	private ChannelFlusher	channelFlusher		= null;

	private EventLoopThread	channelFlushThread	= null;

	public ClientTCPSelectorLoop(NIOContext context, SocketChannelConnector connector) {

		super(context);

		this._alpha_acceptor = new SocketChannelSelectionConnector(context, connector);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {

		this.selector = Selector.open();

		this.channelFlusher = new ChannelFlusherImpl(context);

		this.channelFlushThread = new EventLoopThread(channelFlusher, channelFlusher.toString());

		this._alpha_acceptor.setChannelFlusher(channelFlusher);

		this.channelFlushThread.start();

		SocketChannelSelectionConnector selectionConnector = (SocketChannelSelectionConnector) this._alpha_acceptor;

		selectionConnector.setSelector(selector);

		channel.register(selector, SelectionKey.OP_CONNECT);
	}

	public void stop() {

		super.stop();

		LifeCycleUtil.stop(channelFlushThread);
	}
}
