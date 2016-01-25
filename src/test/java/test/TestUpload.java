package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.client.Response;

public class TestUpload {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestUploadServlet";
		String param = "temp.zip";
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();
		
		long old = System.currentTimeMillis();
		File file = new File("D:/temp1.zip");
		FileInputStream inputStream = new FileInputStream(file);
		Response response = session.request(serviceKey,param , inputStream);
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		System.out.println(response.getContent());
		
		connector.close();
	}
}
