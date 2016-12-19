package com.generallycloud.nio.container;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.common.StringUtil;

public class RESMessageDecoder {

	public static RESMessage decode(String content){
		if (StringUtil.isNullOrBlank(content)) {
			return null;
		}
		JSONObject object = JSON.parseObject(content);
		int code = object.getIntValue("code");
		Object data = object.get("data");
		String description = object.getString("description");
		return new RESMessage(code,data, description);
	}
}
