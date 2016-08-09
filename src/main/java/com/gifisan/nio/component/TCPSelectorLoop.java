package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;

public abstract class TCPSelectorLoop extends AbstractSelectorLoop implements SelectionAcceptor {

	private Logger				logger	= LoggerFactory.getLogger(TCPSelectorLoop.class);
	protected SelectionAcceptor	_read_acceptor;
	protected SelectionAcceptor	_write_acceptor;
	protected SelectionAcceptor	_alpha_acceptor;

	public TCPSelectorLoop(NIOContext context, EndPointWriter endPointWriter) {
		this._write_acceptor = new TCPSelectionWriter();
		this._read_acceptor = new TCPSelectionReader(context);
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
				_alpha_acceptor.accept(selectionKey);
			} else if (selectionKey.isConnectable()) {
				_alpha_acceptor.accept(selectionKey);
			}

		} catch (Throwable e) {
			acceptException(selectionKey, e);
		}
	}

	protected void acceptException(SelectionKey selectionKey, Throwable exception) {

		SelectableChannel channel = selectionKey.channel();

		Object attachment = selectionKey.attachment();

		if (isTCPEndPoint(attachment)) {

			TCPEndPoint endPoint = (TCPEndPoint) attachment;

			endPoint.endConnect();

			CloseUtil.close(endPoint);
		}

		CloseUtil.close(channel);

		selectionKey.cancel();

		logger.error(exception.getMessage(), exception);
	}

	private boolean isTCPEndPoint(Object object) {
		return object != null && (object.getClass() == DefaultTCPEndPoint.class || object instanceof TCPEndPoint);
	}

	public String toString() {
		return "TCP:Selector@" + String.valueOf(selector.toString());
	}

}
