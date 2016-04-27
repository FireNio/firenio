package com.gifisan.nio.server.service.impl;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.service.NIOServlet;
import com.gifisan.nio.server.session.IOSession;

public class ShowMemoryServlet extends NIOServlet{

	private String				username		= null;
	private String				password		= null;
	
	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		Parameters param = future.getParameters();
		String username = param.getParameter("username");
		String password = param.getParameter("password");
		
		boolean result = this.username.equals(username) && this.password.equals(password);
		if (result) {
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
			future.write(RESMessage.R_UNAUTH.toString());
		}
		session.flush(future);
		
	}
	
	public void initialize(NIOContext context, Configuration config) throws Exception {
		this.username = config.getProperty("username");
		this.password = config.getProperty("password");
	}


}
