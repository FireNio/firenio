package com.gifisan.mtp.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.gifisan.mtp.server.Attributes;

public class AttributesImpl implements Attributes{
	
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public Object removeAttribute(String key) {
		return this.attributes.remove(key);
	}

	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	public Set<String> getAttributeNames() {
		return this.attributes.keySet();
	}

	public void clearAttributes() {
		this.attributes.clear();
		
	}

	
	
}
