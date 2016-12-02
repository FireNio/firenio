package com.generallycloud.nio.component;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public class MapParameters extends JsonParameters {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MapParameters(Map parameters) {
		super(new JSONObject(parameters));
	}
	
}
