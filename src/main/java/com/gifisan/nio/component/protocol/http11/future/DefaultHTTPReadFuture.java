package com.gifisan.nio.component.protocol.http11.future;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.gifisan.nio.common.KMPByteUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.future.AbstractReadFuture;

/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public class DefaultHTTPReadFuture extends AbstractReadFuture implements HTTPReadFuture {

	private boolean				header_complete;
	private ByteBuffer				read_buffer;
	private String					paramString;
	private boolean				hasOutputStream;
	private OutputStream			outputStream;
	private ByteBuffer				body_buffer;
	private boolean				body_complete;
	private int					read_length		= 0;
	private List<Cookie>			cookieList		= null;
	private List<HttpHeader>			headerList		= null;
	private Map<String, String>		requestParams		= new HashMap<String, String>();
	protected String				host;
	protected int					status			= 200;
	protected int					contentLength;
	protected String				requestURI;
	protected String				method;
	protected String				version;
	protected String				boundary;
	protected String				contentType;
	protected Map<String, String>	cookies			= new HashMap<String, String>();
	protected Map<String, String>	request_headers	= new HashMap<String, String>();
	protected BufferedOutputStream	header_buffer		= new BufferedOutputStream(1024 * 2);

	private static final Logger		logger			= LoggerFactory.getLogger(DefaultHTTPReadFuture.class);
	private static final KMPByteUtil	KMP_HEADER		= new KMPByteUtil("\r\n\r\n".getBytes());
	private HttpHeaderParser			httpHeaderParser;

	public DefaultHTTPReadFuture(HttpHeaderParser headerParser, Session session,ByteBuffer buffer) {
		super(session);
		this.httpHeaderParser = headerParser;
		this.read_buffer = buffer;
	}

	public DefaultHTTPReadFuture(Session session, String url, String method) {
		super(session);
		this.requestURI = url;
		this.method = method;
	}

	protected DefaultHTTPReadFuture() {
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	// FIXME 是否会出现第一次读到\r\n结束，下一次loop开头读到\r\n的情况
	public boolean read() throws IOException {

		TCPEndPoint endPoint = this.endPoint;

		ByteBuffer buffer = this.read_buffer;

		return decode(endPoint, buffer);
	}

	public boolean decode(TCPEndPoint endPoint, ByteBuffer buffer) throws IOException {

		try {
			
			if (!header_complete) {

				endPoint.read(buffer);

				BufferedOutputStream header_buffer = this.header_buffer;

				int length = buffer.position();

				byte[] source_array = buffer.array();

				int pos = KMP_HEADER.match(source_array, 0, length);

				if (pos == -1) {

					header_buffer.write(source_array, 0, length);

					return false;

				} else {

					pos += 4;

					header_buffer.write(source_array, 0, pos);

					doHeaderComplete(header_buffer);

					int index = requestURI.indexOf("?");

					if (index > -1) {
						paramString = requestURI.substring(index + 1, requestURI.length());

						parseParamString(paramString);

						requestURI = requestURI.substring(0, index);
					}

					if (contentLength < 1) {

						body_complete = true;

					} else if (contentLength > 1 << 21) {

						outputStream = new BufferedOutputStream(contentLength);

						read_length = length - pos;

						outputStream.write(source_array, pos, read_length);

					} else {

						this.hasOutputStream = true;

						this.body_buffer = ByteBuffer.allocate(1024 * 256);

						IOEventHandleAdaptor eventHandle = session.getContext().getIOEventHandleAdaptor();

						try {
							eventHandle.acceptAlong(session, this);
						} catch (Exception e) {
							logger.debug(e);
						}

						if (this.outputStream == null) {

							throw new IOException("none outputstream");
						}

						read_length = length - pos;

						outputStream.write(source_array, pos, read_length);
					}
				}
			}

			if (!body_complete) {

				if (read_length < contentLength) {

					buffer = body_buffer;

					buffer.clear();

					endPoint.read(buffer);

					fill(outputStream, buffer);
				}

				if (read_length == contentLength) {

					BufferedOutputStream o = (BufferedOutputStream) outputStream;

					if (!hasOutputStream && o.size() > 0) {

						paramString = new String(o.toByteArray(), session.getContext().getEncoding());

						parseParamString(paramString);
					}

					body_complete = true;

					return true;
				}

				return false;
			}

			return true;
			
		} finally {
			
			buffer.clear();
		}
	}

	private void fill(OutputStream outputStream, ByteBuffer buffer) throws IOException {

		byte[] array = buffer.array();

		int length = buffer.position();

		if (length == 0) {
			return;
		}

		read_length += length;

		outputStream.write(array, 0, buffer.position());
	}

	public boolean hasOutputStream() {
		return hasOutputStream;
	}

	private void parseParamString(String paramString) {
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
			requestParams.put(key, value);
		}
	}

	private void doHeaderComplete(BufferedOutputStream header_buffer) throws UnsupportedEncodingException {

		header_complete = true;

		String header_string = header_buffer.toString(session.getContext().getEncoding());

		httpHeaderParser.parseHeader(header_string, this);
	}

	public List<HttpHeader> getHeaderList() {
		return headerList;
	}

	public String getHost() {
		return host;
	}

	public int getContentLength() {
		return contentLength;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public String getMethod() {
		return method;
	}

	public String getVersion() {
		return version;
	}

	public String getBoundary() {
		return boundary;
	}

	public String getContentType() {
		return contentType;
	}

	public Map<String, String> getRequestParams() {
		return requestParams;
	}

	public String getRequestParam(String key) {
		return requestParams.get(key);
	}

	public String getParamString() {
		return paramString;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public String getServiceName() {
		return requestURI;
	}

	public void addCookie(Cookie cookie) {

		if (cookieList == null) {
			cookieList = new ArrayList<Cookie>();
		}

		cookieList.add(cookie);
	}
	
	protected ByteBuffer getReadBuffer() {
		return read_buffer;
	}

	public List<Cookie> getCookieList() {
		return cookieList;
	}

	public String getCookie(String name) {
		return cookies.get(name);
	}

	public String getHeader(String name) {
		return request_headers.get(name);
	}

	public void addHeader(String name, String value) {

		if (headerList == null) {
			headerList = new ArrayList<HttpHeader>();
		}

		headerList.add(new HttpHeader(name, value));
	}

	public static void main(String[] args) throws IOException {

		String line = "POST /test/test.htm HTTP/1.1\r\n"
				+ "Cookie: test222=222222; JSESSIONID=A9EF49BF15EE32B8A120F4D743D42EE3\r\n"
				+ "Content-Length: 1325";

		line = "Cookie: test222=222222; JSESSIONID=A9EF49BF15EE32B8A120F4D743D42EE3\r\n" + "Content-Length: 1325";

		DefaultHTTPReadFuture future = new DefaultHTTPReadFuture();

		ServerHttpHeaderParser httpParser = new ServerHttpHeaderParser();

		httpParser.parseHeader(line, future);

		System.out.println(JSON.toJSONString(future, true));
	}

}
