package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.concurrent.Waiter;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.OnReadFuture;
import com.gifisan.nio.extend.implementation.SYSTEMDownloadServlet;

public class TestDownload {
	
	public static void main(String[] args) throws IOException {

		String serviceName = SYSTEMDownloadServlet.SERVICE_NAME;
		
		String fileName = "upload-temp.zip";
		
		JSONObject j = new JSONObject();
		j.put("fileName", fileName);
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();
		
		final Waiter w = new Waiter();
		
		session.listen(serviceName, new OnReadFuture() {
			
			public void onResponse(FixedSession session, ReadFuture future) {
				
				try {
					if (future.hasOutputStream()) {
						
						if (future.getOutputStream() == null) {
							
							File file = new File("download.zip");
							
							FileOutputStream outputStream = new FileOutputStream(file);
							
							future.setOutputStream(outputStream);
							
							return;
						}
					}
					
					System.out.println("_________"+future.getText());
					
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
