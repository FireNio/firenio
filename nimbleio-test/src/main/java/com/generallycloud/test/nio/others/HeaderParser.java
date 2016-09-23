package com.generallycloud.test.nio.others;


public class HeaderParser {

//	private int				index		= 0;
//
//	private boolean			first_line_parsed;
//
//	private boolean			parse_key		= true;
//
//	private StringBuilder		value;
//
//	private StringBuilder		first_line	= new StringBuilder();
//
//	private BufferedOutputStream	buffer		= new BufferedOutputStream(1024 * 4);
//
//	private int				length;
//
//	public void write(byte[] array, int offset, int length) {
//
//		length += length;
//
//		buffer.write(array, offset, length);
//	}
//
//	public boolean decode() {
//
//		byte[] array = buffer.array();
//
//		if (!first_line_parsed) {
//
//			StringBuilder first_line = this.first_line;
//
//			for (; index < length;) {
//
//				char c = (char) array[index];
//
//				if ('\r' == c) {
//
//				} else if ('\n' == c) {
//					first_line_parsed = true;
//				} else {
//					first_line.append(c);
//					index++;
//					break;
//				}
//				index++;
//			}
//		}
//
//		StringBuilder value = this.value;
//		String k = null;
//		for (; index < length;) {
//			
//			char c = (char) array[index];
//			
//			if (':' == c) {
//				k = value.toString();
//				value = new StringBuilder();
//				
//				if (' ' != lexer.current()) {
//					lexer.previous();
//				}
//				headers.put(k, findHeaderValue(lexer));
//				break;
//			}else{
//				value.append(c);
//				break;
//				
//			}
//			if (!lexer.next()) {
//				break;
//			}
//		}
//
//		return true;
//	}
//
//	private String findHeaderValue(StringLexer lexer) {
//		StringBuilder value = new StringBuilder();
//		for (; lexer.next();) {
//			char c = lexer.current();
//			switch (c) {
//			case '\r':
//				break;
//			case '\n':
//				return value.toString();
//			default:
//				value.append(c);
//				break;
//			}
//		}
//		return value.toString();
//	}

	public static void main(String[] args) {

	}
}
