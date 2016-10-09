package com.generallycloud.test.nio.nio;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.test.service.nio.TestUploadServlet;

public class TestUpload {

	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");
		
		String serviceKey = TestUploadServlet.SERVICE_NAME;

		Map params = new HashMap();
		params.put("fileName", "temp.zip");

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		long old = System.currentTimeMillis();
		File file = new File("D:/GIT/NimbleIO/temp1.zip");
		FileInputStream inputStream = new FileInputStream(file);
		NIOReadFuture future = session.request(serviceKey, params, inputStream);
		System.out.println("Time:" + (System.currentTimeMillis() - old));
		System.out.println(future.getText());

		connector.close();
	}
}
