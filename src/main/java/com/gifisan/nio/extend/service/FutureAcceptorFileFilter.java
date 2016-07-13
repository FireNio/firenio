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
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.DynamicClassLoader;
import com.gifisan.nio.extend.configuration.Configuration;

public class FutureAcceptorFileFilter extends FutureAcceptorServiceFilter {

	public FutureAcceptorFileFilter(DynamicClassLoader classLoader) {
		super(classLoader);
	}

	private Logger				logger		= LoggerFactory.getLogger(FutureAcceptorFileFilter.class);

	private Map<String, byte[]>	html_cache	= new HashMap<String, byte[]>();

	protected void accept404(Session session, ReadFuture future, String serviceName) throws IOException {

		byte [] array = html_cache.get(serviceName);
		
		if (array == null) {
			array = html_cache.get("/index.html");
			if (array == null) {
				super.accept404(session, future, serviceName);
				return;
			}
		}
		
		future.write(array);
		session.flush(future);
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		String rootPath = context.getAppLocalAddress();
		
		File rootFile = new File(rootPath); 

		scanFolder(rootFile, rootPath);
		
		super.initialize(context, config);
	}

	private void scanFolder(File file, String root) throws IOException {
		if (file.exists()) {
			if (file.isFile()) {
				byte[] bytes = FileUtil.readFileToByteArray(file);
				String fileName = file.getCanonicalPath();
				String staticName = fileName.substring(root.length() - 1, fileName.length());
				staticName = staticName.replace("\\", "/");
				html_cache.put(staticName, bytes);
				LoggerUtil.prettyNIOServerLog(logger, "mapping static :{}@{}",staticName,fileName);
			} else if (file.isDirectory()) {
				File [] fs = file.listFiles();
				for(File f : fs){
					scanFolder(f, root);
				}
			}
		}
	}
}
