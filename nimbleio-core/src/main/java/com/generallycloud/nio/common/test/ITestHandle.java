package com.generallycloud.nio.common.test;

import java.math.BigDecimal;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class ITestHandle {
	
	private static Logger logger = LoggerFactory.getLogger(ITestHandle.class);

	public static void doTest(ITest test,long time,String testName) {
		logger.info("################## Test start ####################");
		logger.info("## Test Name:"+testName);
		long old = System.currentTimeMillis();
		for (long i = 0; i < time; i++) {
			try {
				test.test();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		long now = System.currentTimeMillis();
		long spend = (now-old);
		logger.info("## Execute Time:"+time);
		logger.info("## OP(W)/S:"+ new BigDecimal(time).divide(new BigDecimal(spend),2,BigDecimal.ROUND_HALF_UP).doubleValue() / 10);
		logger.info("## Expend Time:"+spend);
	}
}
