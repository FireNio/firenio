package com.generallycloud.nio.common;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtil {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:MM:ss:SS");
	
	public static String now(){
		return sdf.format(new Date());
		
	}
	
	public static void main(String[] args) {
		
		System.out.println(DateUtil.now());
		
		
	}
}
