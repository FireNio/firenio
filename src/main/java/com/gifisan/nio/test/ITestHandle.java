package com.gifisan.nio.test;

public class ITestHandle {

	public static void doTest(ITest test,int time,String testName) {
		System.out.println("################## Test start ####################");
		System.out.println("## Test Name:"+testName);
		long old = System.currentTimeMillis();
		for (int i = 0; i < time; i++) {
			try {
				test.test();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		System.out.println("## Execute Time:"+time);
		System.out.println("## Expend Time:"+(System.currentTimeMillis()-old));
	}
}
