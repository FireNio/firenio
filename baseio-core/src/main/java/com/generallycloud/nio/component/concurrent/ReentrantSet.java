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
package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 仅适用于：</BR>
 * M => PUT </BR>
 * M => REMOVE </BR>
 * O => GET </BR>
 * O => FOREACH
 *
 * @param <K>
 */
public class ReentrantSet<K> {

	private static final byte[]		V	= {};

	private ReentrantMap<K, byte[]>	keys	= new ReentrantMap<K, byte[]>();

	public void add(K key) {

		keys.put(key, V);
	}

	public void remove(K key) {

		keys.remove(key);
	}

	public boolean contains(K key) {

		return keys.get(key) != null;
	}
	
	public ReentrantLock getReentrantLock() {
		return keys.getReentrantLock();
	}

}
