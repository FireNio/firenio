package com.test.servlet.http;

import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.extend.plugin.http.HttpSession;
import com.gifisan.nio.extend.service.HTTPFutureAcceptorService;

public class TestShowMemoryServlet extends HTTPFutureAcceptorService{
	
	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
		
		int M = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		StringBuilder builder = new StringBuilder();
		builder.append("总内存：");
		builder.append(runtime.totalMemory()/M);
		builder.append("M;</BR>已占用内存：");
		builder.append((runtime.totalMemory() - runtime.freeMemory()) / M);
		builder.append("M;</BR>空闲内存：");
		builder.append(runtime.freeMemory() / M);
		builder.append("M;</BR>总内存：");
		builder.append(runtime.maxMemory()/M + "M;");
		
		future.write("服务器内存使用情况：</BR>");
		future.write(builder.toString());
		
		session.flush(future);
		
	}
	
}
