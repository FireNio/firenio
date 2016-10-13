package com.generallycloud.test.nio.nio;

import java.io.File;

import com.generallycloud.nio.codec.nio.NIOProtocolFactory;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FileSendUtil;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.protocol.ReadFuture;
import com.test.service.nio.TestUploadServlet;

public class TestUpload {

	static SocketChannelConnector connector = null;
	
	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");
		
		String serviceName = TestUploadServlet.SERVICE_NAME;

		IOEventHandleAdaptor eventHandle = new IOEventHandleAdaptor() {
			
			public void accept(Session session, ReadFuture future) throws Exception {
				NIOReadFuture f = (NIOReadFuture) future;
				System.out.println();
				System.out.println(f.getText());
				System.out.println();
				
				CloseUtil.close(connector);
				
			}

			public void futureSent(Session session, ReadFuture future) {
				NIOReadFuture f = (NIOReadFuture) future;
				System.out.println("报文已发送："+f.getWriteBuffer());
			}
		};

		connector = IOConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setProtocolFactory(new NIOProtocolFactory());

		Session session = connector.connect();
		
		String fileName = "lantern-installer-beta.exe";
		
		fileName = "flashmail-2.4.exe";
		
//		fileName = "jdk-8u102-windows-x64.exe";
		
		File file = new File("D:/TEMP/"+fileName);
		
		FileSendUtil fileSendUtil = new FileSendUtil();
		
		fileSendUtil.sendFile(session, serviceName, file, 1024 * 800);
		
		
		
	}
}
