package com.gifisan.nio.server.service.impl;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.service.NIOServlet;

public class SYSTEMShowMemoryServlet extends NIOServlet{
	
	public static final String SERVICE_NAME = SYSTEMShowMemoryServlet.class.getSimpleName();

	public void accept(IOSession session,ReadFuture future) throws Exception {
		if (session.getLoginCenter().isValidate(session, future)) {
			int M = 1024 * 1024;
			Runtime runtime = Runtime.getRuntime();
			StringBuilder builder = new StringBuilder();
			builder.append("总内存：");
			builder.append(runtime.totalMemory()/M);
			builder.append("M;\n已占用内存：");
			builder.append((runtime.totalMemory() - runtime.freeMemory()) / M);
			builder.append("M;\n空闲内存：");
			builder.append(runtime.freeMemory() / M);
			builder.append("M;\n总内存：");
			builder.append(runtime.maxMemory()/M + "M;");
			
			future.write("服务器内存使用情况：\n");
			future.write(builder.toString());
		}else{
			future.write(RESMessage.UNAUTH.toString());
		}
		session.flush(future);
		
	}
	
}
