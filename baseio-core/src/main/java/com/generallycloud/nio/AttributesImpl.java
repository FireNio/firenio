package com.generallycloud.nio;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AttributesImpl implements Attributes{
	
	private Map<Object, Object> attributes = new HashMap<Object, Object>();

	@Override
	public Object removeAttribute(Object key) {
		return this.attributes.remove(key);
	}

	@Override
	public void setAttribute(Object key, Object value) {
		this.attributes.put(key, value);
	}

	@Override
	public Object getAttribute(Object key) {
		return this.attributes.get(key);
	}

	@Override
	public Set<Object> getAttributeNames() {
		return this.attributes.keySet();
	}

	@Override
	public void clearAttributes() {
		this.attributes.clear();
		
	}

	
	
}
