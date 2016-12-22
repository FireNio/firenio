/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
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
				
				selected = selector.select(16);
				
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
