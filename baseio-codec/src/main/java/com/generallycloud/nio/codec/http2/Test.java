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
package com.generallycloud.nio.codec.http2;

import com.generallycloud.nio.common.MathUtil;

public class Test {

	public static void main(String[] args) {

		String pri = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";

		byte[] array = pri.getBytes();

		String str = MathUtil.bytes2HexString(array);

		System.out.println(str);
		
		System.out.println(((byte)200) & 0xff);

	}
}
