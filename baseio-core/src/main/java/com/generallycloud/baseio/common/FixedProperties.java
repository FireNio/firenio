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

import java.util.Properties;

public class FixedProperties extends Properties{
	
	private static final long	serialVersionUID	= 1L;

	public void loadString(String content){
		if (StringUtil.isNullOrBlank(content)) {
			return;
		}
		
		String [] lines = content.split("\n");
		
		for(String line :lines){
			
			insertOneRow(line);
		}
	}
	
	private void insertOneRow(String line){
		
		if (StringUtil.isNullOrBlank(line)) {
			return;
		}
		
		int index = line.indexOf("=");
		
		if (index == -1) {
			return;
		}
		
		String key = line.substring(0,index);
		String value = line.substring(index+1,line.length());
		
		key = trim(key);
		value = trim(value);
		
		put(key, value);
	}
	
	private String trim(String value){
		return value.trim().replace("\r", "").replace("\t", "");
	}
	
	public static void main(String[] args) {
		
		FixedProperties p = new FixedProperties();
		
		p.insertOneRow("aaa=bbb");
		
		System.out.println(p.get("aaa"));
		
	}
	
}
