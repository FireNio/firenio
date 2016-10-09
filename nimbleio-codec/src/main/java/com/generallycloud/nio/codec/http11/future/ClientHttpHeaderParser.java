package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.common.StringLexer;
import com.generallycloud.nio.common.StringUtil;

public class ClientHttpHeaderParser extends AbstractHttpHeaderParser{
	
	protected void parseContentType(AbstractHttpReadFuture future, String contentType) {
		
		if (!StringUtil.isNullOrBlank(contentType)) {

			if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {

				future.contentType = CONTENT_APPLICATION_URLENCODED;

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

		future.version = array[0];
		future.status = HttpStatus.getHttpStatus(Integer.parseInt(array[1]));
	}
}
