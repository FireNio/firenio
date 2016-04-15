package com.gifisan.nio.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.service.AbstractNIOFilter;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class DownloadFilter extends AbstractNIOFilter {

	private boolean			exclude		= false;
	private Map<String, String>	excludesMap	= null;

	public void accept(Request request, Response response) throws Exception {

		String serviceName = request.getServiceName();
		
		int point = serviceName.lastIndexOf('.');
		
		if (point == -1) {
			return;
		}
		
		String subfix = serviceName.substring(point);

		if (canDownload(subfix)) {

			String filePath = serviceName;
			
			Parameters param = request.getParameters();
			
			int start = param.getIntegerParameter("start");
			
			int downloadLength = param.getIntegerParameter("length");
			
			File file = new File(filePath);

			FileInputStream inputStream = null;
			try {
				if (!file.exists()) {
					RESMessage message = new RESMessage(404, "file not found:" + filePath);
					response.write(message.toString());
					response.flush();
					return;
				}

				inputStream = new FileInputStream(file);

				int available = inputStream.available();

				if (downloadLength == 0) {
					downloadLength = available - start;
				}

				response.setInputStream(inputStream);

				response.flush();

			} catch (FileNotFoundException e) {
				DebugUtil.debug(e);
			} catch (IOException e) {
				DebugUtil.debug(e);
			}
		}
	}

	private boolean canDownload(String subfix) {
		return exclude ? !excludesMap.containsKey(subfix) : true;

	}

	public void initialize(ServerContext context, Configuration config) throws Exception {
		String excludesContent = (String) config.getAttribute("excludes");
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

	public void destroy(ServerContext context, Configuration config) throws Exception {

	}
}
