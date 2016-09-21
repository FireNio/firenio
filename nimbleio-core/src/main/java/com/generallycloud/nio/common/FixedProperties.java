package com.generallycloud.nio.common;

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
