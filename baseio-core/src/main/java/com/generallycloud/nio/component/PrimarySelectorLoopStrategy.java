package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;


public class PrimarySelectorLoopStrategy extends AbstractSelectorLoopStrategy{

	public void loop(SelectorLoop looper) throws IOException {

		Selector selector = looper.getSelector();

		int selected;

		// long last_select = System.currentTimeMillis();

		if (hasTask) {

			if (runTask-- > 0) {

				handleEvents(looper, false);

				return;
			}

			selected = selector.selectNow();
		} else {

			selected = selector.select(8);
		}
		
		if (selected < 1) {

			// selectEmpty(last_select);
		} else {

			Set<SelectionKey> selectionKeys = selector.selectedKeys();

			for (SelectionKey key : selectionKeys) {

				looper.accept(key);
			}

			selectionKeys.clear();
		}

		handleEvents(looper, true);
	}

	public void regist(SocketChannel channel, SelectorLoop selectorLoop) throws IOException {
		
		byte [] lock = selectorLoop.getIsWaitForRegistLock();
		
		synchronized (lock) {

			selectorLoop.setWaitForRegist(true);

			selectorLoop.wakeup();

			super.regist(channel, selectorLoop);
			
			selectorLoop.setWaitForRegist(false);
		}
	}
	
}
