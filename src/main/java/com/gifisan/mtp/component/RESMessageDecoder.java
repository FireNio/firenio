package com.gifisan.mtp.component;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.common.StringUtil;

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
