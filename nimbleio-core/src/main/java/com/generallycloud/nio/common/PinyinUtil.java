package com.generallycloud.nio.common;

import net.sourceforge.pinyin4j.PinyinHelper;

import com.alibaba.fastjson.JSONObject;

public class PinyinUtil {
	private static String		specialJSON	= "{会:'hui'}";
	
	private static JSONObject	special		;
	
	static {
	
		special = JSONObject.parseObject(specialJSON);
	
	}
	
	public static String toPinyin(char hanzi) {
		String pinyin = special.getString(String.valueOf(hanzi));
		if (StringUtil.isNullOrBlank(pinyin)) {
			String[] pys = PinyinHelper.toHanyuPinyinStringArray(hanzi);
			if (pys == null || pys.length == 0) {
				pinyin = null;
			} else {
				pinyin = pys[0].substring(0, pys[0].length() - 1);
			}
		}
		return pinyin;
	}
	
	public static String toPinyin(String hanzi) {
		if (StringUtil.isNullOrBlank(hanzi)) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		char[] cs = hanzi.toCharArray();
		for (char c : cs) {
			String p = toPinyin(c);
			if (p == null) {
				builder.append(c);
				continue;
			}
			builder.append(toPinyin(c));
		}
		return builder.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(toPinyin("c"));
		System.out.println(toPinyin("好aj;klj，我好的"));
		
		
	}
}

