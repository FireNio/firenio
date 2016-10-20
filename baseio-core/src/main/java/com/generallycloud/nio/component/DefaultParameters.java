package com.generallycloud.nio.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.common.StringUtil;

public class DefaultParameters implements Parameters {

	private JSONObject	object	;

	private String		json		;

	public DefaultParameters(String json) {
		if (!StringUtil.isNullOrBlank(json)) {
			try {
				object = JSONObject.parseObject(json);
			} catch (Exception e) {
				throw new IllegalArgumentException(json, e);
			}

			this.json = json;
		}
	}
	
	public DefaultParameters(JSONObject object) {
		this.object = object;
	}

	public boolean getBooleanParameter(String key) {
		if (object == null) {
			return false;
		}
		return object.getBooleanValue(key);
	}

	public int getIntegerParameter(String key) {
		return getIntegerParameter(key, 0);
	}

	public int getIntegerParameter(String key, int defaultValue) {
		if (object == null) {
			return defaultValue;
		}
		int value = object.getIntValue(key);
		if (value == 0) {
			return defaultValue;
		}
		return value;
	}

	public long getLongParameter(String key) {
		return getLongParameter(key, 0);
	}

	public long getLongParameter(String key, long defaultValue) {
		if (object == null) {
			return defaultValue;
		}
		long value = object.getLongValue(key);
		if (value == 0) {
			return defaultValue;
		}
		return value;
	}

	public Object getObjectParameter(String key) {
		if (object == null) {
			return null;
		}
		return object.get(key);
	}

	public String getParameter(String key) {
		return getParameter(key, null);
	}

	public String getParameter(String key, String defaultValue) {
		if (object == null) {
			return defaultValue;
		}
		String value = object.getString(key);
		if (StringUtil.isNullOrBlank(value)) {
			return defaultValue;
		}
		return value;
	}

	public String toString() {
		if (json == null) {
			json = object.toJSONString();
		}
		return json;
	}

	public JSONObject getJSONObject(String key) {
		return object.getJSONObject(key);
	}

	public JSONArray getJSONArray(String key) {
		return object.getJSONArray(key);
	}

}
