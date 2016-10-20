package com.generallycloud.nio.extend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;

public class FileReceiveUtil {

	public static final String	FILE_NAME	= "file-name";

	public static final String	IS_END		= "isEnd";

	private Logger				logger		= LoggerFactory.getLogger(FileReceiveUtil.class);

	private int				num;

	private String				prefix;

	private String				ACCEPT_FILE	= "accept-file";
	
	public FileReceiveUtil(String prefix) {
		this.prefix = prefix;
	}

	public void accept(Session session, NIOReadFuture future,boolean callback) throws Exception {

		Parameters parameters = future.getParameters();

		OutputStream outputStream = (OutputStream) session.getAttribute(ACCEPT_FILE);

		if (outputStream == null) {

			String fileName = prefix + parameters.getParameter(FILE_NAME);

			outputStream = new FileOutputStream(new File(fileName));

			session.setAttribute(ACCEPT_FILE, outputStream);

			logger.info("accept...................open,file={}", fileName);
		}

		byte[] data = future.getBinary();

		outputStream.write(data, 0, future.getBinaryLength());

		logger.info("accept...................{},{}", future.getBinaryLength(), (num++));

		boolean isEnd = parameters.getBooleanParameter(IS_END);

		if (isEnd) {

			logger.info("accept...................close,stream={}", outputStream);

			CloseUtil.close(outputStream);

			session.removeAttribute(ACCEPT_FILE);
			
			if (callback) {

				future.write("传输成功！");

				session.flush(future);
			}
		}
	}
}
