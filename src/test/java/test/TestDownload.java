package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.OnReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;

public class TestDownload {
	
	public static void main(String[] args) throws IOException {

		String serviceName = "upload-temp.zip";
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();
		
		session.listen(serviceName, new OnReadFuture() {
			
			public void onResponse(FixedSession session, ReadFuture future) {
				
				try {
					if (future.hasOutputStream()) {
						
						if (future.getOutputStream() == null) {
							
							File file = new File("download.zip");
							
							FileOutputStream outputStream = new FileOutputStream(file);
							
							future.setOutputStream(outputStream);
						}
					}
					System.out.println("_________"+future.getText());
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		long old = System.currentTimeMillis();
		
		ReadFuture future = session.request(serviceName, null);
		
		if (!future.hasOutputStream()) {
			System.out.println(future.getText());
		}else{
			
			System.out.println("下载成功！");
		}
		
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		CloseUtil.close(connector);
		
	}
}
