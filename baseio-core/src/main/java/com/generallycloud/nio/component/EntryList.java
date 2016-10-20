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
