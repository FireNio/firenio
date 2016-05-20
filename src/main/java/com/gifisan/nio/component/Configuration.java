package com.gifisan.nio.component;

import com.alibaba.fastjson.JSONObject;

public class Configuration extends DefaultParameters{

	public Configuration(JSONObject object) {
		super(object);
	}

	public Configuration(String json) {
		super(json);
	}

}
