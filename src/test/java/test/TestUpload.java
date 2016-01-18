package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;

public class TestUpload {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestUploadServlet";
		String param = "temp.zip";
		NIOClient client = ClientUtil.getClient();
		
		long old = System.currentTimeMillis();
		client.connect();
		File file = new File("D:/temp1.zip");
		FileInputStream inputStream = new FileInputStream(file);
		Response response = client.request(serviceKey,param , inputStream);
		client.close();
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		System.out.println(response.getContent());
	}
}
