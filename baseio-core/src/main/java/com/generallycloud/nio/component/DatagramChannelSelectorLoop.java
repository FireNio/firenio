package com.generallycloud.nio.component;

import java.nio.channels.SelectionKey;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public abstract class DatagramChannelSelectorLoop extends AbstractSelectorLoop {

	private Logger				logger	= LoggerFactory.getLogger(DatagramChannelSelectorLoop.class);
	private SelectionAcceptor	_read_acceptor;

	public DatagramChannelSelectorLoop(NIOContext context) {
		this._read_acceptor = new DatagramChannelSelectionReader(context);
	}

	public void accept(SelectionKey selectionKey) {
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

		logger.error(e.getMessage(), e);
	}

	public String toString() {
		return "UDP:Selector@" + this.selector.toString();
	}

}
