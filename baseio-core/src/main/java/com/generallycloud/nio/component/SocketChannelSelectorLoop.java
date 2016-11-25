package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public abstract class SocketChannelSelectorLoop extends AbstractSelectorLoop {

	protected SelectionAcceptor		_read_acceptor;
	protected SelectionAcceptor		_write_acceptor;
	protected SocketChannelContext	context;
	protected ProtocolDecoder		protocolDecoder		= null;
	protected ProtocolEncoder		protocolEncoder		= null;
	protected ProtocolFactory		protocolFactory		= null;

	public SocketChannelSelectorLoop(ChannelService service, SelectorLoop[] selectorLoops) {

		super(service, selectorLoops);
		
		this.context = (SocketChannelContext) service.getContext();
		
		this.protocolFactory = context.getProtocolFactory();

		this.protocolDecoder = protocolFactory.getProtocolDecoder();

		this.protocolEncoder = protocolFactory.getProtocolEncoder();

		this.selectorLoops = selectorLoops;

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
	
	public SocketChannelContext getContext() {
		return context;
	}

	protected abstract void acceptPrepare(SelectionKey selectionKey) throws IOException;

	private SelectionAcceptor createSocketChannelSelectionReader(SocketChannelContext context) {
		return new SocketChannelSelectionReader(context);
	}
	
	
	public SocketChannel buildSocketChannel(SelectionKey selectionKey) throws SocketException {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel != null) {

			return channel;
		}

		channel = new NioSocketChannel(this, selectionKey);

		selectionKey.attach(channel);

		return channel;
	}
	

	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

}
