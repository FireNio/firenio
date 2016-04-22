package com.gifisan.nio.service.impl;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.NIOServlet;

public class ShowMemoryServlet extends NIOServlet{

	private String				username		= null;
	private String				password		= null;
	
	public void accept(Session session) throws Exception {
		Parameters param = session.getParameters();
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
			
			session.write("服务器内存使用情况：\n");
			session.write(builder.toString());
		}else{
			session.write(RESMessage.R_UNAUTH.toString());
		}
		session.flush();
		
	}
	
	public void initialize(NIOContext context, Configuration config) throws Exception {
		this.username = config.getProperty("username");
		this.password = config.getProperty("password");
	}


}
