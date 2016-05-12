package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.server.NIOContext;

public class TCPSelectorLoop extends AbstractSelectorLoop implements SelectionAcceptor, Runnable {

	private Logger				logger			= LoggerFactory.getLogger(TCPSelectorLoop.class);
	private SelectionAcceptor	_read_acceptor		= null;
	private SelectionAcceptor	_write_acceptor	= null;
	private SelectionAcceptor	_accept_acceptor	= null;

	public TCPSelectorLoop(NIOContext context, Selector selector, EndPointWriter endPointWriter) {
		this.selector = selector;
		this._accept_acceptor = new TCPSelectionAcceptor(selector);
		this._read_acceptor = new TCPSelectionReader(context, endPointWriter);
		this._write_acceptor = new TCPSelectionWriter(context, endPointWriter);
	}

	public void accept(SelectionKey selectionKey) throws IOException {
		if (!selectionKey.isValid()) {
			return;
		}

		try {

			if (selectionKey.isReadable()) {
				_read_acceptor.accept(selectionKey);
			} else if (selectionKey.isWritable()) {
				_write_acceptor.accept(selectionKey);
			} else if (selectionKey.isAcceptable()) {
				_accept_acceptor.accept(selectionKey);
			} else if (selectionKey.isConnectable()) {
				logger.error("Connectable=================");
			}

		} catch (IOException e) {
			acceptException(selectionKey, e);
		}

	}

	protected Thread getLooperThread() {

		return new Thread(this, "TCP:Selector@" + this.selector.toString());
	}

}
