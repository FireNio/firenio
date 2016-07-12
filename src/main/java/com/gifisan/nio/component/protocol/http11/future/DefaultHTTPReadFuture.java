package com.gifisan.nio.component.protocol.http11.future;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.FileUtil;
import com.gifisan.nio.common.KMPByteUtil;
import com.gifisan.nio.common.KMPUtil;
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
	private Map<String, String>			headers			= new HashMap<String, String>();
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

	private void test(String content) {

		HTTP_HEADER_PARSER.parseHeader(content, this);
	}

	private static class HttpHeaderParser {

		private static final byte	R	= '\r';

		private static final byte	N	= '\n';

		private static final byte	C	= ':';

		private static final byte	S	= ' ';

		private String createUnexpectExceptionMessage(char token, int index) {
			return new StringBuilder("unexpect token ").append(token).append(",at index ").append(index).toString();
		}

		private void parseFirstLine(Lexer lexer, DefaultHTTPReadFuture future) {

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
				} else if (S == c) {
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

		private String parseMapValue(Lexer lexer) {
			StringBuilder builder = new StringBuilder();
			do {
				char ch = lexer.current();
				if (ch == R || ch == N) {
					lexer.previous();
					break;
				} else {
					builder.append(ch);
				}
			} while (lexer.next());
			return builder.toString();
		}

		public void parseHeader(String content, DefaultHTTPReadFuture future) {
			Map<String, String> headers = future.headers;
			Lexer lexer = new Lexer(0, content.toCharArray());
			parseFirstLine(lexer, future);
			String key = findMapKey(lexer);
			String value = null;
			boolean findKey = false;
			while (lexer.next()) {
				char ch = lexer.current();
				switch (ch) {
				case C:
					if (findKey) {
						throw new IllegalArgumentException(createUnexpectExceptionMessage(ch,
								lexer.currentIndex()));
					} else {
						lexer.next();
						lexer.next();
						value = parseMapValue(lexer);
						findKey = true;
						headers.put(key, value);
					}
					break;
				case R:
					if (!findKey) {
						throw new RuntimeException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
					} else {
						lexer.next();
						if (!lexer.next()) {
							break;
						}
						key = findMapKey(lexer);
						findKey = false;

					}
					break;
				default:
					throw new RuntimeException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
				}
			}

			future.host = headers.get("Host");
			
			String _contentLength = headers.get("Content-Length");

			if (!StringUtil.isNullOrBlank(_contentLength)) {
				future.contentLength = Integer.parseInt(_contentLength);
			}
			
			String contentType = headers.get("Content-Type");
			
			if (!StringUtil.isNullOrBlank(contentType)) {
				
				if ("application/x-www-form-urlencoded".equals(contentType)) {
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
		}

		private String findMapKey(Lexer lexer) {
			StringBuilder builder = new StringBuilder();
			do {
				char ch = lexer.current();
				if (ch == C) {
					lexer.previous();
					return builder.toString();
				} else {
					builder.append(ch);
				}
			} while (lexer.next());
			return builder.toString();
		}

		private class Lexer {

			private int	index	= 0;
			private char[]	codes	= null;

			public Lexer(int index, char[] codes) {
				this.index = index;
				this.codes = codes;
			}

			public void previous() {
				index--;
			}

			public boolean next() {
				return ++index < codes.length;
			}

			public char current() {
				return codes[index];
			}

			public int currentIndex() {
				return index;
			}

			public String toString() {
				return new String("current:" + this.index + "," + this.current());
			}
		}
	}

	public Map<String, String> getHeaders() {
		return headers;
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

	public static void main(String[] args) throws IOException {
		
		KMPByteUtil kmpUtil = new KMPByteUtil("\r\n\r\n".getBytes());
		
		byte [] source_array = new byte[4096];
		
		int pos = kmpUtil.match(source_array, 0, source_array.length);
	
		System.out.println(pos);
	}

}
