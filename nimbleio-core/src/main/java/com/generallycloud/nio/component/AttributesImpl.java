package com.generallycloud.nio.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AttributesImpl implements Attributes{
	
	private Map<Object, Object> attributes = new HashMap<Object, Object>();

	public Object removeAttribute(Object key) {
		return this.attributes.remove(key);
	}

	public void setAttribute(Object key, Object value) {
		this.attributes.put(key, value);
	}

	public Object getAttribute(Object key) {
		return this.attributes.get(key);
	}

	public Set<Object> getAttributeNames() {
		return this.attributes.keySet();
	}

	public void clearAttributes() {
		this.attributes.clear();
		
	}

	
	
}
