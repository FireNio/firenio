package com.generallycloud.test.nio.nio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.implementation.SYSTEMDownloadServlet;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestDownload {
	
	public static void main(String[] args) throws Exception {

		String serviceName = SYSTEMDownloadServlet.SERVICE_NAME;
		
		String fileName = "upload-temp.zip";
		
		JSONObject j = new JSONObject();
		j.put("fileName", fileName);
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();
		
		final Waiter w = new Waiter();
		
		session.listen(serviceName, new OnReadFuture() {
			
			public void onResponse(Session session, ReadFuture future) {
				
				NIOReadFuture f = (NIOReadFuture) future;
				
				Parameters parameters = f.getParameters();
				
				OutputStream outputStream = (OutputStream) session.getAttachment();
				
				try {
					if (outputStream == null) {
						
						String fileName = "download-" + f.getText();
						
						outputStream = new FileOutputStream(new File(fileName));
						
						session.setAttachment(outputStream);
					}
					
					byte [] data = f.getBinary();
					
					outputStream.write(data);
					
					boolean isEnd = parameters.getBooleanParameter("isEnd");
					
					if (isEnd) {
						
						CloseUtil.close(outputStream);
						
						session.setAttachment(null);
						
						w.setPayload(null);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		long old = System.currentTimeMillis();
		
		session.write(serviceName, j.toJSONString());
		
		w.await(999999);
		
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		CloseUtil.close(connector);
		
	}
}
