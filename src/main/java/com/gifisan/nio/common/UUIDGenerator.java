package com.gifisan.nio.common;

import java.util.UUID;

import com.gifisan.nio.common.test.ITest;
import com.gifisan.nio.common.test.ITestHandle;

public class UUIDGenerator {

	
	public static String random(){
		return UUID.randomUUID().toString();
		
	}
	
	public static void main(String[] args) {
		
		ITestHandle.doTest(new ITest() {
			
			public void test() {
				random();
			}
		}, 1000000,"uuid");
		
		
		String id = UUID.randomUUID().toString();
		System.out.println(id);
		System.out.println(random());
		
	}
	
	
}
