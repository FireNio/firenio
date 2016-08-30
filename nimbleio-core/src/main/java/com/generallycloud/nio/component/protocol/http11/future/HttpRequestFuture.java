package com.generallycloud.nio.component.protocol.http11.future;

import java.util.Map;

import com.generallycloud.nio.component.protocol.ReadFuture;

public interface HttpRequestFuture extends ReadFuture{

	public abstract String getUrl();

	public abstract String getRequestURI();

	public abstract String getMethod();

	public abstract Map<String, String> getCookies();

	public abstract void setCookies(Map<String, String> cookies);

	public abstract void setCookie(String name, String value);

	public abstract Map<String, String> getHeaders();

	public abstract void setHeaders(Map<String, String> headers);

	public abstract void setHeader(String name, String value);

	public abstract Map<String, String> getParams();

	public abstract void setParams(Map<String, String> params);

	public abstract void setParam(String name, String value);

	public abstract void setRequestURI(String requestURI);

}