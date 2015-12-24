package com.gifisan.mtp.servlet.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.context.ServletContext;
import com.gifisan.mtp.servlet.MTPFilter;

public class DownloadFilter implements MTPFilter{
	
	private boolean except 				= false;
	
	private Map<String,String> exceptMap 	= null;
	
	private int BLOCK 						= 102400;

	public boolean doFilter(Request request,Response response) throws Exception {
		
		String serviceName = request.getServiceName();
		int point = serviceName.lastIndexOf('.');
		if (point == -1) {
			return false;
		}
		String subfix = serviceName.substring(point);
		
		if (canDownload(subfix)) {
			String filePath 		= serviceName;
			int start 				= request.getIntegerParameter("start");
			int downloadLength 		= request.getIntegerParameter("length");
			File file 				= new File(filePath);

			if (!file.exists()) {
				response.write("file not found!");
				response.flush();
				return true;
			}
			
			FileInputStream inputStream = new FileInputStream(file);
			
			int available = inputStream.available();
			
			if (downloadLength == 0) {
				downloadLength = available - start;
			}
			
			response.setStreamResponse(downloadLength);
			
			inputStream.skip(start);
			
			int BLOCK = this.BLOCK;
			
			if (BLOCK > downloadLength) {
				byte [] bytes = new byte[downloadLength];
				inputStream.read(bytes);
				response.write(bytes);
			}else{
				byte [] bytes	= new byte[BLOCK];
				int times 		= downloadLength / BLOCK;
				int remain		= downloadLength % BLOCK;
				while(times > 0){
					inputStream.read(bytes);
					response.write(bytes);
					times--;
				}
				if (remain > 0) {
					inputStream.read(bytes,0,remain);
					response.write(bytes, 0, remain);
				}
			}
			
			CloseUtil.close(inputStream);
			
			return true;
		}
		
		return false;
	}
	
	private boolean canDownload(String subfix){
		return except ? !exceptMap.containsKey(subfix) :true;
		
	}

	public void initialize(ServletContext context, FilterConfig config)
			throws Exception {
		String exceptContent = (String)config.getAttribute("except");
		if (StringUtil.isNullOrBlank(exceptContent)) {
			return;
		}
		this.except = true;
		this.exceptMap = new HashMap<String, String>();
		String []excepts = exceptContent.split("|");
		for(String except:excepts){
			exceptMap.put(except, null);
		}
	}

	public void destroy(ServletContext context, FilterConfig config)
			throws Exception {
		
	}

	
	
}
