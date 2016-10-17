package com.generallycloud.nio.codec.http11.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.nio.codec.http11.WebSocketProtocolFactory;
import com.generallycloud.nio.common.BASE64Util;
import com.generallycloud.nio.common.ByteBufferUtil;
import com.generallycloud.nio.common.KMPByteUtil;
import com.generallycloud.nio.common.KMPUtil;
import com.generallycloud.nio.common.SHA1Util;
import com.generallycloud.nio.common.StringLexer;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;

//FIXME 解析BODY中的内容
//FIXME 改进header parser
/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public abstract class AbstractHttpReadFuture extends AbstractIOReadFuture implements HttpReadFuture {

	protected static final KMPByteUtil	KMP_HEADER		= new KMPByteUtil("\r\n\r\n".getBytes());
	protected static final KMPUtil		KMP_BOUNDARY		= new KMPUtil("boundary=");
	
	protected boolean				body_complete;
	protected String				boundary;
	protected int					contentLength;
	protected String				contentType;
	protected List<Cookie>			cookieList;
	protected Map<String, String>	cookies;
	protected boolean				header_complete;
	protected String				host;
	protected String				method;
	protected ByteBuffer			bodyContent;
	protected Map<String, String>	params;
	protected Map<String, String>	request_headers;
	protected String				requestURI;
	protected String				requestURL;
	protected Map<String, String>	response_headers;

	protected String				version;
	protected boolean				hasBodyContent;
	protected IOSession			session;
	protected HttpStatus			status			= HttpStatus.C200;
	protected List<String>			headerLines		= new ArrayList<String>();
	protected StringBuilder			currentHeaderLine	= new StringBuilder();

	public AbstractHttpReadFuture(NIOContext context) {
		super(context);
	}

	public AbstractHttpReadFuture(IOSession session, ByteBuffer readBuffer) {
		super(session.getContext());
		this.session = session;
		this.cookies = new HashMap<String, String>();
		this.request_headers = new HashMap<String, String>();
	}

	public void addCookie(Cookie cookie) {

		if (cookieList == null) {
			cookieList = new ArrayList<Cookie>();
		}

		cookieList.add(cookie);
	}

	protected void decodeBody() {

		if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {
			// FIXME encoding
			String paramString = new String(bodyContent.array(), 0, bodyContent.position(), session.getContext()
					.getEncoding());

			parseParamString(paramString);
		} else {
			// FIXME 解析BODY中的内容
		}

		body_complete = true;
	}

	public String getBoundary() {
		return boundary;
	}

	public int getContentLength() {
		return contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public String getCookie(String name) {
		return cookies.get(name);
	}

	public List<Cookie> getCookieList() {
		return cookieList;
	}

	public String getHost() {
		return host;
	}

	public String getMethod() {
		return method;
	}

	public byte[] getBodyContent() {
		return bodyContent.array();
	}

	public boolean hasBodyContent() {
		return hasBodyContent;
	}

	public String getRequestParam(String key) {
		return params.get(key);
	}

	public Map<String, String> getRequestParams() {
		return params;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public String getFutureName() {
		return getRequestURI();
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getVersion() {
		return version;
	}
	
	private void parse_cookies(String line, Map<String, String> cookies) {
		StringLexer l = new StringLexer(0, line.toCharArray());
		StringBuilder value = new StringBuilder();
		String k = null;
		String v = null;
		boolean findKey = true;
		for (;;) {
			char c = l.current();
			switch (c) {
			case ' ':
				break;
			case '=':
				if (!findKey) {
					throw new IllegalArgumentException();
				}
				k = value.toString();
				value = new StringBuilder();
				findKey = false;
				break;
			case ';':
				if (findKey) {
					throw new IllegalArgumentException();
				}
				findKey = true;
				v = value.toString();
				value = new StringBuilder();
				cookies.put(k, v);
				break;
			default:
				value.append(c);
				break;
			}
			if (!l.next()) {
				break;
			}
		}

		cookies.put(k, value.toString());
	}
	
	protected abstract void parseFirstLine(String line) ;
	
	public void parseHeader(List<String> headerLines) {
		
		parseFirstLine(headerLines.get(0));
		
		for (int i = 1; i < headerLines.size(); i++) {
			
			String l = headerLines.get(i);
			
			int p = l.indexOf(":");
			
			if (p == -1) {
				setRequestHeader(l, null);
				continue;
			}
			
			String name = l.substring(0,p).trim();
			
			String value = l.substring(p+1).trim();
			
			setRequestHeader(name, value);
		}
	}
	
	protected abstract void parseContentType(String contentType);
	
	private void doAfterParseHeader() throws IOException {

		host = getRequestHeader("Host");

		String _contentLength = getRequestHeader("Content-Length");

		int contentLength = 0;
		
		if (!StringUtil.isNullOrBlank(_contentLength)) {
			contentLength = Integer.parseInt(_contentLength);
			this.contentLength = contentLength;
		}

		String contentType = getRequestHeader("Content-Type");
		
		parseContentType(contentType);

		String cookie = getRequestHeader("Cookie");

		if (!StringUtil.isNullOrBlank(cookie)) {
			parse_cookies(cookie, cookies);
		}
		
		if (contentLength < 1) {

			body_complete = true;

		} else if (contentLength < 1024 * 1024) {

			hasBodyContent = true;

			bodyContent = ByteBuffer.allocate(contentLength);
		} else {
			//FIXME 写入临时文件
			throw new IOException("max content 1024 * 1024,content "+contentLength);
		}
	}

	// FIXME 是否会出现第一次读到\r\n结束，下一次loop开头读到\r\n的情况
	public boolean read(IOSession session, ByteBuffer buffer) throws IOException {

		if (!header_complete) {

			for (; buffer.hasRemaining();) {
				byte b = buffer.get();
				if (b == '\n') {
					if (currentHeaderLine.length() == 0) {
						
						header_complete = true;
						
						break;
					} else {
						headerLines.add(currentHeaderLine.toString());
						currentHeaderLine.setLength(0);
					}
					continue;
				} else if (b == '\r') {
					continue;
				} else {
					currentHeaderLine.append((char) b);
				}
			}
			
			if (!header_complete) {
				return false;
			}

			parseHeader(headerLines);

			doAfterParseHeader();			
			
		}

		if (!body_complete) {
			
			if (bodyContent.hasRemaining()) {
				
				ByteBufferUtil.read(bodyContent, buffer);
			}
			
			if (bodyContent.hasRemaining()) {
				return false;
			}
			
			decodeBody();
			
		}

		return true;
	}

	public void setRequestParams(Map<String, String> params) {
		this.params = params;
	}

	public void setReuestParam(String key, String value) {
		if (params == null) {
			params = new HashMap<String, String>();
		}

		this.params.put(key, value);
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public void flush() {

		if (updateWebSocketProtocol) {

			session.setProtocolDecoder(WEBSOCKET_PROTOCOL_DECODER);
			session.setProtocolEncoder(WEBSOCKET_PROTOCOL_ENCODER);
			session.setProtocolFactory(PROTOCOL_FACTORY);

			session.setAttribute(WebSocketReadFuture.SESSION_KEY_SERVICE_NAME, getFutureName());
		}

		super.flush();
	}

	protected static final WebSocketProtocolFactory	PROTOCOL_FACTORY			= new WebSocketProtocolFactory();

	protected static final ProtocolDecoder			WEBSOCKET_PROTOCOL_DECODER	= PROTOCOL_FACTORY
																			.getProtocolDecoder();

	protected static final ProtocolEncoder			WEBSOCKET_PROTOCOL_ENCODER	= PROTOCOL_FACTORY
																			.getProtocolEncoder();

	private boolean							updateWebSocketProtocol;

	public void updateWebSocketProtocol() {

		String Sec_WebSocket_Key = getRequestHeader("Sec-WebSocket-Key");

		if (!StringUtil.isNullOrBlank(Sec_WebSocket_Key)) {

			// 258EAFA5-E914-47DA-95CA-C5AB0DC85B11 必须这个值？

			String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

			byte[] key_array = SHA1Util.SHA1(Sec_WebSocket_Key_Magic);

			String acceptKey = BASE64Util.byteArrayToBase64(key_array);

			setStatus(HttpStatus.C101);
			setResponseHeader("Connection", "Upgrade");
			setResponseHeader("Upgrade", "WebSocket");
			setResponseHeader("Sec-WebSocket-Accept", acceptKey);

			updateWebSocketProtocol = true;
			return;
		}
		throw new IllegalArgumentException("illegal http header : empty Sec-WebSocket-Key");
	}

	public void release() {

	}

	public boolean hasBody() {
		return contentLength > 0;
	}

	public String getRequestHeader(String name) {

		if (StringUtil.isNullOrBlank(name)) {
			return null;
		}

		return request_headers.get(name.toLowerCase());
	}

	public void setRequestHeader(String name, String value) {
		if (StringUtil.isNullOrBlank(name)) {
			return;
		}

		if (request_headers == null) {
			throw new RuntimeException("did you want to set response header ?");
		}

		request_headers.put(name.toLowerCase(), value);
	}

	public void setResponseHeader(String name, String value) {
		if (response_headers == null) {
			response_headers = new HashMap<String, String>();
			setDefaultResponseHeaders(response_headers);
		}
		response_headers.put(name, value);
	}

	protected abstract void setDefaultResponseHeaders(Map<String, String> headers);

	public Map<String, String> getRequestHeaders() {
		return request_headers;
	}

	public Map<String, String> getResponseHeaders() {
		if (response_headers == null) {
			response_headers = new HashMap<String, String>();
			setDefaultResponseHeaders(response_headers);
		}
		return response_headers;
	}

	public void setRequestHeaders(Map<String, String> headers) {
		this.request_headers = headers;
	}

	public void setResponseHeaders(Map<String, String> headers) {
		this.response_headers = headers;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String url) {
		this.requestURL = url;

		int index = url.indexOf("?");

		if (index > -1) {

			String paramString = url.substring(index + 1, url.length());

			parseParamString(paramString);

			requestURI = url.substring(0, index);

		} else {

			this.requestURI = url;
		}
	}

	protected void parseParamString(String paramString) {

		String[] array = paramString.split("&");

		for (String s : array) {

			if (StringUtil.isNullOrBlank(s)) {
				continue;
			}

			String[] unitArray = s.split("=");

			if (unitArray.length != 2) {
				continue;
			}

			String key = unitArray[0];
			String value = unitArray[1];
			params.put(key, value);
		}
	}

}
