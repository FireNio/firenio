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

import java.nio.charset.Charset;
import java.security.MessageDigest;

public class MD5Token {

	private static char		hexDigits[]	= { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f' };
	private static MD5Token	instance		= new MD5Token();

	private MD5Token() {
	}

	public static MD5Token getInstance() {
		return instance;
	}

	public String getShortToken(String value, Charset encoding) {
		return getLongToken(value.getBytes()).substring(8, 24);
	}

	public String getLongToken(String value, Charset encoding) {
		return getLongToken(value.getBytes()).toString();
	}

	public String getLongToken(byte[] array) {
		return getLongToken(array, 0, array.length);
	}

	public String getLongToken(byte[] array, int off, int len) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(array, off, len);
			return toHex(md5.digest());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private String toHex(byte[] bytes) {
		StringBuilder s = new StringBuilder(32);
		int length = bytes.length;
		for (int i = 0; i < length; i++) {
			s.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			s.append(hexDigits[bytes[i] & 0x0f]);
		}
		return s.toString();
	}
}
