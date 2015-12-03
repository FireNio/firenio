package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.yoocent.mtp.client.NIOClient;
import com.yoocent.mtp.client.Response;
import com.yoocent.mtp.servlet.test.TestUploadServlet;

public class TestUpload {
	
	
	public static void main(String[] args) throws IOException {

		long timeout = 100000000;
		String serviceKey = TestUploadServlet.SERVICE_NAME;
		String param = "{fileName:\"temp.zip\"}";
		NIOClient client = ClientUtil.getClient();
		
		client.connect();
		File file = new File("D:/temp1.zip");
		FileInputStream inputStream = new FileInputStream(file);
		Response response = client.request(serviceKey,param , inputStream, timeout);
		client.close();
		
		System.out.println(response.getContent());
	}
}
