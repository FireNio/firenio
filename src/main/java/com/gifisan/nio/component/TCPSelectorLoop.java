package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;

public class TCPSelectorLoop extends AbstractSelectorLoop implements SelectionAcceptor {

	private Logger				logger			= LoggerFactory.getLogger(TCPSelectorLoop.class);
	private SelectionAcceptor	_read_acceptor		;
	private SelectionAcceptor	_write_acceptor	;
	private SelectionAcceptor	_accept_acceptor	;

	public TCPSelectorLoop(NIOContext context, Selector selector, EndPointWriter endPointWriter) {
		this.selector = selector;
		this._write_acceptor  = new TCPSelectionWriter();
		this._read_acceptor   = new TCPSelectionReader(context);
		this._accept_acceptor = new TCPSelectionAcceptor(context,endPointWriter,selector);
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
		return "TCP:Selector@"+this.selector.toString();
	}

}
