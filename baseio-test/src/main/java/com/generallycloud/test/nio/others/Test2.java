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
