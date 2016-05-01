package com.gifisan.nio.component;


public class DefaultAuthority implements ActiveAuthority {

	private boolean authored = false;
	
	private long authorTime = 0;
	
	private String secretKey = null;

	public boolean isAuthored() {
		return authored;
	}

	public long getAuthorTime() {
		return authorTime;
	}

	public String getSecretKey() {
		return secretKey;
	}
	
	public void author(String secretKey){
		this.secretKey = secretKey;
		this.authored = true;
		this.authorTime = System.currentTimeMillis();
	}
	
	public void unauthor(){
		this.secretKey = null;
		this.authored = false;
		this.authorTime = 0;
	}
	
}
