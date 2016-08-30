package com.generallycloud.nio.common.cmd;

import java.util.HashMap;

import com.alibaba.fastjson.JSONObject;

public class CmdRequest {

	private String cmd;
	
	private HashMap<String, String> params = new HashMap<String, String>();

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	
	public String getParam(String key){
		return params.get(key);
	}
	
	
	public void putParam(String key,String value){
		this.params.put(key, value);
	}

	public HashMap<String, String> getParams() {
		return params;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}
	
	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
