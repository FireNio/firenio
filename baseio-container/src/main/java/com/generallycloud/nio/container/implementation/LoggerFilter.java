package com.generallycloud.nio.container.implementation;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptorFilter;
import com.generallycloud.nio.protocol.NamedReadFuture;
import com.generallycloud.nio.protocol.ParametersReadFuture;

public class LoggerFilter extends FutureAcceptorFilter {

	private Logger logger = LoggerFactory.getLogger(LoggerFilter.class);

	@Override
	protected void accept(SocketSession session, NamedReadFuture future) throws Exception {

		String futureName = future.getFutureName();

		if(futureName.endsWith(".html") 
			||futureName.endsWith(".css")
			||futureName.endsWith(".js")
			||futureName.endsWith(".jpg")
			||futureName.endsWith(".png")
			||futureName.endsWith(".ico")){
			
			return;
		}
		
		String remoteAddr = session.getRemoteAddr();

		String readText = future.getReadText();
		
		if (!StringUtil.isNullOrBlank(readText)) {

			logger.info("请求IP：{}，服务名称：{}，请求内容：{}", new String[] { remoteAddr, futureName, readText });
			return;
		}
		
		if (future instanceof ParametersReadFuture) {
			
			Parameters parameters = ((ParametersReadFuture) future).getParameters();
			
			if (parameters.size() > 0) {
				
				logger.info("请求IP：{}，服务名称：{}，请求内容：{}", new String[] { remoteAddr, futureName, parameters.toString() });
				
				return;
			}
		}

		logger.info("请求IP：{}，服务名称：{}",remoteAddr, futureName);
	}

}
