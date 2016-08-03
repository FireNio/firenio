package com.gifisan.nio.extend.startup;

import java.io.IOException;
import java.util.Scanner;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.LoggerSEListener;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ConnectorCloseSEListener;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.gifisan.nio.extend.configuration.ServerConfiguration;
import com.gifisan.nio.extend.implementation.SYSTEMStopServerServlet;

public class NIOServerShutdown {

	public static void main(String[] args) throws IOException {

		NIOServerShutdown shutdown = new NIOServerShutdown();
		
		shutdown.shutdown(args);
		
	}

	public void shutdown(String [] args) throws IOException {
		
		if (args == null || args.length < 3) {

			System.out.print("参数不正确，按回车键退出>>");

			Scanner scanner = new Scanner(System.in);

			scanner.nextLine();

			return;
		}

		int port = Integer.valueOf(args[0]);

		String username = args[1];

		String password = args[2];
		
		ServerConfiguration serverConfiguration = new ServerConfiguration();
		
		serverConfiguration.setSERVER_TCP_PORT(port);

		String serviceName = SYSTEMStopServerServlet.SERVICE_NAME;

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = new TCPConnector();

		NIOContext context = new DefaultNIOContext();
		
		context.setServerConfiguration(serverConfiguration);

		context.setIOEventHandleAdaptor(eventHandle);

		context.addSessionEventListener(new LoggerSEListener());

		context.addSessionEventListener(new ConnectorCloseSEListener(connector));

		connector.setContext(context);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();
		
		session.login(username, password);
		
		NIOReadFuture future = session.request(serviceName, null);

		System.out.println(future.getText());

		CloseUtil.close(connector);

		System.out.print("按回车键退出>>");

		Scanner scanner = new Scanner(System.in);

		scanner.nextLine();
	}

}
