package com.generallycloud.nio.codec.http11.future;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.protocol.AbstractIOReadFuture;

public class HttpRequestFutureImpl extends AbstractIOReadFuture implements HttpRequestFuture {

	private String				url;

	private String				requestURI;

	private String				method;

	private Map<String, String>	cookies;

	private Map<String, String>	headers;

	private Map<String, String>	params;

	public HttpRequestFutureImpl(String url, String method) {
		this.url = url;
		this.method = method;
	}

	public String getFutureName() {
		return requestURI;
	}

	public String getUrl() {
		return url;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public String getMethod() {
		return method;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}
	
	public void setCookie(String name,String value){
		if(cookies == null){
			cookies = new HashMap<String, String>();
		}
		cookies.put(name, value);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	public void setHeader(String name,String value){
		if(headers == null){
			headers = new HashMap<String, String>();
		}
		headers.put(name, value);
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
	public void setParam(String name,String value){
		if(params == null){
			params = new HashMap<String, String>();
		}
		params.put(name, value);
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public boolean read() throws IOException {
		return true;
	}

	public void release() {
		
	}
	
}
