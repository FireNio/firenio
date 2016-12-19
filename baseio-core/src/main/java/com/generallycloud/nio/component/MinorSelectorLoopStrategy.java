package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;


public class MinorSelectorLoopStrategy extends AbstractSelectorLoopStrategy{

	public MinorSelectorLoopStrategy(SelectorLoop selectorLoop) {
		super(selectorLoop);
	}

	private void waitForRegist(SelectorLoop looper) {
		ReentrantLock lock = looper.getIsWaitForRegistLock();

		lock.lock();
		
		lock.unlock();
	}

	@Override
	public void loop(SelectorLoop looper) throws IOException {

		Selector selector = looper.getSelector();

		int selected;

		// long last_select = System.currentTimeMillis();

		if (hasTask) {

			if (runTask-- > 0) {

				handlePositiveEvents(looper, false);

				return;
			}

			selected = selector.selectNow();
		} else {

			if (selecting.compareAndSet(false, true)) {
				
				selected = selector.select(8);
				
				selecting.set(false);
			}else{
				
				selected = selector.selectNow();
			}
		}
		
		if (looper.isWaitForRegist()) {

			waitForRegist(looper);
		}

		if (selected < 1) {
			
			handleNegativeEvents(looper);

			// selectEmpty(last_select);
		} else {

			Set<SelectionKey> selectionKeys = selector.selectedKeys();

			for (SelectionKey key : selectionKeys) {

				looper.accept(key);
			}

			selectionKeys.clear();
		}

		handlePositiveEvents(looper, true);
	}
	
}
