package com.gifisan.nio.component.protocol.http11.future;

import java.util.Map;

import com.gifisan.nio.common.KMPUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringLexer;
import com.gifisan.nio.common.StringUtil;

public abstract class AbstractHttpHeaderParser implements HttpHeaderParser {

	public static final String	CONTENT_TYPE_URLENCODED	= "application/x-www-form-urlencoded";
	public static final String	CONTENT_TYPE_MULTIPART		= "multipart/form-data";

	protected final byte		R					= '\r';
	protected final byte		N					= '\n';
	private KMPUtil			KMP_BOUNDARY			= new KMPUtil("boundary=");
	private Logger				logger				= LoggerFactory.getLogger(AbstractHttpHeaderParser.class);

	protected abstract void parseFirstLine(StringLexer lexer, DefaultHTTPReadFuture future) ;

	public void parseHeader(String content, DefaultHTTPReadFuture future) {
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

	private String findHeaderValue(StringLexer lexer) {
		StringBuilder value = new StringBuilder();
		for (; lexer.next();) {
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

	private void doAfterParseHeader(DefaultHTTPReadFuture future) {

		Map<String, String> headers = future.request_headers;

		future.host = headers.get("Host");

		String _contentLength = headers.get("Content-Length");

		if (!StringUtil.isNullOrBlank(_contentLength)) {
			future.contentLength = Integer.parseInt(_contentLength);
		}

		String contentType = headers.get("Content-Type");

		if (!StringUtil.isNullOrBlank(contentType)) {

			if (CONTENT_TYPE_URLENCODED.equals(contentType)) {

				future.contentType = CONTENT_TYPE_URLENCODED;

			} else if (contentType.startsWith("multipart/form-data;")) {

				int index = KMP_BOUNDARY.match(contentType);

				if (index != -1) {
					future.boundary = contentType.substring(index + 9);
				}

				future.contentType = CONTENT_TYPE_MULTIPART;
			} else {
				logger.error("unsupport content type:" + contentType);
			}
		} else {
			future.contentType = CONTENT_TYPE_URLENCODED;
		}

		String cookie = headers.get("Cookie");

		if (!StringUtil.isNullOrBlank(cookie)) {
			parseLine(cookie, future.cookies);
		}
	}

	private void parseLine(String line, Map<String, String> map) {
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
				map.put(k, v);
				break;
			default:
				value.append(c);
				break;
			}
			if (!l.next()) {
				break;
			}
		}

		map.put(k, value.toString());
	}

}
