package com.generallycloud.nio.component;

import java.nio.channels.CompletionHandler;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class WriteCompletionHandler implements CompletionHandler<Integer, AioSocketChannel>{

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void completed(Integer result, AioSocketChannel channel) {
		
		channel.writeCallback(result);
	}

	@Override
	public void failed(Throwable exc, AioSocketChannel channel) {
		
		logger.error(exc.getMessage() + " channel:" + channel, exc);

		CloseUtil.close(channel);
	}
	
}
