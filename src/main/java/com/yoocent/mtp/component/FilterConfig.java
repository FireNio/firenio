package com.yoocent.mtp.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.yoocent.mtp.server.Attributes;

public class FilterConfig extends AttributesImpl implements Attributes{

	private Map<String,Object> _config = new HashMap<String, Object>();
	
	public void setConfig(Map<String, Object> config) {
		this._config = config;
	}

	public void removeAttribute(String key) {
		this._config.remove(key);
	}

	public void setAttribute(String key, Object value) {
		this._config.put(key, value);
	}

	public Object getAttribute(String key) {
		return this._config.get(key);
	}

	public Set<String> getAttributeNames() {
		return this._config.keySet();
	}

	public void clearAttributes() {
		this._config.clear();
	}

}
