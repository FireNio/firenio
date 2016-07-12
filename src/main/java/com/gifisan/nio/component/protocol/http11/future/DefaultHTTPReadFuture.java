package com.gifisan.nio.component.protocol.http11.future;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.FileUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.future.AbstractReadFuture;

public class DefaultHTTPReadFuture extends AbstractReadFuture implements HTTPReadFuture {

	private static final byte	R				= '\r';
	private static final byte	N				= '\n';

	private Map<String, String>	headers			= new HashMap<String, String>();
	private boolean			header_complete;
	private String				host;
	private int				contentLength;
	private String				requestURI;
	private ByteBuffer			read_cache;
	private String				method;
	private String				version;
	private BufferedOutputStream	header_buffer		= new BufferedOutputStream();
	private static final HttpHeaderParser		httpHeaderParser	= new HttpHeaderParser();

	public DefaultHTTPReadFuture(Session session) {
		super(session);
	}
	
	

	protected DefaultHTTPReadFuture() {
	}



	public boolean read() throws IOException {

		TCPEndPoint endPoint = this.endPoint;

		ByteBuffer buffer = this.read_cache;

		if (!header_complete) {
			BufferedOutputStream header_buffer = this.header_buffer;

			endPoint.read(buffer);

			for (; buffer.hasRemaining();) {

				if (R == buffer.get() && N == buffer.get() && R == buffer.get() && N == buffer.get()) {
					header_buffer.write(buffer.array(), 0, buffer.position());
					header_complete = true;
					break;
				}
			}

			if (header_complete) {

				doHeaderComplete();

			} else {

				header_buffer.write(buffer.array(), 0, buffer.limit());

				return false;
			}
		}

		if (contentLength > 1024 * 1024 * 2) {

		} else {
			
		}
		return false;
	}

	private void doHeaderComplete() throws UnsupportedEncodingException {

		String header_string = header_buffer.toString(session.getContext().getEncoding());

		httpHeaderParser.parseHeader(header_string, this);

	}
	
	private void test(String content){
		
		httpHeaderParser.parseHeader(content, this);
	}
	
	
	private static class HttpHeaderParser {
		
		private static final byte	R			= '\r';

		private static final byte	N			= '\n';

		private static final byte	C			= ':';
		
		private static final byte	S			= ' ';

		private String createUnexpectExceptionMessage(char token, int index) {
			return new StringBuilder("unexpect token ").append(token).append(",at index ").append(index).toString();
		}
		
		private void parseFirstLine(Lexer lexer,DefaultHTTPReadFuture future){
			
			int index = 0;
			String [] array = new String [3];
			StringBuilder builder = new StringBuilder();
			for(;;){
				char c = lexer.current();
				if (R == c) {
					array[index++] = builder.toString();
					builder = new StringBuilder();
					lexer.next();
					lexer.next();
					break;
				}else if(S == c){
					array[index++] = builder.toString();
					builder = new StringBuilder();
					lexer.next();
				}else {
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

		public void parseHeader(String content,DefaultHTTPReadFuture future) {
			Map<String,String> headers = future.headers;
			Lexer lexer = new Lexer(0, content.toCharArray());
			parseFirstLine(lexer,future);
			String key = findMapKey(lexer);
			String value = null;
			boolean findKey = false;
			while (lexer.next()) {
				char ch = lexer.current();
				switch (ch) {
				case C:
					if (findKey) {
						throw new IllegalArgumentException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
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
			
			if(!StringUtil.isNullOrBlank(_contentLength)){
				future.contentLength = Integer.parseInt(_contentLength);
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



	public static void main(String[] args) throws IOException {
		
		DefaultHTTPReadFuture defaultHTTPReadFuture = new DefaultHTTPReadFuture();

		String content = FileUtil.readContentByCls("test.f", Encoding.UTF8);

		defaultHTTPReadFuture.test(content);

		System.out.println(JSON.toJSONString(defaultHTTPReadFuture.headers,true));
	}

	
	
}
