package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.ChannelWriterImpl;
import com.generallycloud.nio.component.ChannelWriter;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.TCPSelectorLoop;
import com.generallycloud.nio.component.concurrent.UniqueThread;

public class ServerTCPSelectorLoop extends TCPSelectorLoop {

	private ChannelWriter	channelWriter			= null;

	private UniqueThread	channelWriterThread		= null;

	public ServerTCPSelectorLoop(NIOContext context, CoreProcessors processors) {

		super(context);

		this._alpha_acceptor = new TCPSelectionAcceptor(context, processors);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {
		// 打开selector
		this.selector = Selector.open();

		this.channelWriter = new ChannelWriterImpl(context);

		this.channelWriterThread = new UniqueThread(channelWriter, channelWriter.toString());
		
		this._alpha_acceptor.setChannelWriter(channelWriter);

		this.channelWriterThread.start();

		TCPSelectionAcceptor selectionAcceptor = (TCPSelectionAcceptor) this._alpha_acceptor;

		selectionAcceptor.setSelector(selector);

		// 注册监听事件到该selector
		channel.register(selector, SelectionKey.OP_ACCEPT);
	}

	public void stop() {

		super.stop();

		LifeCycleUtil.stop(channelWriterThread);
	}
}
