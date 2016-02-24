package com.gifisan.nio.servlet.impl;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.component.RequestParam;
import com.gifisan.nio.server.NIOServlet;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServerContext;

public class ShowMemoryServlet extends NIOServlet{

	private String				username		= null;
	private String				password		= null;
	
	public void accept(Request request, Response response) throws Exception {
		RequestParam param = request.getParameters();
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
			
			response.write("服务器内存使用情况：\n");
			response.write(builder.toString());
		}else{
			response.write(RESMessage.R_UNAUTH.toString());
		}
		response.flush();
		
	}
	
	public void initialize(ServerContext context, Configuration config) throws Exception {
		this.username = config.getProperty("username");
		this.password = config.getProperty("password");
	}


}
