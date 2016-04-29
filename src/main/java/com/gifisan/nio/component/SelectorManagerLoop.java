package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.AbstractLifeCycleListener;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.LifeCycleListener;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.server.NIOContext;

public class SelectorManagerLoop extends AbstractLifeCycle implements SelectionAcceptor, Runnable {

	private Logger				logger			= LoggerFactory.getLogger(SelectorManagerLoop.class);
	private Thread				looper			= null;
	private Selector			selector			= null;
	private SelectionAcceptor	_read_acceptor		= null;
	private SelectionAcceptor	_write_acceptor	= null;
	private SelectionAcceptor	_accept_acceptor	= null;

	public SelectorManagerLoop(NIOContext context, Selector selector) {
		this.selector = selector;
		this._accept_acceptor = new NIOSelectionAcceptor(selector);
		this._read_acceptor = new NIOSelectionReader(context);
		this._write_acceptor = new NIOSelectionWriter(context);
	}

	private void acceptException(SelectionKey selectionKey, IOException exception) {

		SelectableChannel channel = selectionKey.channel();

		Object attachment = selectionKey.attachment();

		if (isEndPoint(attachment)) {

			EndPoint endPoint = (EndPoint) attachment;
			
			endPoint.endConnect();

			CloseUtil.close(endPoint);
		}

		CloseUtil.close(channel);

		selectionKey.cancel();

		logger.error(exception.getMessage(), exception);
	}

	private boolean isEndPoint(Object object) {
		return object != null && (object.getClass() == NIOEndPoint.class || object instanceof EndPoint);
	}

	public void run() {
		for (; isRunning();) {

			try {

				Selector selector = this.selector;

				int selected = selector.select(1000);

				if (selected < 1) {
					continue;
				}

				Set<SelectionKey> selectionKeys = selector.selectedKeys();

				Iterator<SelectionKey> iterator = selectionKeys.iterator();

				for (; iterator.hasNext();) {

					SelectionKey selectionKey = iterator.next();

					iterator.remove();

					accept(selectionKey);

				}

			} catch (Throwable e) {

				logger.error(e.getMessage(), e);
			}
		}
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
			}else if(selectionKey.isConnectable()){
				logger.info("=================");
			}

		} catch (IOException e) {
			acceptException(selectionKey, e);
		}

	}

	protected void doStart() throws Exception {

		this.addLifeCycleListener(new EventListener());

		this.looper = new Thread(this, "Selector@" + this.selector.toString());
	}

	protected void doStop() throws Exception {
		this.selector.wakeup();
		this.selector.close();
	}

	private class EventListener extends AbstractLifeCycleListener implements LifeCycleListener {

		public void lifeCycleStarted(LifeCycle lifeCycle) {
			looper.start();
		}

		public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {
			logger.error(exception.getMessage(), exception);
		}

	}

}
