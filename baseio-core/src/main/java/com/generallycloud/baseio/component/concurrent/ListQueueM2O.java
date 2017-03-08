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
package com.generallycloud.baseio.component.concurrent;

public class ListQueueM2O<T> extends AbstractListQueue<T> implements ListQueue<T> {

	private FixedAtomicInteger	_end;

	protected ListQueueM2O(int capability) {
		super(capability);
		_end = new FixedAtomicInteger(capability - 1);
	}

	protected ListQueueM2O() {
		super();
		_end = new FixedAtomicInteger(_capability - 1);
	}

	@Override
	public final int getAndIncrementEnd() {
		return _end.getAndIncrement();
	}

}
