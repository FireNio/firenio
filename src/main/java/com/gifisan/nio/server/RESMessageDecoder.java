package com.gifisan.nio.server;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.common.StringUtil;

public class RESMessageDecoder {

	public static RESMessage decode(String content){
		if (StringUtil.isNullOrBlank(content)) {
			return null;
		}
		JSONObject object = JSONObject.parseObject(content);
		int code = object.getIntValue("code");
		String description = object.getString("description");
		return new RESMessage(code, description);
	}
}
