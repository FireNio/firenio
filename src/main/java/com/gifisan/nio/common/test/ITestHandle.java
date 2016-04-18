package com.gifisan.nio.common.test;

import java.math.BigDecimal;

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
		long spend = (System.currentTimeMillis()-old);
		System.out.println("## Execute Time:"+time);
		System.out.println("## OP/S:"+ new BigDecimal(time*1000).divide(new BigDecimal(spend),2,BigDecimal.ROUND_HALF_UP));
		System.out.println("## Expend Time:"+spend);
	}
}
