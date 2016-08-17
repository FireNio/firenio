package com.gifisan.nio.extend.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.common.FileUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.LoggerUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpHeaderParser;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.DynamicClassLoader;
import com.gifisan.nio.extend.configuration.Configuration;

public class FutureAcceptorHttpFilter extends FutureAcceptorServiceFilter {

	public FutureAcceptorHttpFilter(DynamicClassLoader classLoader) {
		super(classLoader);
	}

	private Logger					logger		= LoggerFactory.getLogger(FutureAcceptorHttpFilter.class);

	private Map<String, HttpEntity>	html_cache	= new HashMap<String, HttpEntity>();

	protected void accept404(Session session, ReadFuture future, String serviceName) throws IOException {

		String _service_name = serviceName;
		
		if ("/".equals(_service_name)) {
			_service_name = "/index.html";
		}
		
		HttpEntity entity = html_cache.get(_service_name);

		if (entity == null) {
			entity = html_cache.get("/404.html");
			if (entity == null) {
				super.accept404(session, future, serviceName);
				return;
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

		scanFolder(rootFile, rootPath);

		super.initialize(context, config);
	}

	private void scanFolder(File file, String root) throws IOException {

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
		// mapping.put("", HttpHeaderParser.);

		if (file.exists()) {
			if (file.isFile()) {

				String contentType = getContentType(file.getName(), mapping);

				byte[] bytes = FileUtil.readFileToByteArray(file);

				String fileName = file.getCanonicalPath();

				fileName = fileName.replace("\\", "/");

				String staticName = fileName.substring(root.length() - 1, fileName.length());

				HttpEntity entity = new HttpEntity();

				entity.array = bytes;
				entity.contentType = contentType;

				html_cache.put(staticName, entity);

				LoggerUtil.prettyNIOServerLog(logger, "mapping static :{}@{}", staticName, fileName);
			} else if (file.isDirectory()) {
				File[] fs = file.listFiles();
				for (File f : fs) {
					scanFolder(f, root);
				}
			}
		}
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
	}
}
