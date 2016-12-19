package com.generallycloud.test.nio.protobase;

import java.io.File;

import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FileSendUtil;
import com.generallycloud.nio.container.protobase.example.TestUploadServlet;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestUpload {

	static SocketChannelConnector connector = null;
	
	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");
		
		String serviceName = TestUploadServlet.SERVICE_NAME;

		IoEventHandleAdaptor eventHandle = new IoEventHandleAdaptor() {
			
			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				ProtobaseReadFuture f = (ProtobaseReadFuture) future;
				System.out.println();
				System.out.println(f.getReadText());
				System.out.println();
				
				CloseUtil.close(connector);
				
			}

			@Override
			public void futureSent(SocketSession session, ReadFuture future) {
				ProtobaseReadFuture f = (ProtobaseReadFuture) future;
				System.out.println("报文已发送："+f.getReadText());
			}
		};

		connector = IoConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setProtocolFactory(new ProtobaseProtocolFactory());

		SocketSession session = connector.connect();
		
		String fileName = "lantern-installer-beta.exe";
		
		fileName = "flashmail-2.4.exe";
		
//		fileName = "jdk-8u102-windows-x64.exe";
		
		File file = new File("D:/TEMP/"+fileName);
		
		FileSendUtil fileSendUtil = new FileSendUtil();
		
		fileSendUtil.sendFile(session, serviceName, file, 1024 * 800);
		
		
		
	}
}
