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
import com.gifisan.nio.common.KMPUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringLexer;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.future.AbstractReadFuture;

/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR>
 * Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public class DefaultHTTPReadFuture extends AbstractReadFuture implements HTTPReadFuture {

	private String						host;
	private int						contentLength;
	private boolean					header_complete;
	private String						requestURI;
	private ByteBuffer					read_cache;
	private String						method;
	private String						version;
	private String						boundary;
	private String						contentType;
	private String						paramString;
	private boolean					hasOutputStream;
	private OutputStream				outputStream;
	private ByteBuffer					body_buffer;
	private boolean					body_complete;
	private int						read_length		= 0;
	private int						status 			= 200;
	private List<Cookie>				cookieList		= null;
	private List<HttpHeader>				headerList		= null;
	private Map<String, String>			cookies			= new HashMap<String, String>();
	private Map<String, String>			request_headers	= new HashMap<String, String>();
	private Map<String, String>			requestParams 		= new HashMap<String, String>();
	private BufferedOutputStream			header_buffer		= new BufferedOutputStream(1024 * 2);
	
	private static final KMPByteUtil 	KMP_HEADER 		= new KMPByteUtil("\r\n\r\n".getBytes());
	private static final KMPUtil 		KMP_BOUNDARY 		= new KMPUtil("boundary=");
	private static final Logger 		logger 			= LoggerFactory.getLogger(DefaultHTTPReadFuture.class);
	
	private static final HttpHeaderParser	HTTP_HEADER_PARSER	= new HttpHeaderParser();
	private static final String CONTENT_TYPE_URLENCODED 		= "application/x-www-form-urlencoded";
	private static final String CONTENT_TYPE_MULTIPART 		= "multipart/form-data";
	
	public DefaultHTTPReadFuture(Session session, ByteBuffer readBuffer) {
		super(session);
		this.read_cache = readBuffer;
	}

	protected DefaultHTTPReadFuture() {
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	//FIXME 是否会出现第一次读到\r\n结束，下一次loop开头读到\r\n的情况
	public boolean read() throws IOException {

		TCPEndPoint endPoint = this.endPoint;

		ByteBuffer buffer = this.read_cache;
		
		buffer.clear();
		
		return decode(endPoint, buffer);
	}
	
	public boolean decode(TCPEndPoint endPoint,ByteBuffer buffer) throws IOException{
		
		if (!header_complete) {
			
			endPoint.read(buffer);
			
			BufferedOutputStream header_buffer = this.header_buffer;
			
			int length = buffer.position();
			
			byte [] source_array = buffer.array();
			
			int pos = KMP_HEADER.match(source_array, 0, length);
			
			if (pos == -1) {
				header_buffer.write(source_array, 0, length);
				
				return false;
			}else{
				
				pos +=4;
				
				header_buffer.write(source_array,0,pos);
				
				doHeaderComplete(header_buffer);
				
				if (CONTENT_TYPE_URLENCODED.equals(contentType)) {
					
					if ("GET".equals(method)) {
						
						int index = requestURI.indexOf("?");
						
						if (index > -1) {
							paramString = requestURI.substring(index+1, requestURI.length());
							
							parseParamString(paramString);
							
							requestURI = requestURI.substring(0,index);
						}
						
						body_complete = true;
						
					}else{
						
						outputStream = new BufferedOutputStream(contentLength);
						
						read_length = length - pos;
						
						outputStream.write(source_array, pos, read_length);
					}
				}else if(CONTENT_TYPE_MULTIPART.equals(contentType)){
					
					this.hasOutputStream = true;
					
					int bufferLength = 1024 * 256;

					bufferLength = contentLength > bufferLength ? bufferLength : contentLength;

					this.body_buffer = ByteBuffer.allocate(bufferLength);
					
					IOEventHandleAdaptor eventHandle = session.getContext().getIOEventHandleAdaptor();
					
					try {
						eventHandle.acceptAlong(session, this);
					} catch (Exception e) {
						logger.debug(e);
					}
					
					if (this.outputStream == null) {
						
						buffer.clear();
						
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
			
			if(read_length == contentLength) {
				
				if(!hasOutputStream){
					
					BufferedOutputStream o = (BufferedOutputStream)outputStream;
					
					paramString = new String(o.toByteArray(),session.getContext().getEncoding());
				
					parseParamString(paramString);
				} 
				
				body_complete = true;
				
				return true;
			}
			
			return false;
		}
		
		return true;
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

	private void parseParamString(String paramString){
		String [] array = paramString.split("&");
		for(String s : array){
			
			if (StringUtil.isNullOrBlank(s)) {
				continue;
			}
			
			String [] unitArray = s.split("=");
			
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

		HTTP_HEADER_PARSER.parseHeader(header_string, this);
	}

	private static class HttpHeaderParser {

		private static final byte	R	= '\r';

		private static final byte	N	= '\n';

		private void parseFirstLine(StringLexer lexer, DefaultHTTPReadFuture future) {

			int index = 0;
			String[] array = new String[3];
			StringBuilder builder = new StringBuilder();
			for (;;) {
				char c = lexer.current();
				if (R == c) {
					array[index++] = builder.toString();
					builder = new StringBuilder();
					lexer.next();
					lexer.next();
					break;
				} else if (' ' == c) {
					array[index++] = builder.toString();
					builder = new StringBuilder();
					lexer.next();
				} else {
					builder.append(lexer.current());
					lexer.next();
				}
			}

			if (index < 3) {
				throw new IllegalArgumentException("http header first line breaked");
			}

			future.method = array[0];
			future.requestURI = array[1];
			future.version = array[2];
		}

		private void parseHeader(String content,DefaultHTTPReadFuture future) {
			Map<String, String> headers = future.request_headers;
			StringLexer lexer = new StringLexer(0, content.toCharArray());
			parseFirstLine(lexer, future);
			StringBuilder value = new StringBuilder();
			String k = null;
			for (;;) {
				char c = lexer.current();
				switch (c) {
				case ':':
					k = value.toString();
					value = new StringBuilder();
					lexer.next();
					if (' ' != lexer.current()) {
						lexer.previous();
					}
					headers.put(k, findHeaderValue(lexer));
					break;
				default:
					value.append(c);
					break;
				}
				if (!lexer.next()) {
					break;
				}
			}
			
			doAfterParseHeader(future);
		}
		
		private String findHeaderValue(StringLexer lexer){
			StringBuilder value = new StringBuilder();
			for (;lexer.next();) {
				char c = lexer.current();
				switch (c) {
				case R:
					break;
				case N:
					return value.toString();
				default:
					value.append(c);
					break;
				}
			}
			return value.toString();
		}

		private void doAfterParseHeader(DefaultHTTPReadFuture future){
			
			Map<String,String> headers = future.request_headers;
			
			future.host = headers.get("Host");
			
			String _contentLength = headers.get("Content-Length");

			if (!StringUtil.isNullOrBlank(_contentLength)) {
				future.contentLength = Integer.parseInt(_contentLength);
			}
			
			String contentType = headers.get("Content-Type");
			
			if (!StringUtil.isNullOrBlank(contentType)) {
				
				if (CONTENT_TYPE_URLENCODED.equals(contentType)) {
					
					future.contentType = CONTENT_TYPE_URLENCODED;
				
				}else if(contentType.startsWith("multipart/form-data;")){
					
					int index = KMP_BOUNDARY.match(contentType);
					
					if (index != -1) {
						future.boundary = contentType.substring(index + 9);
					}
					
					future.contentType = CONTENT_TYPE_MULTIPART;
				}else{
					logger.error("unsupport content type:"+contentType);
				}
			}else{
				future.contentType = CONTENT_TYPE_URLENCODED;
			}
			
			String cookie = headers.get("Cookie");
			
			if (!StringUtil.isNullOrBlank(cookie)) {
				parseLine(cookie, future.cookies);
			}
		}
		
		private void parseLine(String line,Map<String,String> map){
			StringLexer l = new StringLexer(0, line.toCharArray());
			StringBuilder value = new StringBuilder();
			String k = null;
			String v = null;
			boolean findKey = true;
			for(;;){
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
					map.put(k, v);
					break;
				default:
					value.append(c);
					break;
				}
				if(!l.next()){
					break;
				}
			}
			
			map.put(k, value.toString());
		}
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
	
	public String getRequestParam(String key){
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
	
	public void addCookie(Cookie cookie){
		
		if (cookieList == null) {
			cookieList = new ArrayList<Cookie>();
		}
		
		cookieList.add(cookie);
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
		
		String line = "POST /test/test.htm HTTP/1.1\r\n"+
					"Cookie: test222=222222; JSESSIONID=A9EF49BF15EE32B8A120F4D743D42EE3\r\n"+
					"Content-Length: 1325";
		
		line = "Cookie: test222=222222; JSESSIONID=A9EF49BF15EE32B8A120F4D743D42EE3\r\n"+
				"Content-Length: 1325";
		
		DefaultHTTPReadFuture future = new DefaultHTTPReadFuture();
		
		HTTP_HEADER_PARSER.parseHeader(line, future);
		
		System.out.println(JSON.toJSONString(future, true));
	}
	

}
