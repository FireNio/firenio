package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.component.ClientLauncher;
import com.gifisan.nio.component.future.ReadFuture;

public class TestUpload {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestUploadServlet";
		String param = "temp.zip";
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();
		
		long old = System.currentTimeMillis();
		File file = new File("D:/GIT/NimbleIO/temp1.zip");
		FileInputStream inputStream = new FileInputStream(file);
		ReadFuture future = session.request(serviceKey,param , inputStream);
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		System.out.println(future.getText());
		
		connector.close();
	}
}
