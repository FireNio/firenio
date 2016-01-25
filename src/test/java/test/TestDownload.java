package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.component.InputStream;

public class TestDownload {
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestDownloadServlet";
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();
		
		serviceKey = "upload-temp.zip";
		
		Response response = session.request(serviceKey, null);
		
		if (response.getType() == Response.TEXT) {
			System.out.println(response.getContent());
		}else{
			
			InputStream inputStream = response.getInputStream();
			
			File file = new File("download.zip");
			
			FileOutputStream outputStream = new FileOutputStream(file);
			
			
			int BLOCK = 102400;
			ByteBuffer BUFFER = ByteBuffer.allocate(BLOCK);
			int length = inputStream.read(BUFFER);
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
