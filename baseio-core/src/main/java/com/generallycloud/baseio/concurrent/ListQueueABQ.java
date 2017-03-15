/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;

public class ListQueueABQ<T> implements ListQueue<T> {

	private ArrayBlockingQueue<T>	queue;
	private Logger				logger	= LoggerFactory.getLogger(ListQueueABQ.class);

	public ListQueueABQ(int capacity) {
		this.queue = new ArrayBlockingQueue<T>(capacity);
	}
	
	@Override
	public boolean offer(T object) {
		return queue.offer(object);
	}

	@Override
	public T poll() {
		return queue.poll();
	}

	@Override
	public T poll(long timeout) {
		try {
			return queue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.debug(e);
			return null;
		}
	}

	@Override
	public int size() {
		return queue.size();
	}

}
