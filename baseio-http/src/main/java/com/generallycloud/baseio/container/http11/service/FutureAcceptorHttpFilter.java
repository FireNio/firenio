/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.baseio.container.http11.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.codec.http11.future.EmptyServerHttpReadFuture;
import com.generallycloud.baseio.codec.http11.future.HttpReadFuture;
import com.generallycloud.baseio.codec.http11.future.HttpStatus;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.HtmlUtil;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.service.FutureAcceptorServiceFilter;
import com.generallycloud.baseio.protocol.ChannelWriteFuture;
import com.generallycloud.baseio.protocol.NamedReadFuture;


//FIXME if-modified-since http code 304 res 
//FIXME limit too large file
public class FutureAcceptorHttpFilter extends FutureAcceptorServiceFilter {

	private Logger					logger		= LoggerFactory.getLogger(FutureAcceptorHttpFilter.class);

	private Map<String, HttpEntity>	html_cache	= new HashMap<String, HttpEntity>();

	@Override
	protected void accept404(SocketSession session, NamedReadFuture future, String serviceName) throws IOException {

		HttpEntity entity = html_cache.get(serviceName);

		HttpStatus status = HttpStatus.C200;
		
		if (entity == null) {
			//FIXME 404 status
			status = HttpStatus.C404;
			entity = html_cache.get("/404.html");
			if (entity == null) {
				super.accept404(session, future, serviceName);
				return;
			}
		}

		File file = entity.file;

		if (file != null && file.lastModified() > entity.lastModify) {

			synchronized (entity) {
				
				reloadEntity(entity, session.getContext(),status);
			}
		}
		
		session.flush(entity.future.duplicate(future));
	}
	
	private void reloadEntity(HttpEntity entity,SocketChannelContext context,HttpStatus status) throws IOException{
		
		EmptyServerHttpReadFuture f = new EmptyServerHttpReadFuture(context);
		
		f.setStatus(status);
		
		String text = entity.text;
		
		File file = entity.file;

		if (text != null) {
			
			f.setResponseHeader("Content-Type", entity.contentType);
			f.setResponseHeader("Connection", "keep-alive");
			f.write(text);
			
			entity.lastModify = System.currentTimeMillis();
			
			ReleaseUtil.release(entity.future);
			
			entity.future = context.getProtocolEncoder().encode(UnpooledByteBufAllocator.getHeapInstance(), f);
			
			return;
		}
			
		byte [] data = FileUtil.readFileToByteArray(file);
		
		f.setResponseHeader("Content-Type", entity.contentType);
		f.setResponseHeader("Connection", "keep-alive");
		f.writeBinary(data);
		
		entity.lastModify = file.lastModified();
		
		ReleaseUtil.release(entity.future);
		
		entity.future = context.getProtocolEncoder().encode(UnpooledByteBufAllocator.getHeapInstance(), f);
	}
	
	@Override
	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		String rootPath = context.getAppLocalAddress();

		File rootFile = new File(rootPath);

		Map<String, String> mapping = new HashMap<String, String>();

		mapping.put("htm", HttpReadFuture.CONTENT_TYPE_TEXT_HTML);
		mapping.put("html", HttpReadFuture.CONTENT_TYPE_TEXT_HTML);
		mapping.put("js", HttpReadFuture.CONTENT_APPLICATION_JAVASCRIPT);
		mapping.put("css", HttpReadFuture.CONTENT_TYPE_TEXT_CSS);
		mapping.put("png", HttpReadFuture.CONTENT_TYPE_IMAGE_PNG);
		mapping.put("jpg", HttpReadFuture.CONTENT_TYPE_IMAGE_JPEG);
		mapping.put("jpeg", HttpReadFuture.CONTENT_TYPE_IMAGE_JPEG);
		mapping.put("gif", HttpReadFuture.CONTENT_TYPE_IMAGE_GIF);
		mapping.put("txt", HttpReadFuture.CONTENT_TYPE_TEXT_PLAIN);
		mapping.put("ico", HttpReadFuture.CONTENT_TYPE_IMAGE_ICON);

		if (rootFile.exists()) {
			scanFolder(context.getChannelContext(),rootFile, rootPath, mapping,"");
		}

		super.initialize(context, config);
	}

	private void scanFolder(SocketChannelContext context,File file, String root, Map<String, String> mapping,String path) throws IOException {

		if (file.isFile()) {

			String contentType = getContentType(file.getName(), mapping);

			String fileName = file.getCanonicalPath().replace("\\", "/");

			HttpEntity entity = new HttpEntity();

			entity.contentType = contentType;
			entity.file = file;
			entity.lastModify = 0;
			
			html_cache.put(path, entity);

			LoggerUtil.prettyNIOServerLog(logger, "mapping static :{}@{}", path, fileName);
			
		} else if (file.isDirectory()) {
			
			String staticName = path;
			
			if ("/lib".equals(staticName)) {
				return;
			}

			if ("".equals(staticName)) {
				staticName = "/";
			}

			File[] fs = file.listFiles();

			StringBuilder b = new StringBuilder(HtmlUtil.HTML_HEADER);

			b.append("		<div style=\"margin-left:20px;\">\n");
			b.append("			Index of " + staticName + "\n");
			b.append("		</div>\n");
			b.append("		<hr>\n");

			if (!"/".equals(staticName)) {
				int index = staticName.lastIndexOf("/");
				String parentStaticName;
				if (index == 0) {
					parentStaticName = "..";
				}else{
					parentStaticName = staticName.substring(0, index);
				}
				b.append("		<p>\n");
				b.append("			<a href=\"" + parentStaticName + "\">&lt;dir&gt;..</a>\n");
				b.append("		</p>\n");
			}

			StringBuilder db = new StringBuilder();
			StringBuilder fb = new StringBuilder();

			for (File f : fs) {

				String staticName1 = path +"/"+ f.getName();
				
				scanFolder(context,f, root, mapping, staticName1);

				if (f.isDirectory()) {
					if ("/lib".equals(staticName1)) {
						continue;
					}
					String a = "<a href=\"" + staticName1 + "\">&lt;dir&gt;" + f.getName()+ "</a>\n";
					db.append("		<p>\n");
					db.append("			" + a);
					db.append("		</p>\n");
				} else {
					String a = "<a href=\"" + staticName1 + "\">" + f.getName() + "</a>\n";
					fb.append("		<p>\n");
					fb.append("			" + a);
					fb.append("		<p>\n");
				}
			}

			b.append(db);
			b.append(fb);

			b.append("		<hr>\n");
			b.append(HtmlUtil.HTML_BOTTOM);

			HttpEntity entity = new HttpEntity();

			entity.contentType = HttpReadFuture.CONTENT_TYPE_TEXT_HTML;
			entity.file = file;
			entity.text = b.toString();

			html_cache.put(staticName, entity);
		}
			
	}

	private String getContentType(String fileName, Map<String, String> mapping) {

		int index = fileName.lastIndexOf(".");

		if (index == -1) {
			return HttpReadFuture.CONTENT_TYPE_TEXT_PLAIN;
		}

		String subfix = fileName.substring(index + 1);

		String contentType = mapping.get(subfix);

		if (contentType == null) {
			contentType = HttpReadFuture.CONTENT_TYPE_TEXT_PLAIN;
		}

		return contentType;
	}

	private class HttpEntity {

		ChannelWriteFuture	future;

		String			contentType;

		File				file;

		long				lastModify;
		
		String text;
	}
}
