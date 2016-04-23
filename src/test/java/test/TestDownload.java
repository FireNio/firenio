package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.service.ServiceAcceptor;

public class TestDownload {
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestDownloadServlet";
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSession session = connector.getClientSession();
		
		session.onStream("upload-temp.zip", new ServiceAcceptor() {
			
			public void accept(Session session, ReadFuture future) throws Exception {
				File file = new File("download.zip");
				
				FileOutputStream outputStream = new FileOutputStream(file);
				
				future.setIOEvent(outputStream, null);
				
			}
		});
		
		serviceKey = "upload-temp.zip";
		
		long old = System.currentTimeMillis();
		
		ReadFuture future = session.request(serviceKey, null);
		
		if (!future.hasOutputStream()) {
			System.out.println(future.getText());
		}else{
			
			System.out.println("下载成功！");
		}
		
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		CloseUtil.close(connector);
		
	}
}
