package com.gifisan.nio.component.protocol.http11.future;

import com.alibaba.fastjson.JSON;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringLexer;

public class ServerHttpHeaderParser extends AbstractHttpHeaderParser{
	
	private Logger logger = LoggerFactory.getLogger(ServerHttpHeaderParser.class);

	private void skipRN(StringLexer lexer) {
		for (;;) {
			char c = lexer.current();
			if (R == c || N == c) {
				lexer.next();
				continue;
			}
			break;
		}
	}
	
	protected void parseFirstLine(StringLexer lexer, DefaultHTTPReadFuture future) {
		skipRN(lexer);
		int index = 0;
		String[] array = new String[5];
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

		if (index == 3) {
			future.method = array[0];
			future.requestURI = array[1];
			future.version = array[2];
		} else if (index == 4) {
			future.method = array[0];
			future.requestURI = array[2];
			future.version = array[3];
		} else if (index == 5) {
			future.method = array[0];
			future.requestURI = array[3];
			future.version = array[4];
		} else {
			logger.error(JSON.toJSONString(array));
			logger.error(lexer.toString());
			logger.error(future.header_buffer.toString());
			throw new IllegalArgumentException("http header first line breaked");
		}
	}
}
