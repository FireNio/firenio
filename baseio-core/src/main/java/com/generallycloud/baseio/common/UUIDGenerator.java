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

import java.util.UUID;

public class UUIDGenerator {

	
	public static String random(){
		
		UUID uuid = UUID.randomUUID();
		
		long mostSigBits = uuid.getMostSignificantBits();
		long leastSigBits = uuid.getLeastSignificantBits();
		
		return new StringBuilder()
			.append(digits(mostSigBits >> 32, 8))
			.append(digits(mostSigBits >> 16, 4))
			.append(digits(mostSigBits, 4))
			.append(digits(leastSigBits >> 48, 4))
			.append(digits(leastSigBits, 12))
			.toString();
	}
	
	private static String digits(long val, int digits) {
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}
	
}
