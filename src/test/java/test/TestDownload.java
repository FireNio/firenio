package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.yoocent.mtp.client.NIOClient;
import com.yoocent.mtp.client.Response;
import com.yoocent.mtp.common.CloseUtil;
import com.yoocent.mtp.servlet.test.TestDownloadServlet;

public class TestDownload {
	
	public static void main(String[] args) throws IOException {

		long timeout = 100000000;
		String serviceKey = TestDownloadServlet.SERVICE_KEY;
		NIOClient client = ClientUtil.getClient();
		
		client.connect();
		Response response = client.request(serviceKey, null , timeout);
		InputStream inputStream = response.getInputStream();
		byte [] bytes = new byte[1024];
		int length = inputStream.read(bytes);
		
		File file = new File("download.zip");
		
		FileOutputStream outputStream = new FileOutputStream(file);
		
		while(length == 1024){
			outputStream.write(bytes);
			length = inputStream.read(bytes);
		}
		
		if (length > 0) {
			outputStream.write(bytes,0,length);
		}
		
		CloseUtil.close(outputStream);
		CloseUtil.close(client);
		
		System.out.println("下载成功！");
	}
}
