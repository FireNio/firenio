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
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.service.AbstractNIOFilter;

public class DownloadFilter extends AbstractNIOFilter {

	private boolean			exclude		= false;
	private Map<String, String>	excludesMap	= null;

	public void accept(IOSession session,ReadFuture future) throws Exception {

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
					session.write(message.toString());
					session.flush();
					return;
				}

				inputStream = new FileInputStream(file);

				int available = inputStream.available();

				if (downloadLength == 0) {
					downloadLength = available - start;
				}

				session.write(inputStream,null);
				
				session.flush();

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

	public void initialize(NIOContext context, Configuration config) throws Exception {
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

	public void destroy(NIOContext context, Configuration config) throws Exception {

	}
}
