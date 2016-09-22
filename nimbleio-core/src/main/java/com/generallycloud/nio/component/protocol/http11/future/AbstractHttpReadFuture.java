package com.generallycloud.nio.component.protocol.http11.future;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.nio.common.BASE64Util;
import com.generallycloud.nio.common.KMPByteUtil;
import com.generallycloud.nio.common.SHA1Util;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.protocol.AbstractIOReadFuture;
import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.http11.WebSocketProtocolFactory;

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
	protected BufferedOutputStream	outputStream;
	protected Map<String, String>	params;
	protected ByteBuffer			read_buffer;
	protected int					read_length		= 0;
	protected Map<String, String>	request_headers	= new HashMap<String, String>();
	protected String				requestURI;
	protected Map<String, String>	response_headers;

	protected HttpStatus			status			= HttpStatus.C200;
	protected String				version;
	protected boolean 			hasOutputStream;

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
	
	public boolean decode(SocketChannel channel, ByteBuffer buffer) throws IOException {

		try {

			if (!header_complete) {

				channel.read(buffer);

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

					ByteBuffer body_buffer = this.body_buffer;

					body_buffer.clear();

					channel.read(body_buffer);

					fill(outputStream, body_buffer);
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
	
	public boolean isHasOutputStream() {
		return hasOutputStream;
	}

	public void setHasOutputStream(boolean hasOutputStream) {
		this.hasOutputStream = hasOutputStream;
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
	
	public BufferedOutputStream getBody() {
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

	public String getFutureName() {
		return requestURI;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getVersion() {
		return version;
	}

	// FIXME 是否会出现第一次读到\r\n结束，下一次loop开头读到\r\n的情况
	public boolean read() throws IOException {

		SocketChannel channel = this.channel;

		ByteBuffer buffer = this.read_buffer;

		return decode(channel, buffer);
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
			
			channel.setProtocolDecoder(WEBSOCKET_PROTOCOL_DECODER);
			channel.setProtocolEncoder(WEBSOCKET_PROTOCOL_ENCODER);
			channel.setProtocolFactory(PROTOCOL_FACTORY);
			
			session.setAttribute(WebSocketReadFuture.SESSION_KEY_SERVICE_NAME, getFutureName());
		}
		
		super.flush();
	}
	
	private static final WebSocketProtocolFactory PROTOCOL_FACTORY = new WebSocketProtocolFactory();

	private static final ProtocolDecoder WEBSOCKET_PROTOCOL_DECODER = PROTOCOL_FACTORY.getProtocolDecoder();
	
	private static final ProtocolEncoder WEBSOCKET_PROTOCOL_ENCODER = PROTOCOL_FACTORY.getProtocolEncoder();
	
	private boolean updateWebSocketProtocol;
	
	public void updateWebSocketProtocol() {
		
		String Sec_WebSocket_Key = getHeader("Sec-WebSocket-Key");

		if (!StringUtil.isNullOrBlank(Sec_WebSocket_Key)) {
			
			//258EAFA5-E914-47DA-95CA-C5AB0DC85B11 必须这个值？
			
			String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
			
			byte[] key_array = SHA1Util.SHA1(Sec_WebSocket_Key_Magic);

			String acceptKey = BASE64Util.byteArrayToBase64(key_array);

			setStatus(HttpStatus.C101);
			setHeader("Connection", "Upgrade");
			setHeader("Upgrade", "WebSocket");
			setHeader("Sec-WebSocket-Accept", acceptKey);
			
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
	
}
