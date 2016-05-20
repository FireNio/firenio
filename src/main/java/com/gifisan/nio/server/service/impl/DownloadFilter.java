package com.gifisan.nio.server.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.service.AbstractNIOFilter;

public class DownloadFilter extends AbstractNIOFilter {

	private boolean			exclude		= false;
	private Map<String, String>	excludesMap	= null;
	private static final Logger	logger		= LoggerFactory.getLogger(DownloadFilter.class);

	public void accept(IOSession session,ServerReadFuture future) throws Exception {

		String serviceName = future.getServiceName();
		
		int point = serviceName.lastIndexOf('.');
		
		if (point == -1) {
			return;
		}
		
		String subfix = serviceName.substring(point);

		if (canDownload(subfix)) {

			String filePath = serviceName;
			
			Parameters param = future.getParameters();
			
			int start = param.getIntegerParameter("start");
			
			int downloadLength = param.getIntegerParameter("length");
			
			File file = new File(filePath);

			FileInputStream inputStream = null;
			try {
				if (!file.exists()) {
					RESMessage message = new RESMessage(404, "file not found:" + filePath);
					future.write(message.toString());
					session.flush(future);
					return;
				}

				inputStream = new FileInputStream(file);

				int available = inputStream.available();

				if (downloadLength == 0) {
					downloadLength = available - start;
				}

				future.setInputIOEvent(inputStream, null);
				
				session.flush(future);

			} catch (IOException e) {
				logger.debug(e);
			}
		}
	}

	private boolean canDownload(String subfix) {
		return exclude ? !excludesMap.containsKey(subfix) : true;

	}

	public void initialize(ServerContext context, Configuration config) throws Exception {
		String excludesContent = (String) config.getParameter("excludes");
		if (StringUtil.isNullOrBlank(excludesContent)) {
			return;
		}
		this.exclude = true;
		this.excludesMap = new HashMap<String, String>();
		String[] excludes = excludesContent.split("\\|");
		for (String exclude : excludes) {
			if (StringUtil.isNullOrBlank(exclude)) {
				continue;
			}
			excludesMap.put(exclude, null);
		}

	}

	public void destroy(NIOContext context, Configuration config) throws Exception {

	}
}
