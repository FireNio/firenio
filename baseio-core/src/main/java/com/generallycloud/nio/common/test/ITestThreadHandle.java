package com.generallycloud.nio.common.test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class ITestThreadHandle{
	
	private static Logger logger = LoggerFactory.getLogger(ITestThreadHandle.class);
	
	public static void doTest(Class<? extends ITestThread> clazz,int threads,int time){
		
		logger.info("################## Test start ####################");
		
		int allTime = time * threads;
		
		CountDownLatch latch = new CountDownLatch(allTime);
		
		ITestThread  []ts = new ITestThread[threads]; 
		
		for (int i = 0; i < threads; i++) {
			
			try {
				ITestThread t = clazz.newInstance();
				
				t.setLatch(latch);
				
				t.setTime(time);
				
				t.prepare();
				
				ts[i] = t;
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
		}
		
		logger.info("## prepare complete");
		
		long old = System.currentTimeMillis();
		
		for (int i = 0; i < ts.length; i++) {
			new Thread(ts[i]).start();
		}
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		long spend = (System.currentTimeMillis() - old);
		
		
		logger.info("## Execute Time:" + allTime);
		logger.info("## OP/S:"
				+ new BigDecimal(allTime * 1000L).divide(new BigDecimal(spend), 2, BigDecimal.ROUND_HALF_UP));
		logger.info("## Expend Time:" + spend);
		
		for (int i = 0; i < ts.length; i++) {
			ts[i].stop();
		}
	}
}
