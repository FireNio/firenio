package com.generallycloud.nio.component.protocol.http11.future;

import java.util.Map;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.protocol.NamedReadFuture;

public abstract interface HttpReadFuture extends NamedReadFuture {

	public abstract String getHeader(String name);

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

	public abstract String getMethod();

	public abstract String getVersion();

	public abstract String getBoundary();

	public abstract String getContentType();
	
	public abstract Map<String, String> getRequestParams();
	
	public abstract Map<String, String> getHeaders();

	public abstract String getRequestParam(String key);

	public abstract void setReuestParam(String key,String value);
	
	public abstract void setRequestParams(Map<String, String> params);
	
	public abstract BufferedOutputStream getBody();
	
	public abstract boolean hasBody();

	public abstract HttpStatus getStatus();

	public abstract void setStatus(HttpStatus status);

	public abstract String getCookie(String name);
	
	public abstract void addCookie(Cookie cookie);
	
	public abstract void setHeader(String name,String value);
	
	public abstract void updateWebSocketProtocol();
}
