package com.generallycloud.test.nio.nio;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.nio.NIOProtocolFactory;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FileReceiveUtil;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.implementation.SYSTEMDownloadServlet;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestDownload {
	
	public static void main(String[] args) throws Exception {

		String serviceName = SYSTEMDownloadServlet.SERVICE_NAME;
		
		String fileName = "upload-flashmail-2.4.exe";
		
		JSONObject j = new JSONObject();
		j.put(FileReceiveUtil.FILE_NAME, fileName);
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setProtocolFactory(new NIOProtocolFactory());

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();
		
		final FileReceiveUtil fileReceiveUtil = new FileReceiveUtil("download-");
		
		session.listen(serviceName, new OnReadFuture() {
			
			public void onResponse(Session session, ReadFuture future) {
				
				try {
					fileReceiveUtil.accept(session, (NIOReadFuture) future,false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		long old = System.currentTimeMillis();
		
		session.write(serviceName, j.toJSONString());
		
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		ThreadUtil.sleep(5000);
		
		CloseUtil.close(connector);
		
	}
}
