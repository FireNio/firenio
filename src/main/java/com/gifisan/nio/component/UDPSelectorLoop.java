package com.gifisan.nio.component;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.server.NIOContext;

public class UDPSelectorLoop extends AbstractSelectorLoop implements SelectionAcceptor {

	private Logger				logger		= LoggerFactory.getLogger(UDPSelectorLoop.class);
	private SelectionAcceptor	_read_acceptor	= null;

	public UDPSelectorLoop(NIOContext context, Selector selector) {
		this.selector = selector;
		this._read_acceptor = new UDPSelectionReader(context);
	}

	public void accept(SelectionKey selectionKey)  {
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

		} catch (Exception e) {
			acceptException(selectionKey, e);
		}
	}

	private void acceptException(SelectionKey selectionKey, Exception e) {
		
		logger.error(e.getMessage(),e);
	}
	
	public String toString() {
		return "UDP:Selector@" + this.selector.toString();
	}

}
