package com.generallycloud.nio.common.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.nio.common.StringLexer;

public class JSON {

	private static String createUnexpectExceptionMessage(char token, int index) {
		return new StringBuilder("unexpect token ").append(token).append(",at index ").append(index)
				.toString();
	}
	
	private static String findMapKey(StringLexer lexer) throws JSONSyntaxException {
		lexer.next();
		boolean isQotesStart = skipWhitespace(lexer) == JSONToken.DOUBLE_QUOTES;
		if (isQotesStart) {
			return parseQuotesValue(lexer, lexer.current());
		} else {
			StringBuilder builder = new StringBuilder();
			if (lexer.current() == JSONToken.COLON) {
				throw new JSONSyntaxException(createUnexpectExceptionMessage(JSONToken.COLON,
						lexer.currentIndex()));
			}
			boolean end = false;
			do {
				char ch = lexer.current();
				
				
				if (ch == JSONToken.COLON) {
					lexer.previous();
					return builder.toString();
				}else if(isWhitespace(ch)){
					end = true;
				} else {
					if(end){
						throw new JSONSyntaxException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
					}
					builder.append(ch);
				}
			} while (lexer.next());
		}
		throw new JSONSyntaxException("EOF");
	}
	
	private static Object parseNullValue(StringLexer lexer) throws JSONSyntaxException {
		if (lexer.next(3)) {
			int index = lexer.currentIndex();
			if (lexer.charAt(index) == JSONToken.L && lexer.charAt(index - 1) == JSONToken.L
					&& lexer.charAt(index - 2) == JSONToken.U) {
				return null;
			} else {
				throw new JSONSyntaxException("is there should be a 'null',at index "+ lexer.currentIndex());
			}
		} else {
			throw new JSONSyntaxException("EOF");
		}
	}

	private static Boolean parseTrueValue(StringLexer lexer) throws JSONSyntaxException {
		if (lexer.next(3)) {
			int index = lexer.currentIndex();
			if (lexer.charAt(index) == JSONToken.E && lexer.charAt(index - 1) == JSONToken.U
					&& lexer.charAt(index - 2) == JSONToken.R) {
				return Boolean.TRUE;
			} else {
				throw new JSONSyntaxException("is there should be a 'true',at index "+ lexer.currentIndex());
			}
		} else {
			throw new JSONSyntaxException("EOF");
		}
	}

	private static Boolean parseFalseValue(StringLexer lexer) throws JSONSyntaxException {
		if (lexer.next(4)) {
			int index = lexer.currentIndex();
			if (lexer.charAt(index) == JSONToken.E && lexer.charAt(index - 1) == JSONToken.S
					&& lexer.charAt(index - 2) == JSONToken.L
					&& lexer.charAt(index - 3) == JSONToken.A) {
				return Boolean.FALSE;
			} else {
				throw new JSONSyntaxException("is there should be a 'false',at index "+ lexer.currentIndex());
			}
		} else {
			throw new JSONSyntaxException("EOF");
		}
	}

	private static char skipWhitespace(StringLexer lexer){
		char ch = lexer.current();
		for (;isWhitespace(ch);) {
			lexer.next();
			ch = lexer.current();
		}
		return ch;
		
	}
	
	private static Object findMapValue(StringLexer lexer) throws JSONSyntaxException {
		lexer.next();
		char ch = skipWhitespace(lexer);
		switch (ch) {
		case JSONToken.DOUBLE_QUOTES:
			return parseQuotesValue(lexer, lexer.current());
		case JSONToken.T:
			return parseTrueValue(lexer);
		case JSONToken.F:
			return parseFalseValue(lexer);
		case JSONToken.N:
			return parseNullValue(lexer);
		case JSONToken.ARRAY_START:
			return parseArray(lexer);
		case JSONToken.OBJECT_START:
			return parseMap(lexer);
		default:
			if (isNumber(ch) || ch == JSONToken.MINUS) {
				return parseNumberValue(lexer);
			} else {
				throw new JSONSyntaxException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
			}
		}
	}

	private static boolean isNumber(char ch) {
		return (ch > 47 && ch < 58);
	}

