package com.generallycloud.nio.component.protocol.http11.future;

import com.alibaba.fastjson.JSON;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.StringLexer;
import com.generallycloud.nio.common.StringUtil;

public class ServerHttpHeaderParser extends AbstractHttpHeaderParser{
	
	private Logger logger = LoggerFactory.getLogger(ServerHttpHeaderParser.class);

	private void skipRN(StringLexer lexer) {
		for (;;) {
			char c = lexer.current();
			if (R == c || N == c) {
				if (!lexer.next()) {
					return;
				}
				continue;
			}
			break;
		}
	}
	
	protected void parseContentType(AbstractHttpReadFuture future,String contentType) {
		
		if (!StringUtil.isNullOrBlank(contentType)) {

			if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {

				future.contentType = CONTENT_APPLICATION_URLENCODED;

			}else if(CONTENT_TYPE_TEXT_PLAIN.equals(contentType)){
				
				future.contentType = CONTENT_TYPE_TEXT_PLAIN;
				
			} else if (contentType.startsWith("multipart/form-data;")) {

				int index = KMP_BOUNDARY.match(contentType);

				if (index != -1) {
					future.boundary = contentType.substring(index + 9);
				}

				future.contentType = CONTENT_TYPE_MULTIPART;
			} else {
				//FIXME other content-type
			}
		} else {
			future.contentType = CONTENT_APPLICATION_URLENCODED;
		}
	}

	protected void parseFirstLine(StringLexer lexer, AbstractHttpReadFuture future) {
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
