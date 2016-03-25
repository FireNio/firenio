package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.Response;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.StreamUtil;
import com.gifisan.nio.component.InputStream;
import com.gifisan.nio.component.ProtocolDecoder;

public class TestDownload {
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestDownloadServlet";
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();
		
		serviceKey = "upload-temp.zip";
		
		long old = System.currentTimeMillis();
		
		Response response = session.request(serviceKey, null);
		
		if (response.getProtocolType() == ProtocolDecoder.TEXT) {
			System.out.println(response.getText());
		}else{
			
			InputStream inputStream = response.getInputStream();
			
			File file = new File("download.zip");
			
			FileOutputStream outputStream = new FileOutputStream(file);
			
			StreamUtil.write(inputStream, outputStream, 102400);
			
			CloseUtil.close(outputStream);
			System.out.println("下载成功！");
		}
		
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		CloseUtil.close(connector);
		
	}
}
