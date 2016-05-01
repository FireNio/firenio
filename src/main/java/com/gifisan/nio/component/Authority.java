package com.gifisan.nio.component;

public interface Authority {

	public abstract boolean isAuthored();

	public abstract long getAuthorTime();

	public abstract String getSecretKey();

}