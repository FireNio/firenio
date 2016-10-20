package com.generallycloud.nio.extend.security;

import com.generallycloud.nio.common.MessageFormatter;

public class Permission {

	private Integer	permissionID;

	private String		permissionAPI;

	private String		description;

	private int		invoked		;

	// 按分钟计算
	private int		frequency		= 60;

	protected void setFrequency(int frequency) {
		if (frequency < 1) {
			return;
		}
		this.frequency = frequency;
	}

	private long		nextSection	;

	public Integer getPermissionID() {
		return permissionID;
	}

	public String getPermissionAPI() {
		return permissionAPI;
	}

	public String getDescription() {
		return description;
	}

	protected void setPermissionID(Integer permissionID) {
		this.permissionID = permissionID;
	}

	protected void setPermissionAPI(String permissionAPI) {
		this.permissionAPI = permissionAPI;
	}

	protected void setDescription(String description) {
		this.description = description;
	}

	public boolean invoke() {

		long currentTimeMillis = System.currentTimeMillis();

		if (currentTimeMillis < nextSection) {

			return invoked++ < frequency;

		} else {

			nextSection = currentTimeMillis + 60 * 1000;

			invoked++;

			return true;
		}
	}

	public Permission clone() {
		Permission p = new Permission();

		p.description = description;
		p.frequency = frequency;
		p.permissionAPI = permissionAPI;
		p.permissionID = permissionID;

		return p;
	}
	
	public String toString() {
		return MessageFormatter.format("[id:{},api:{}]", permissionID,permissionAPI);
	}

}
