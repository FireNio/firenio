package com.test.service.http;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.protocol.http11.HttpContext;
import com.generallycloud.nio.component.protocol.http11.HttpSession;
import com.generallycloud.nio.component.protocol.http11.future.HttpReadFuture;
import com.generallycloud.nio.extend.service.HTTPFutureAcceptorService;

public class TestShowMemoryServlet extends HTTPFutureAcceptorService{
	
	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
		
		NIOContext context = session.getIOSession().getContext();
		HttpContext httpContext = session.getContext();
		
		BigDecimal time = new BigDecimal(System.currentTimeMillis() - context.getStartupTime());
		BigDecimal anHour = new BigDecimal(60 * 60 * 1000);
		BigDecimal hour = time.divide(anHour, 3, RoundingMode.HALF_UP);
		
		int M = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		StringBuilder builder = new StringBuilder();
		
		builder.append("<!DOCTYPE html>\n");
		builder.append("<html lang=\"en\">\n");
		builder.append("	<head>\n");
		builder.append("		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
		builder.append("		<meta name=\"viewport\" content=\"initial-scale=1, maximum-scale=3, minimum-scale=1, user-scalable=no\">\n");
		builder.append("		<title>nimbleio</title>\n");
		builder.append("		<style type=\"text/css\"> \n");
		builder.append("			p {margin:15px;}\n");
		builder.append("			a:link { color:#F94F4F;  }");
		builder.append("			a:visited { color:#F94F4F; }");
		builder.append("			a:hover { color:#000000; }");
		builder.append("		</style>\n");
		builder.append("	</head>\n");
		builder.append("	<body style=\"font-family:Georgia;\">\n");
		builder.append("		<div style=\"margin-left:20px;\">\n");
		builder.append("服务器内存使用情况：</BR>\n");
		builder.append("虚拟机占用内存：");
		builder.append(runtime.totalMemory()/M);
		builder.append("M;\n</BR>已占用内存：");
		builder.append((runtime.totalMemory() - runtime.freeMemory()) / M);
		builder.append("M;\n</BR>空闲内存：");
		builder.append(runtime.freeMemory() / M);
		builder.append("M;\n</BR>服务器当前连接数（io-session）：");
		builder.append(context.getSessionFactory().getManagedSessionSize());
		builder.append(";\n</BR>服务器当前会话数（http-session）：");
		builder.append(httpContext.getHttpSessionFactory().getManagedSessionSize());
		builder.append(";\n</BR>服务运行时间：");
		builder.append(hour + "H;");
		builder.append("		</div>\n");
		builder.append("		<hr>\n");
		builder.append("	</body>\n");
		builder.append("</html>");
		
		future.write(builder.toString());
		
		session.flush(future);
		
	}
	
}
