package com.generallycloud.nio.container.implementation;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public class SYSTEMShowMemoryServlet extends FutureAcceptorService{
	
	public static final String SERVICE_NAME = SYSTEMShowMemoryServlet.class.getSimpleName();

	public void accept(SocketSession session, ReadFuture future) throws Exception {
		
		int M = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		StringBuilder builder = new StringBuilder();
		builder.append("服务器内存使用情况：\n");
		builder.append("虚拟机占用内存：");
		builder.append(runtime.totalMemory()/M);
		builder.append("M;\n已占用内存：");
		builder.append((runtime.totalMemory() - runtime.freeMemory()) / M);
		builder.append("M;\n空闲内存：");
		builder.append(runtime.freeMemory() / M);
		builder.append("M;\n虚拟机最大可占用内存：");
		builder.append(runtime.maxMemory()/M + "M;");
		
		future.write(builder.toString());
		
		session.flush(future);
		
	}
	
}
