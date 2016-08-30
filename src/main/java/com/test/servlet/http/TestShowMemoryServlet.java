package com.test.servlet.http;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.extend.plugin.http.HttpSession;
import com.gifisan.nio.extend.service.HTTPFutureAcceptorService;

public class TestShowMemoryServlet extends HTTPFutureAcceptorService{
	
	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
		
		NIOContext context = session.getIOSession().getContext();
		
		BigDecimal time = new BigDecimal(System.currentTimeMillis() - context.getStartupTime());
		BigDecimal anHour = new BigDecimal(60 * 60 * 1000);
		BigDecimal hour = time.divide(anHour, 3, RoundingMode.HALF_UP);
		
		int M = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		StringBuilder builder = new StringBuilder();
		builder.append("虚拟机占用内存：");
		builder.append(runtime.totalMemory()/M);
		builder.append("M;</BR>已占用内存：");
		builder.append((runtime.totalMemory() - runtime.freeMemory()) / M);
		builder.append("M;</BR>空闲内存：");
		builder.append(runtime.freeMemory() / M);
//		builder.append("M;</BR>虚拟机最大可占用内存：");
//		builder.append(runtime.maxMemory()/M );
		builder.append("M;</BR>服务运行时间：");
		builder.append(hour + "H;");
		
		future.write("服务器内存使用情况：</BR>");
		future.write(builder.toString());
		
		session.flush(future);
		
	}
	
}
