package com.generallycloud.nio.extend.configuration;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.component.DefaultParameters;

public class Configuration extends DefaultParameters{

	public Configuration(JSONObject object) {
		super(object);
	}

	public Configuration(String json) {
		super(json);
	}

}
