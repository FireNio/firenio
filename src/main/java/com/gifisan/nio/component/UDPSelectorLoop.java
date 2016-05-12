package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.server.NIOContext;

public class UDPSelectorLoop extends AbstractSelectorLoop implements SelectionAcceptor, Runnable {

	private Logger				logger		= LoggerFactory.getLogger(UDPSelectorLoop.class);
	private SelectionAcceptor	_read_acceptor	= null;

	public UDPSelectorLoop(NIOContext context, Selector selector) {
		this.selector = selector;
		this._read_acceptor = new UDPSelectionReader(context);
	}

	public void accept(SelectionKey selectionKey) throws IOException {
		if (!selectionKey.isValid()) {
			return;
		}

		try {

			if (selectionKey.isReadable()) {
				_read_acceptor.accept(selectionKey);
			} else if (selectionKey.isWritable()) {
				logger.error("Writable=================");
			} else if (selectionKey.isAcceptable()) {
				logger.error("Acceptable=================");
			} else if (selectionKey.isConnectable()) {
				logger.error("Connectable=================");
			}

		} catch (IOException e) {
			acceptException(selectionKey, e);
		}

	}

	protected Thread getLooperThread() {

		return new Thread(this, "UDP:Selector@" + this.selector.toString());
	}
}
