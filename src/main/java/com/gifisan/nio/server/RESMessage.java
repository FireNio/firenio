package com.gifisan.nio.server;

import com.alibaba.fastjson.JSONObject;

public class RESMessage {

	public static RESMessage	R404_EMPTY	= new RESMessage(404, "EMPTY SERVICE-NAME");

	public static RESMessage	R_SUCCESS		= new RESMessage(0, "SUCCESS");

	public static RESMessage	R_SYSTEM_ERROR	= new RESMessage(-1, "SYSTEM ERROR");

	public static RESMessage	R_UNAUTH		= new RESMessage(403, "REQUEST FORBIDDEN");

	public static RESMessage	R_TIMEOUT		= new RESMessage(503, "TIMEOUT");

	public static RESMessage	R_USER_EXIST	= new RESMessage(1001, "USER EXIST");

	private int			code;
	private Object			data			= null;
	private String			description;

	protected RESMessage(int code) {
		this.code = code;
	}

	public RESMessage(int code, String description) {
		this.code = code;
		this.description = description;
	}

	public RESMessage(int code, Object data, String description) {
		this.code = code;
		this.data = data;
		this.description = description;
	}

	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	private String	string	= null;

	public String toString() {
		if (string == null) {

			if (data == null) {
				if (description == null) {
					string = new StringBuilder("{\"code\":").append(code).append("}").toString();
				} else {

					string = new StringBuilder("{\"code\":").append(code).append(",\"description\":\"")
							.append(description).append("\"}").toString();
				}
			} else {
				string = JSONObject.toJSONString(this);
			}
		}
		return string;
	}

	public static void main(String[] args) {

		System.out.println(R_SUCCESS);

		System.out.println(new RESMessage(100, null));
	}
}
