/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.common;

public class PinyinUtil {
    //	private static String		specialJSON	= "{会:'hui'}";
    //	
    //	private static JSONObject	special		;
    //	
    //	static {
    //	
    //		special = JSON.parseObject(specialJSON);
    //	
    //	}
    //	
    //	public static String toPinyin(char hanzi) {
    //		String pinyin = special.getString(String.valueOf(hanzi));
    //		if (StringUtil.isNullOrBlank(pinyin)) {
    //			String[] pys = PinyinHelper.toHanyuPinyinStringArray(hanzi);
    //			if (pys == null || pys.length == 0) {
    //				pinyin = null;
    //			} else {
    //				pinyin = pys[0].substring(0, pys[0].length() - 1);
    //			}
    //		}
    //		return pinyin;
    //	}
    //	
    //	public static String toPinyin(String hanzi) {
    //		if (StringUtil.isNullOrBlank(hanzi)) {
    //			return null;
    //		}
    //		StringBuilder builder = new StringBuilder();
    //		char[] cs = hanzi.toCharArray();
    //		for (char c : cs) {
    //			String p = toPinyin(c);
    //			if (p == null) {
    //				builder.append(c);
    //				continue;
    //			}
    //			builder.append(toPinyin(c));
    //		}
    //		return builder.toString();
    //	}
    //	
    //	public static void main(String[] args) {
    //		System.out.println(toPinyin("c"));
    //		System.out.println(toPinyin("好aj;klj，我好的"));
    //		
    //		
    //	}
}
