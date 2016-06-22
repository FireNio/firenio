package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.component.future.ReadFuture;

public class TestUpload {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestUploadServlet";
		String param = "temp.zip";
		TCPConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSession session = connector.getClientSession();
		
		long old = System.currentTimeMillis();
		File file = new File("D:/GIT/NimbleIO/temp1.zip");
		FileInputStream inputStream = new FileInputStream(file);
		ReadFuture future = session.request(serviceKey,param , inputStream);
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		System.out.println(future.getText());
		
		connector.close();
	}
}
