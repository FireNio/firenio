package com.gifisan.nio.component.protocol.http11.future;

import com.gifisan.nio.common.StringLexer;

public class ClientHttpHeaderParser extends AbstractHttpHeaderParser{
	
	protected void parseFirstLine(StringLexer lexer, DefaultHTTPReadFuture future) {
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
		future.status = Integer.parseInt(array[1]);
//		future.xxx = array[2];
	}
}
