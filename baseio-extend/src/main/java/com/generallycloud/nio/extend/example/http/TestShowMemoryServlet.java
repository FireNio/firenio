package com.generallycloud.nio.extend.example.http;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.codec.http11.HttpContext;
import com.generallycloud.nio.codec.http11.HttpSession;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.HtmlUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.extend.service.HTTPFutureAcceptorService;

public class TestShowMemoryServlet extends HTTPFutureAcceptorService {

	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {

		BaseContext context = session.getIOSession().getContext();
		HttpContext httpContext = session.getContext();

		BigDecimal time = new BigDecimal(System.currentTimeMillis() - context.getStartupTime());
		BigDecimal anHour = new BigDecimal(60 * 60 * 1000);
		BigDecimal hour = time.divide(anHour, 3, RoundingMode.HALF_UP);

		ByteBufAllocator allocator = context.getByteBufAllocator();
		
		String allocatorDes = allocator.toString();
		
		allocatorDes = allocatorDes.substring(allocatorDes.indexOf("["));
		
		ServerConfiguration configuration = context.getServerConfiguration();

		int SERVER_MEMORY_POOL_CAPACITY = configuration.getSERVER_MEMORY_POOL_CAPACITY();
		int SERVER_MEMORY_POOL_UNIT = configuration.getSERVER_MEMORY_POOL_UNIT();

		double MEMORY_POOL_SIZE = new BigDecimal(SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_UNIT).divide(
				new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).doubleValue();

		int M = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		StringBuilder builder = new StringBuilder(HtmlUtil.HTML_HEADER);

		builder.append("		<div style=\"margin-left:20px;\">\n");
		builder.append("服务器内存使用情况：</BR>\n");
		builder.append("虚拟机占用内存：");
		builder.append(runtime.totalMemory() / M);
		builder.append("M;\n</BR>已占用内存：");
		builder.append((runtime.totalMemory() - runtime.freeMemory()) / M);
		builder.append("M;\n</BR>空闲内存：");
		builder.append(runtime.freeMemory() / M);
		builder.append("M;\n</BR>内存池大小：");
		builder.append(MEMORY_POOL_SIZE);
		builder.append("M;\n</BR>内存池状态（Heap）：");
		builder.append(allocatorDes);
		builder.append(";\n</BR>服务器当前连接数（io-session）：");
		builder.append(context.getSessionManager().getManagedSessionSize());
		builder.append(";\n</BR>服务器当前会话数（http-session）：");
		builder.append(httpContext.getHttpSessionManager().getManagedSessionSize());
		builder.append(";\n</BR>服务运行时间：");
		builder.append(hour + "H;");
		builder.append("		</div>\n");
		builder.append("		<hr>\n");

		builder.append("<p style=\"color: #FDA58C\">");
		builder.append("	Powered by baseio@");
		builder.append("	<a style=\"color:#F94F4F;\" href=\"https://github.com/generallycloud/baseio#readme\">");
		builder.append("		https://github.com/generallycloud/baseio");
		builder.append("	</a>");
		builder.append("</p>");
		
		builder.append(HtmlUtil.HTML_BOTTOM);

		future.write(builder.toString());

		future.setResponseHeader("Content-Type", "text/html");

		session.flush(future);
	}

}
