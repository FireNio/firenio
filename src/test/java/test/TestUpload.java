package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.gifisan.nio.common.IOConnectorUtil;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;

public class TestUpload {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestUploadServlet";
		String param = "temp.zip";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();
		
		long old = System.currentTimeMillis();
		File file = new File("D:/GIT/NimbleIO/temp1.zip");
		FileInputStream inputStream = new FileInputStream(file);
		NIOReadFuture future = session.request(serviceKey,param , inputStream);
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		System.out.println(future.getText());
		
		connector.close();
	}
}
