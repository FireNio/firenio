package test;

import java.io.File;
import java.io.IOException;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.FileUtil;
import com.gifisan.nio.component.ClientLauncher;
import com.gifisan.nio.component.future.ReadFuture;

public class TestSimpleBigParam {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestSimpleServlet";
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		String temp = "网易科技腾讯科技阿里巴巴";
		StringBuilder builder = new StringBuilder(temp);
		for (int i = 0; i < 600000; i++) {
			builder.append("\n");
			builder.append(temp);
		}
		ReadFuture future = session.request(serviceKey, builder.toString());
		FileUtil.write(new File(TestSimpleBigParam.class.getName()), future.getText());
		System.out.println("处理完成");
		
		CloseUtil.close(connector);
		
		
		
	}
}
