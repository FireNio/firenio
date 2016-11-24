package com.generallycloud.test.nio.others;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.generallycloud.nio.common.CloseUtil;

public class TestFileOutputStream {

	
	
	public static void main(String[] args) throws IOException {
		
		
		File file = new File("test.txt");
		
		
		FileOutputStream outputStream = new FileOutputStream(file);
		
		FileChannel channel = outputStream.getChannel();
		
		CloseUtil.close(channel);
		
		CloseUtil.close(outputStream);
		
	}
}
