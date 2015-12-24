package com.gifisan.mtp.component;

import com.alibaba.fastjson.JSONObject;

public class RESMessage {
	
	public static RESMessage R404_EMPTY = new RESMessage(404, "empty service-name");
	

	private int code;
	
	private String description;
	
	public RESMessage(int code, String description) {
		this.code = code;
		this.description = description;
	}

	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	private String string = null;
	
	public String toString() {
		if (string == null) {
			string = JSONObject.toJSONString(this);
		}
		return string;
	}
	
}
