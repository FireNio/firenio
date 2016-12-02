package com.generallycloud.nio.container.implementation;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptorFilter;
import com.generallycloud.nio.protocol.NamedReadFuture;

public class LoggerFilter extends FutureAcceptorFilter {

	private Logger logger = LoggerFactory.getLogger(LoggerFilter.class);

	protected void accept(SocketSession session, NamedReadFuture future) throws Exception {

		String futureName = future.getFutureName();

		if ("/favicon.ico".equals(futureName)) {
			return;
		}

		String remoteAddr = session.getRemoteAddr();

		String readText = future.getReadText();

		if (!StringUtil.isNullOrBlank(readText)) {

			logger.info("请求IP：{}，服务名称：{}，请求内容：{}", new String[] { remoteAddr, futureName, readText });
			return;
		}

		logger.info("请求IP：{}，服务名称：{}",remoteAddr, futureName);
	}

}
