package com.generallycloud.nio.codec.http11.future;

import java.util.List;
import java.util.Map;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.NamedReadFuture;

public abstract interface HttpReadFuture extends NamedReadFuture {

	public abstract String getRequestHeader(String name);
	
	public abstract void setRequestHeader(String name,String value);
	
	public abstract void setResponseHeader(String name,String value);
	
	public abstract Map<String, String> getRequestHeaders();
	
	public abstract Map<String, String> getResponseHeaders();
	
	public abstract void setRequestHeaders(Map<String, String> headers);
	
	public abstract void setResponseHeaders(Map<String, String> headers);

	public abstract String getHost();

	public abstract int getContentLength();

	/**
	 * <table summary="Examples of Returned Values">
	 * <tr align=left>
	 * <th>First line of HTTP request</th>
	 * <th>Returned Value</th>
	 * <tr>
	 * <td>POST /some/path.html HTTP/1.1
	 * <td>
	 * <td>/some/path.html
	 * <tr>
	 * <td>GET http://foo.bar/a.html HTTP/1.0
	 * <td>
	 * <td>/a.html
	 * <tr>
	 * <td>GET /xyz?a=b HTTP/1.1
	 * <td>
	 * <td>/xyz
	 * </table>
	 */
	public abstract String getRequestURI();
	
	public abstract String getRequestURL();
	
	public abstract void setRequestURL(String url);
	
	public abstract List<Cookie> getCookieList();

	public abstract String getMethod();

	public abstract String getVersion();

	public abstract String getBoundary();

	public abstract String getContentType();
	
	public abstract Map<String, String> getRequestParams();
	
	public abstract String getRequestParam(String key);

	public abstract void setReuestParam(String key,String value);
	
	public abstract void setRequestParams(Map<String, String> params);
	
	public abstract BufferedOutputStream getBody();
	
	public abstract boolean hasBody();

	public abstract HttpStatus getStatus();

	public abstract void setStatus(HttpStatus status);

	public abstract String getCookie(String name);
	
	public abstract void addCookie(Cookie cookie);
	
	public abstract void updateWebSocketProtocol();
}
