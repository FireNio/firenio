package com.gifisan.mtp.test;

public class ITestHandle {

	public static void doTest(ITest test,int time){
		System.out.println("################## Test start ####################");
		System.out.println("## Test Name:"+test.getTestName());
		long old = System.currentTimeMillis();
		for (int i = 0; i < time; i++) {
			test.test();
		}
		System.out.println("## Execute Time:"+time);
		System.out.println("## Expend Time:"+(System.currentTimeMillis()-old));
	}
}
