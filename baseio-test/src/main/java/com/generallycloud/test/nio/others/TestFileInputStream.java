package com.generallycloud.test.nio.others;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;

public class TestFileInputStream {

	
	
	public static void main(String[] args) throws IOException {
		
		
		File file = new File("test.txt");
		
		
		FileInputStream inputStream = new FileInputStream(file);
		
		inputStream.skip(100);
		
		CloseUtil.close(inputStream);
		
		
		
		
	}
}
