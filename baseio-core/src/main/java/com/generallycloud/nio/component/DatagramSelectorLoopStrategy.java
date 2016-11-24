package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;

public class DatagramSelectorLoopStrategy implements SelectorLoopStrategy{

	public void loop(SelectorLoop looper) throws IOException {
		
		Selector selector = looper.getSelector();

		int selected;

		// long last_select = System.currentTimeMillis();

		selected = selector.select(8);
		
		if (selected < 1) {

			// selectEmpty(last_select);
		} else {

			Set<SelectionKey> selectionKeys = selector.selectedKeys();

			for (SelectionKey key : selectionKeys) {

				looper.accept(key);
			}

			selectionKeys.clear();
		}
	}

	public void fireEvent(SelectorLoopEvent event) {
		
	}

	public void stop() {
		
	}

	public void regist(SocketChannel channel, SelectorLoop selectorLoop) throws IOException {
		
	}

	public com.generallycloud.nio.component.SocketChannel buildSocketChannel(SelectionKey selectionKey,
			SelectorLoop selectorLoop) throws SocketException {
		return null;
	}
	
}
