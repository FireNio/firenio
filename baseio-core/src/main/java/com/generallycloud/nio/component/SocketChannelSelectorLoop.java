package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public abstract class SocketChannelSelectorLoop extends AbstractSelectorLoop {

	protected SelectionAcceptor			_read_acceptor;

	protected SelectionAcceptor			_write_acceptor;

	public SocketChannelSelectorLoop(BaseContext context,SelectableChannel selectableChannel) {
		super(context,selectableChannel);

		this._write_acceptor = new SocketChannelSelectionWriter();

		this._read_acceptor = createSocketChannelSelectionReader(context);
	}

	public void accept(SelectionKey selectionKey) {

		if (!selectionKey.isValid()) {
			cancelSelectionKey(selectionKey);
			return;
		}

		try {

			if (selectionKey.isReadable()) {

				_read_acceptor.accept(selectionKey);
			} else if (selectionKey.isWritable()) {

				_write_acceptor.accept(selectionKey);
			} else {

				acceptPrepare(selectionKey);
			}

		} catch (Throwable e) {

			cancelSelectionKey(selectionKey, e);
		}
		
	}
	
	protected abstract void acceptPrepare(SelectionKey selectionKey) throws IOException;

	public String toString() {
		return "TCP:Selector@" + String.valueOf(selector.toString());
	}

	private SelectionAcceptor createSocketChannelSelectionReader(BaseContext context) {
		return new SocketChannelSelectionReader(context);
	}
	
	protected SocketChannel attachSocketChannel(SelectionKey selectionKey) throws SocketException {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel != null) {

			return channel;
		}

		channel = new NioSocketChannel(this, selectionKey);

		channel.setProtocolDecoder(protocolDecoder);

		channel.setProtocolEncoder(protocolEncoder);

		channel.setProtocolFactory(protocolFactory);

		selectionKey.attach(channel);

		return channel;
	}
	
}
