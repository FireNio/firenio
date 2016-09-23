package com.generallycloud.nio.extend.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.common.FileUtil;
import com.generallycloud.nio.common.HtmlUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.NamedReadFuture;
import com.generallycloud.nio.component.protocol.http11.future.HttpHeaderParser;
import com.generallycloud.nio.component.protocol.http11.future.HttpReadFuture;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.DynamicClassLoader;
import com.generallycloud.nio.extend.configuration.Configuration;

public class FutureAcceptorHttpFilter extends FutureAcceptorServiceFilter {

	public FutureAcceptorHttpFilter(DynamicClassLoader classLoader) {
		super(classLoader);
	}

	private Logger					logger		= LoggerFactory.getLogger(FutureAcceptorHttpFilter.class);

	private Map<String, HttpEntity>	html_cache	= new HashMap<String, HttpEntity>();

	protected void accept404(Session session, NamedReadFuture future, String serviceName) throws IOException {

		String _service_name = serviceName;
		
//		if ("/".equals(_service_name)) {
//			_service_name = "/index.html";
//		}
		
		HttpEntity entity = html_cache.get(_service_name);

		if (entity == null) {
			entity = html_cache.get("/404.html");
			if (entity == null) {
				super.accept404(session, future, serviceName);
				return;
			}
		}
		
		File file = entity.file;
		
		if(file != null && file.lastModified() > entity.lastModify){
			
			synchronized (entity) {
				entity.array = FileUtil.readFileToByteArray(file);
				entity.lastModify = file.lastModified();
			}
		}

		HttpReadFuture f = (HttpReadFuture) future;

		f.setHeader("Content-Type", entity.contentType);

		f.write(entity.array);

		session.flush(f);
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		
		String rootPath = context.getAppLocalAddress();

		File rootFile = new File(rootPath);
		
		Map<String, String> mapping = new HashMap<String, String>();

		mapping.put("htm", HttpHeaderParser.CONTENT_TYPE_TEXT_HTML);
		mapping.put("html", HttpHeaderParser.CONTENT_TYPE_TEXT_HTML);
		mapping.put("js", HttpHeaderParser.CONTENT_APPLICATION_JAVASCRIPT);
		mapping.put("css", HttpHeaderParser.CONTENT_TYPE_TEXT_CSS);
		mapping.put("png", HttpHeaderParser.CONTENT_TYPE_IMAGE_PNG);
		mapping.put("jpg", HttpHeaderParser.CONTENT_TYPE_IMAGE_JPEG);
		mapping.put("jpeg", HttpHeaderParser.CONTENT_TYPE_IMAGE_JPEG);
		mapping.put("gif", HttpHeaderParser.CONTENT_TYPE_IMAGE_GIF);
		mapping.put("txt", HttpHeaderParser.CONTENT_TYPE_TEXT_PLAIN);
		mapping.put("ico", HttpHeaderParser.CONTENT_TYPE_IMAGE_ICON);

		scanFolder(rootFile, rootPath,mapping);

		super.initialize(context, config);
	}

	private void scanFolder(File file, String root,Map<String, String> mapping) throws IOException {

		if (file.exists()) {
			if (file.isFile()) {

				String contentType = getContentType(file.getName(), mapping);

				byte[] bytes = FileUtil.readFileToByteArray(file);

				String fileName = file.getCanonicalPath();

				fileName = fileName.replace("\\", "/");

				String staticName = fileName.substring(root.length() - 1, fileName.length());
				
				staticName = getHttpPath(file, root);

				HttpEntity entity = new HttpEntity();

				entity.array = bytes;
				entity.contentType = contentType;
				entity.file = file;
				entity.lastModify = file.lastModified();

				html_cache.put(staticName, entity);

				LoggerUtil.prettyNIOServerLog(logger, "mapping static :{}@{}", staticName, fileName);
			} else if (file.isDirectory()) {
				
				File[] fs = file.listFiles();
				
				StringBuilder b = new StringBuilder(HtmlUtil.HTML_HEADER);
				
				b.append("		<div style=\"margin-left:20px;\">\n");
				b.append("			Index of "+getHttpPath(file, root)+"\n");
				b.append("		</div>\n");
				b.append("		<hr>\n");
				
				File rootFile = new File(root);
				
				if (!rootFile.equals(file)) {
					b.append("		<p>\n");
					b.append("			<a href=\""+getHttpPath(file.getParentFile(), root)+"\">&lt;dir&gt;..</a>\n");
					b.append("		</p>\n");
				}
				
				StringBuilder db = new StringBuilder();
				StringBuilder fb = new StringBuilder();
				
				for (File f : fs) {
					
					scanFolder(f, root,mapping);
					
					if (f.isDirectory()) {
						db.append("		<p>\n");
						db.append("			<a href=\""+getHttpPath(f, root)+"\">&lt;dir&gt;"+f.getName()+"</a>\n");
						db.append("		</p>\n");
					}else{
						fb.append("		<p>\n");
						fb.append("			<a href=\""+getHttpPath(f, root)+"\">"+f.getName()+"</a>\n");
						fb.append("		<p>\n");
					}
				}
				
				b.append(db);
				b.append(fb);
				
				b.append("		<hr>\n");
				b.append(HtmlUtil.HTML_BOTTOM);
				
				HttpEntity entity = new HttpEntity();
				
				entity.array = b.toString().getBytes(Encoding.DEFAULT);
				entity.contentType = HttpHeaderParser.CONTENT_TYPE_TEXT_HTML;
				
				String staticName = getHttpPath(file, root);
				
				if ("".equals(staticName)) {
					staticName = "/";
				}
				html_cache.put(staticName, entity);
			}
		}
	}
	
	private String getHttpPath(File file,String root) throws IOException{
		
		String fileName = file.getCanonicalPath();

		fileName = fileName.replace("\\", "/");

		String staticName = fileName.substring(root.length() - 1, fileName.length());
		
		if (StringUtil.isNullOrBlank(staticName)) {
			staticName = "/";
		}
		
		return staticName;
	}

	private String getContentType(String fileName, Map<String, String> mapping) {

		int index = fileName.lastIndexOf(".");

		if (index == -1) {
			return HttpHeaderParser.CONTENT_TYPE_TEXT_PLAIN;
		}

		String subfix = fileName.substring(index + 1);

		String contentType = mapping.get(subfix);

		if (contentType == null) {
			contentType = HttpHeaderParser.CONTENT_TYPE_TEXT_PLAIN;
		}

		return contentType;
	}

	private class HttpEntity {

		byte[]	array;

		String	contentType;
		
		File file;
		
		long lastModify;
	}
}
