package test;

import java.io.File;
import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.FileUtil;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;

public class TestSimpleBigParam {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestSimpleServlet";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		String temp = "网易科技腾讯科技阿里巴巴";
		StringBuilder builder = new StringBuilder(temp);
		for (int i = 0; i < 600000; i++) {
			builder.append("\n");
			builder.append(temp);
		}
		NIOReadFuture future = session.request(serviceKey, builder.toString());
		FileUtil.write(new File(TestSimpleBigParam.class.getName()), future.getText());
		System.out.println("处理完成");
		
		CloseUtil.close(connector);
		
		
		
	}
}
