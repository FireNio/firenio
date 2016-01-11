package com.gifisan.mtp.common;

import java.util.UUID;

import com.gifisan.mtp.test.ITest;
import com.gifisan.mtp.test.ITestHandle;

public class UUIDGenerator {

	
	public static String random(){
		return UUID.randomUUID().toString();
		
	}
	
	public static void main(String[] args) {
		
		ITestHandle.doTest(new ITest() {
			
			public void test() {
				UUID.randomUUID().toString();
			}
			
			public String getTestName() {
				return "uuid";
			}
		}, 1000);
		
		
		String id = UUID.randomUUID().toString();
		System.out.println(id);
		System.out.println(random());
		
	}
	
	
}
