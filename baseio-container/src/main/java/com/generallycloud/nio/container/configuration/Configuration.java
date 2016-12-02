package com.generallycloud.nio.container.configuration;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.component.JsonParameters;

public class Configuration extends JsonParameters{

	public Configuration(JSONObject object) {
		super(object);
	}

	public Configuration(String json) {
		super(json);
	}

}
