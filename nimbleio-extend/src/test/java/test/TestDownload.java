package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.implementation.SYSTEMDownloadServlet;

public class TestDownload {
	
	public static void main(String[] args) throws IOException {

		String serviceName = SYSTEMDownloadServlet.SERVICE_NAME;
		
		String fileName = "upload-temp.zip";
		
		JSONObject j = new JSONObject();
		j.put("fileName", fileName);
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();
		
		final Waiter w = new Waiter();
		
		session.listen(serviceName, new OnReadFuture() {
			
			public void onResponse(Session session, ReadFuture future) {
				
				NIOReadFuture f = (NIOReadFuture) future;
				
				try {
					if (f.hasOutputStream()) {
						
						if (f.getOutputStream() == null) {
							
							File file = new File("download.zip");
							
							FileOutputStream outputStream = new FileOutputStream(file);
							
							f.setOutputStream(outputStream);
							
							return;
						}
					}
					
					System.out.println("_________"+f.getText());
					
					w.setPayload(null);
					
				} catch (FileNotFoundException e) {
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
