package com.generallycloud.test.nio.others;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TestFileInputStream {

	
	
	public static void main(String[] args) throws IOException {
		
		
		File file = new File("test.txt");
		
		
		FileInputStream inputStream = new FileInputStream(file);
		
		inputStream.skip(100);
		
		
		
		
		
		
		
		
		
		
		
	}
}