	private static final boolean isWhitespace(char ch) {
		return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\f' || ch == '\b';
	}

	private static List<?> parseArray(StringLexer lexer) throws JSONSyntaxException {
		List<Object> list = new ArrayList<Object>();
		do {
			Object v;
			char ch = skipWhitespace(lexer);
			switch (ch) {
			case JSONToken.ARRAY_START:
				v = findMapValue(lexer);
				if (v == null) {
					break;
				}
				list.add(v);
				break;
			case JSONToken.COMMA:
				v = findMapValue(lexer);
				if (v == null) {
					break;
				}
				list.add(v);
				break;
			case JSONToken.ARRAY_END:
				return list;
			default:
				throw new JSONSyntaxException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
			}
			
		} while (lexer.next());
		throw new JSONSyntaxException("EOF");
	}

	private static Map<?, ?> parseMap(StringLexer lexer) throws JSONSyntaxException {
		Map<String, Object> map = new HashMap<String, Object>();
		String key = findMapKey(lexer);
		Object value = null;
		boolean findKey = false;
		while (lexer.next()) {
			char ch = lexer.current();
			switch (ch) {
			case JSONToken.COLON:
				if (findKey) {
					throw new JSONSyntaxException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
				} else {
					value = findMapValue(lexer);
					findKey = true;
					map.put(key, value);
				}
				break;
			case JSONToken.COMMA:
				if (!findKey) {
					throw new JSONSyntaxException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
				} else {
					key = findMapKey(lexer);
					findKey = false;
				}
				break;
			case JSONToken.OBJECT_END:
				return map;
			default:
				if (isWhitespace(ch)) {
					continue;
				} else {
					throw new JSONSyntaxException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
				}
			}
		}
		return map;
	}

	private static Number parseNumberValue(StringLexer lexer) throws JSONSyntaxException {
		StringBuilder builder = new StringBuilder();
		builder.append(lexer.current());
		boolean end = false;
		boolean dot = false;
		while (lexer.next()) {
			char ch = lexer.current();
			if (isNumber(ch)) {
				if (end) {
					throw new  JSONSyntaxException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
				}
				builder.append(ch);
			} else if(ch == JSONToken.DOT){
				if (dot) {
					throw new JSONSyntaxException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
				}
				dot = true;
				builder.append(ch);
			} else if(isWhitespace(ch)){
				end = true;
			} else if (ch == JSONToken.COMMA || ch == JSONToken.OBJECT_END || ch == JSONToken.ARRAY_END) {
				lexer.previous();
				return new BigDecimal(builder.toString());
			} else {
				throw new JSONSyntaxException(createUnexpectExceptionMessage(ch, lexer.currentIndex()));
			}
		}
		throw new JSONSyntaxException("EOF");
	}

	private static String parseQuotesValue(StringLexer lexer, char startQuotes)
			throws JSONSyntaxException {
		StringBuilder builder = new StringBuilder();
		while (lexer.next()) {
			char ch = lexer.current();
			if (ch == startQuotes) {
				break;
			} else {
				builder.append(ch);
			}
		}
		return builder.toString();
	}

	public static List<?> stringToArray(String content) throws JSONSyntaxException {
		if (content == null || content.length() == 0) {
			return null;
		}
		StringLexer lexer = new StringLexer(0, content.toCharArray());
		if (lexer.current() != JSONToken.ARRAY_START) {
			throw new JSONSyntaxException("except token [ at index 0");
		}
		List list = parseArray(lexer);
		if (!lexer.complate()) {
			throw new JSONSyntaxException("EOF");
		}
		return list;
	}

	public static Map<?, ?> stringToMap(String content) throws JSONSyntaxException {
		if (content == null || content.length() == 0) {
			return null;
		}
		StringLexer lexer = new StringLexer(0, content.toCharArray());
		if (lexer.current() != JSONToken.OBJECT_START) {
			throw new JSONSyntaxException("except token { at index 0");
		}
		Map<?,?> map = parseMap(lexer);
		if (!lexer.complate()) {
			throw new JSONSyntaxException("EOF");
		}
		return map;
	}
}
