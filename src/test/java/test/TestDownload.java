package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.Response;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.InputStream;
import com.gifisan.nio.component.ProtocolDecoder;

public class TestDownload {
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestDownloadServlet";
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();
		
		serviceKey = "upload-temp.zip";
		
		Response response = session.request(serviceKey, null);
		
		if (response.getProtocolType() == ProtocolDecoder.TEXT) {
			System.out.println(response.getText());
		}else{
			
			InputStream inputStream = response.getInputStream();
			
			File file = new File("download.zip");
			
			FileOutputStream outputStream = new FileOutputStream(file);
			
			
			int BLOCK = 102400;
			ByteBuffer BUFFER = ByteBuffer.allocate(BLOCK);
			inputStream.completedRead(BUFFER);
			
			int length = BUFFER.limit();
			
			while (length == BLOCK) {
				outputStream.write(BUFFER.array());
				BUFFER.clear();
				length = inputStream.read(BUFFER);
			}
			
			if (length > 0) {
				outputStream.write(BUFFER.array());
			}
			
			CloseUtil.close(outputStream);
			System.out.println("下载成功！");
		}
		
		CloseUtil.close(connector);
		
	}
}
