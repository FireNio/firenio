package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.DefaultEndPointWriter;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.TCPSelectorLoop;
import com.gifisan.nio.component.concurrent.UniqueThread;

public class ServerTCPSelectorLoop extends TCPSelectorLoop {

	private EndPointWriter	endPointWriter			= null;

	private UniqueThread	endPointWriterThread	= null;

	public ServerTCPSelectorLoop(NIOContext context, CoreProcessors processors) {

		super(context);

		this._alpha_acceptor = new TCPSelectionAcceptor(context, processors);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {
		// 打开selector
		this.selector = Selector.open();

		this.endPointWriter = new DefaultEndPointWriter(context);

		this.endPointWriterThread = new UniqueThread(endPointWriter, endPointWriter.toString());
		
		this._alpha_acceptor.setEndPointWriter(endPointWriter);

		this.endPointWriterThread.start();

		TCPSelectionAcceptor selectionAcceptor = (TCPSelectionAcceptor) this._alpha_acceptor;

		selectionAcceptor.setSelector(selector);

		// 注册监听事件到该selector
		channel.register(selector, SelectionKey.OP_ACCEPT);
	}

	public void stop() {

		super.stop();

		LifeCycleUtil.stop(endPointWriterThread);
	}
}
