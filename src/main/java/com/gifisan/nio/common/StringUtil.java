package com.gifisan.nio.common;

public class StringUtil {

	public static boolean isNullOrBlank(String value){
		
		return value == null || value.length() == 0;
	}

	public static boolean hasLength(String text) {
		
		return text != null && text.length() > 0;
	}

	public static boolean hasText(String text) {
		return text != null && text.trim().length() > 0;
	}
}
