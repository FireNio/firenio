package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.concurrent.Waiter;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.OnReadFuture;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.gifisan.nio.extend.implementation.SYSTEMDownloadServlet;

public class TestDownload {
	
	public static void main(String[] args) throws IOException {

		String serviceName = SYSTEMDownloadServlet.SERVICE_NAME;
		
		String fileName = "upload-temp.zip";
		
		JSONObject j = new JSONObject();
		j.put("fileName", fileName);
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandle);

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
