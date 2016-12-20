/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.test.nio.others;

import java.util.List;

import com.generallycloud.nio.common.FileUtil;

public class Test2 {

	public static void main(String[] args) throws Exception {
		
		List<String> lines = FileUtil.readLines(FileUtil.readFileByCls("test.txt"));
		
		for(String l : lines){
			
//			System.out.println(l);
			
			String []array = l.split("\t");
			
			String index = array[0];
			String name = array[1];
			String value = "";
			
			if (array.length > 2) {
				value = array[2];
			}
			
			
			System.out.println("STATIC_HEADER_TABLE.addHeader(new Header("+index+", \""+name+"\", \""+value+"\"));");
		}
		
	}
	
	
}
