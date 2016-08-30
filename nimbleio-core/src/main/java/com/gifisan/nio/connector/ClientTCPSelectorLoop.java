package com.gifisan.nio.connector;

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

public class ClientTCPSelectorLoop extends TCPSelectorLoop {
	
	private EndPointWriter	endPointWriter			= null;

	private UniqueThread	endPointWriterThread	= null;

	public ClientTCPSelectorLoop(NIOContext context, TCPConnector connector) {

		super(context);

		this._alpha_acceptor = new TCPSelectionConnector(context, connector);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {

		this.selector = Selector.open();
		
		this.endPointWriter = new DefaultEndPointWriter(context);

		this.endPointWriterThread = new UniqueThread(endPointWriter, endPointWriter.toString());
		
		this._alpha_acceptor.setEndPointWriter(endPointWriter);

		this.endPointWriterThread.start();

		TCPSelectionConnector selectionConnector = (TCPSelectionConnector) this._alpha_acceptor;

		selectionConnector.setSelector(selector);

		channel.register(selector, SelectionKey.OP_CONNECT);
	}

	public void stop() {

		super.stop();

		LifeCycleUtil.stop(endPointWriterThread);
	}
}
