package com.gifisan.nio.component.protocol.http11.future;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gifisan.nio.common.KMPByteUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.future.AbstractIOReadFuture;

//FIXME 解析BODY中的内容
/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public abstract class AbstractHttpReadFuture extends AbstractIOReadFuture implements HttpReadFuture {

	protected static final KMPByteUtil	KMP_HEADER		= new KMPByteUtil("\r\n\r\n".getBytes());
	protected ByteBuffer			body_buffer;
	protected boolean				body_complete;
	protected String				boundary;
	protected int					contentLength;
	protected String				contentType;
	protected List<Cookie>			cookieList;
	protected Map<String, String>	cookies			= new HashMap<String, String>();
	protected BufferedOutputStream	header_buffer		= new BufferedOutputStream(1024 * 2);
	protected boolean				header_complete;
	protected String				host;
	protected HttpHeaderParser		httpHeaderParser;
	protected String				method;
	protected OutputStream			outputStream;
	protected Map<String, String>	params;
	protected ByteBuffer			read_buffer;
	protected int					read_length		= 0;
	protected Map<String, String>	request_headers	= new HashMap<String, String>();
	protected String				requestURI;
	protected Map<String, String>	response_headers;

	protected int					status			= 200;
	protected String				version;

	public AbstractHttpReadFuture(Session session, HttpHeaderParser httpHeaderParser,ByteBuffer readBuffer) {
		super(session);
		this.httpHeaderParser = httpHeaderParser;
		this.read_buffer = readBuffer;
	}

	public void addCookie(Cookie cookie) {

		if (cookieList == null) {
			cookieList = new ArrayList<Cookie>();
		}

		cookieList.add(cookie);
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

					decodeHeader(source_array, length, pos);
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

					decodeBody();

					return true;
				}

				return false;
			}

			return true;

		} finally {

			buffer.clear();
		}
	}

	protected abstract void decodeBody();

	protected abstract void decodeHeader(byte[] srouce_array, int length, int pos) throws IOException;

	private void doHeaderComplete(BufferedOutputStream header_buffer) throws UnsupportedEncodingException {

		header_complete = true;

		String header_string = header_buffer.toString(session.getContext().getEncoding());

		httpHeaderParser.parseHeader(header_string, this);
	}

	protected void fill(OutputStream outputStream, ByteBuffer buffer) throws IOException {

		byte[] array = buffer.array();

		int length = buffer.position();

		if (length == 0) {
			return;
		}

		read_length += length;

		outputStream.write(array, 0, buffer.position());
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

	public String getHeader(String name) {
		return request_headers.get(name);
	}

	public Map<String, String> getHeaders() {
		return response_headers;
	}

	public String getHost() {
		return host;
	}

	public String getMethod() {
		return method;
	}

	public OutputStream getOutputStream() {
		return outputStream;
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

	public String getServiceName() {
		return requestURI;
	}

	public int getStatus() {
		return status;
	}

	public String getVersion() {
		return version;
	}

	// FIXME 是否会出现第一次读到\r\n结束，下一次loop开头读到\r\n的情况
	public boolean read() throws IOException {

		TCPEndPoint endPoint = this.endPoint;

		ByteBuffer buffer = this.read_buffer;

		return decode(endPoint, buffer);
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
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

	public void setStatus(int status) {
		this.status = status;
	}
}
