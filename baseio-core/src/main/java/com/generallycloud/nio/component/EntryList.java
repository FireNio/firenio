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

import java.util.ArrayList;
import java.util.List;

public class EntryList<K, V> {

	private List<K>	keyList	= new ArrayList<K>();
	private List<V>	valueList	= new ArrayList<V>();

	public void add(K key, V value) {
		keyList.add(key);
		valueList.add(value);
	}

	public void clear() {
		keyList.clear();
		valueList.clear();
	}

	public int size() {
		return keyList.size();
	}

	public K getKey(int index) {
		return keyList.get(index);
	}

	public V getValue(int index) {
		return valueList.get(index);
	}

	public List<K> getKeyList() {
		return keyList;
	}

	public List<V> getValueList() {
		return valueList;
	}

}
