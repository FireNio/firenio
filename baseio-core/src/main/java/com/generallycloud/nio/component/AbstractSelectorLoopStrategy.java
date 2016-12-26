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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;
import com.generallycloud.nio.component.concurrent.BufferedArrayList;

public abstract class AbstractSelectorLoopStrategy implements SelectorLoopStrategy {

	private Logger								logger	= LoggerFactory
															.getLogger(AbstractSelectorLoopStrategy.class);

	protected boolean							hasTask	= false;
	protected int								runTask	= 0;
	protected SelectorLoop						selectorLoop;
	protected AtomicBoolean						selecting = new AtomicBoolean();
	protected BufferedArrayList<SelectorLoopEvent>	positiveEvents	= new BufferedArrayList<SelectorLoopEvent>();
	protected BufferedArrayList<SelectorLoopEvent>	negativeEvents	= new BufferedArrayList<SelectorLoopEvent>();
	
	
	protected AbstractSelectorLoopStrategy(SelectorLoop selectorLoop) {
		this.selectorLoop = selectorLoop;
	}

	protected void selectEmpty(SelectorLoop looper,long last_select) {

		long past = System.currentTimeMillis() - last_select;

		if (past < 1000) {

			if (!looper.isRunning() || past < 0) {
				return;
			}

			// JDK bug fired ?
			IOException e = new IOException("JDK bug fired ?");
			logger.error(e.getMessage(), e);
			logger.debug("last={},past={}", last_select, past);
			
			looper.rebuildSelector();
		}
	}
	
	protected void handlePositiveEvents(SelectorLoop looper, boolean refresh){
		
		List<SelectorLoopEvent> eventBuffer = positiveEvents.getBuffer();

		if (eventBuffer.size() == 0) {

			hasTask = false;

			return;
		}

		for (SelectorLoopEvent event : eventBuffer) {

			try {

				if (event.handle(looper)) {
					
					//FIXME xiaolv hui jiangdi
					if (event.isPositive()) {
						positiveEvents.offer(event);
					}else{
						negativeEvents.offer(event);
					}
				}
				
			} catch (IOException e) {

				CloseUtil.close(event);

				continue;
			}
		}

		hasTask = positiveEvents.getBufferSize() > 0;

		if (hasTask && refresh) {
			runTask = 5;
		}
		
	}

	protected void handleNegativeEvents(SelectorLoop looper) {

		List<SelectorLoopEvent> eventBuffer = negativeEvents.getBuffer();

		if (eventBuffer.size() == 0) {
			return;
		}

		for (SelectorLoopEvent event : eventBuffer) {

			try {

				if (event.handle(looper)) {
					
					//FIXME xiaolv hui jiangdi
					if (event.isPositive()) {
						positiveEvents.offer(event);
					}else{
						negativeEvents.offer(event);
					}
				}
				
			} catch (IOException e) {

				CloseUtil.close(event);

				continue;
			}
		}
	}

	@Override
	public void stop() {

		List<SelectorLoopEvent> eventBuffer = positiveEvents.getBuffer();

		for (SelectorLoopEvent event : eventBuffer) {

			CloseUtil.close(event);
		}
	}

	@Override
	public void fireEvent(SelectorLoopEvent event) {
		
		positiveEvents.offer(event);
		
		if (positiveEvents.getBufferSize() < 3) {
			
			wakeup();
		}
	}
	
	@Override
	public void wakeup() {
		
		if (selecting.compareAndSet(false, true)) {
			selecting.set(false);
			return;
		}
		
		selectorLoop.wakeup();
	}
	
}
