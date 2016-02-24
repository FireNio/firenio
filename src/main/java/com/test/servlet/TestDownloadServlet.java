package com.test.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.component.RequestParam;
import com.gifisan.nio.server.NIOServlet;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;

public class TestDownloadServlet extends NIOServlet {

	private int			BLOCK		= 102400;

	public void accept(Request request, Response response) throws Exception {
		String filePath = request.getContent();
		RequestParam param = request.getParameters();
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

			response.setStreamResponse(downloadLength);

			inputStream.skip(start);

			int BLOCK = this.BLOCK;

			if (BLOCK > downloadLength) {
				byte[] bytes = new byte[downloadLength];
				inputStream.read(bytes);
				response.write(bytes);
			} else {
				byte[] bytes = new byte[BLOCK];
				int times = downloadLength / BLOCK;
				int remain = downloadLength % BLOCK;
				while (times > 0) {
					inputStream.read(bytes);
					response.write(bytes);
					times--;
				}
				if (remain > 0) {
					inputStream.read(bytes, 0, remain);
					response.write(bytes, 0, remain);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			CloseUtil.close(inputStream);
		}

	}

}
