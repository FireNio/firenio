package com.likemessage.bean;

public class B_Contact {

	private String	backupName;
	private int	groupID;
	private String	groupName;
	private String	nickname;
	private String	phoneNo;
	private int	userID;
	private String	UUID;
	private String	pinyin;

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getBackupName() {
		return backupName;
	}

	public int getGroupID() {
		return groupID;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public String getSortKey() {
		return getBackupName();
	}

	public int getUserID() {
		return userID;
	}

	public String getUUID() {
		return UUID;
	}

	public void setBackupName(String backupName) {
		this.backupName = backupName;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public void setUUID(String UUID) {
		this.UUID = UUID;
	}

}
