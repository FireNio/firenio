package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class DatagramChannelSelectorLoop extends AbstractSelectorLoop {

	private Logger				logger	= LoggerFactory.getLogger(DatagramChannelSelectorLoop.class);
	private SelectionAcceptor	_read_acceptor;

	public DatagramChannelSelectorLoop(ChannelService service,SelectorLoop[] selectorLoops) {
		super(service,selectorLoops);
		this._read_acceptor = new DatagramChannelSelectionReader(this);
		this.selectorLoopStrategy = new DatagramSelectorLoopStrategy();
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
				logger.error("Writable=================");
			} else if (selectionKey.isAcceptable()) {
				logger.error("Acceptable=================");
			} else if (selectionKey.isConnectable()) {
				logger.error("Connectable=================");
			}

		} catch (Throwable e) {
			
			cancelSelectionKey(selectionKey, e);
		}
	}

	public Selector buildSelector(SelectableChannel channel) throws IOException {
		// 打开selector
		Selector selector = Selector.open();
		// 注册监听事件到该selector
		channel.register(selector, SelectionKey.OP_READ);
		
		channel.configureBlocking(false);
		
		return selector;
	}

}
