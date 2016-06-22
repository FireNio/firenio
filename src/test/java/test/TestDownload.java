package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientStreamAcceptor;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.ReadFuture;

public class TestDownload {
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "upload-temp.zip";
		TCPConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSession session = connector.getClientSession();
		
		session.onStreamRead(serviceKey, new ClientStreamAcceptor() {
			
			public void accept(ClientSession session, ReadFuture future) throws Exception {
				File file = new File("download.zip");
				
				FileOutputStream outputStream = new FileOutputStream(file);
				
				future.setOutputIOEvent(outputStream, null);
			}
		});
		
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
