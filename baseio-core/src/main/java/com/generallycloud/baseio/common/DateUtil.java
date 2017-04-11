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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtil {

	public static final DateFormat	HH_mm_ss			= new SimpleDateFormat("HH:mm:ss");
	public static final DateFormat	yyyy_MM_dd		= new SimpleDateFormat("yyyy-MM-dd");
	public static final DateFormat	yyyy_MM_dd_HH_mm_ss	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final DateFormat	yyyyMMdd_HH_mm_ss	= new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	public static final DateFormat	yyyyMMdd			= new SimpleDateFormat("yyyyMMdd");
	public static final DateFormat	yyyyMMddHHmmss		= new SimpleDateFormat("yyyyMMddHHmmss");
	
	public static Date parseHH_mm_ss(String source){
		try {
			return HH_mm_ss.parse(source);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Date parseyyyy_MM_dd(String source){
		try {
			return yyyy_MM_dd.parse(source);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Date parseyyyy_MM_dd_HH_mm_ss(String source){
		try {
			return yyyy_MM_dd_HH_mm_ss.parse(source);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Date parseyyyyMMdd_HH_mm_ss(String source){
		try {
			return yyyyMMdd_HH_mm_ss.parse(source);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Date parseyyyyMMdd(String source){
		try {
			return yyyyMMdd.parse(source);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Date parseyyyyMMddHHmmss(String source){
		try {
			return yyyyMMddHHmmss.parse(source);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
}
