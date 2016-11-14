package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public abstract class SocketChannelSelectorLoop extends AbstractSelectorLoop {

	private Logger						logger	= LoggerFactory.getLogger(SocketChannelSelectorLoop.class);

	protected SelectionAcceptor			_read_acceptor;

	protected SelectionAcceptor			_write_acceptor;

	protected SocketChannelSelectionAlpha	_alpha_acceptor;

	public SocketChannelSelectorLoop(BaseContext context,SelectableChannel selectableChannel) {
		super(context,selectableChannel);

		this._write_acceptor = new SocketChannelSelectionWriter();

		this._read_acceptor = createSocketChannelSelectionReader(context);
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		if (!selectionKey.isValid()) {
			logger.info("isValid selection key={}",selectionKey);
			return;
		}

		try {

			if (selectionKey.isReadable()) {

				_read_acceptor.accept(selectionKey);
			} else if (selectionKey.isWritable()) {

				_write_acceptor.accept(selectionKey);
			} else {

				_alpha_acceptor.accept(selectionKey);
			}

		} catch (Throwable e) {

			acceptException(selectionKey, e);
		}
		
	}

	protected void acceptException(SelectionKey selectionKey, Throwable exception) {

		Object attachment = selectionKey.attachment();

		if (isSocketChannel(attachment)) {

			CloseUtil.close(((SocketChannel) attachment));
		}

		logger.error(exception.getMessage(), exception);
	}

	private boolean isSocketChannel(Object object) {
		return object instanceof SocketChannel;
	}

	public String toString() {
		return "TCP:Selector@" + String.valueOf(selector.toString());
	}

	private SelectionAcceptor createSocketChannelSelectionReader(BaseContext context) {
		
		return new SocketChannelSelectionReader(context);
	}
	
}
