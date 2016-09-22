package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.ChannelWriterImpl;
import com.generallycloud.nio.component.ChannelWriter;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;
import com.generallycloud.nio.component.concurrent.EventLoopThread;

public class ServerTCPSelectorLoop extends SocketChannelSelectorLoop {

	private ChannelWriter	channelWriter			= null;

	private EventLoopThread	channelWriterThread		= null;

	public ServerTCPSelectorLoop(NIOContext context, CoreProcessors processors) {

		super(context);

		this._alpha_acceptor = new SocketChannelSelectionAcceptor(context, processors);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {
		// 打开selector
		this.selector = Selector.open();

		this.channelWriter = new ChannelWriterImpl(context);

		this.channelWriterThread = new EventLoopThread(channelWriter, channelWriter.toString());
		
		this._alpha_acceptor.setChannelWriter(channelWriter);

		this.channelWriterThread.start();

		SocketChannelSelectionAcceptor selectionAcceptor = (SocketChannelSelectionAcceptor) this._alpha_acceptor;

		selectionAcceptor.setSelector(selector);

		// 注册监听事件到该selector
		channel.register(selector, SelectionKey.OP_ACCEPT);
	}

	public void stop() {

		super.stop();

		LifeCycleUtil.stop(channelWriterThread);
	}
}
