package com.generallycloud.nio.extend;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.common.StringUtil;

public class RESMessageDecoder {

	public static RESMessage decode(String content){
		if (StringUtil.isNullOrBlank(content)) {
			return null;
		}
		JSONObject object = JSONObject.parseObject(content);
		int code = object.getIntValue("code");
		Object data = object.get("data");
		String description = object.getString("description");
		return new RESMessage(code,data, description);
	}
}
