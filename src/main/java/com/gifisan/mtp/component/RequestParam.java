package com.gifisan.mtp.component;

public interface RequestParam {

	public abstract boolean getBooleanParameter(String key);

	public abstract int getIntegerParameter(String key);

	public abstract int getIntegerParameter(String key, int defaultValue);

	public abstract long getLongParameter(String key);

	public abstract long getLongParameter(String key, long defaultValue);

	public abstract Object getObjectParameter(String key);

	public abstract String getParameter(String key);

	public abstract String getParameter(String key, String defaultValue);
}
