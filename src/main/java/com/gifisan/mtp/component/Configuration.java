package com.gifisan.mtp.component;

import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.Attributes;

public class Configuration implements Attributes {

	private JSONObject	_config	= null;

	public Configuration(JSONObject config) {
		this._config = config;
	}

	public Object removeAttribute(String key) {
		return this._config.remove(key);
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

	public boolean getBooleanProperty(String key) {
		return _config.getBooleanValue(key);
	}

	public double getDoubleProperty(String key) {
		return _config.getDoubleValue(key);
	}

	public int getIntegerProperty(String key) {
		return _config.getIntValue(key);
	}

	public long getLongProperty(String key) {
		return _config.getLongValue(key);
	}

	public String getProperty(String key) {
		return _config.getString(key);
	}

	public String getPropertyNoBlank(String key) throws Exception {
		String object = (String) _config.get(key);
		if (StringUtil.isNullOrBlank(object)) {
			throw new Exception("property " + key + " is empty");
		}
		return object;

	}

}
