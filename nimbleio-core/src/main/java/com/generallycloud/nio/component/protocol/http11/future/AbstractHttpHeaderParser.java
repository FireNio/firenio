package com.generallycloud.nio.component.protocol.http11.future;

import java.util.Map;

import com.generallycloud.nio.common.KMPUtil;
import com.generallycloud.nio.common.StringLexer;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BufferedOutputStream;

public abstract class AbstractHttpHeaderParser implements HttpHeaderParser {

	protected final byte		R					= '\r';
	protected final byte		N					= '\n';
	protected KMPUtil			KMP_BOUNDARY			= new KMPUtil("boundary=");

	protected abstract void parseFirstLine(StringLexer lexer, AbstractHttpReadFuture future) ;

	public void parseHeader(String content, AbstractHttpReadFuture future) {
		
		Map<String, String> headers = future.request_headers;
		
		StringLexer lexer = new StringLexer(0, content.toCharArray());
		
		parseFirstLine(lexer, future);
		
		StringBuilder value = new StringBuilder();
		
		String k = null;
		
		for (;;) {
			
			char c = lexer.current();
			
			if (':' == c) {
				
				k = value.toString();
				
				value = new StringBuilder();
				
				lexer.next();
				
				if (' ' != lexer.current()) {
					
					lexer.previous();
				}
				
				headers.put(k, findHeaderValue(lexer));
			}else{
				
				value.append(c);
			}
			
			if (!lexer.next()) {
				
				break;
			}
		}

		doAfterParseHeader(future);
	}

	private String findHeaderValue(StringLexer lexer) {
		
		StringBuilder value = new StringBuilder();
		
		for (; lexer.next();) {
			
			char c = lexer.current();
			
			if (R == c) {
				continue;
			}else if(N == c){
				return value.toString();
			}else{
				value.append(c);
			}
		}
		return value.toString();
	}

	private void doAfterParseHeader(AbstractHttpReadFuture future) {

		Map<String, String> headers = future.request_headers;

		future.host = headers.get("Host");

		String _contentLength = headers.get("Content-Length");

		if (!StringUtil.isNullOrBlank(_contentLength)) {
			future.contentLength = Integer.parseInt(_contentLength);
		}

		String contentType = headers.get("Content-Type");
		
		parseContentType(future,contentType);

		String cookie = headers.get("Cookie");

		if (!StringUtil.isNullOrBlank(cookie)) {
			parse_cookies(cookie, future.cookies);
		}
		
		future.outputStream = new BufferedOutputStream(future.contentLength);
	}
	
	protected abstract void parseContentType(AbstractHttpReadFuture future,String contentType);

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
}
