package com.gifisan.nio.component;

import java.io.IOException;
import java.util.Scanner;

import com.gifisan.nio.client.ClientIOEventHandle;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.service.impl.SYSTEMStopServerServlet;

public class ServerShutdown {

	public static void main(String[] args) throws IOException {

		ServerShutdown shutdown = new ServerShutdown();
		
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

		String serviceName = SYSTEMStopServerServlet.SERVICE_NAME;

		TCPConnector connector = new TCPConnector(new ClientIOEventHandle(),"M");
		
		connector.connect();
		
		connector.login(username, password);
		
		ClientSession session = connector.getClientSession();

		ReadFuture future = session.request(serviceName, null);

		System.out.println(future.getText());

		CloseUtil.close(connector);

		System.out.print("按回车键退出>>");

		Scanner scanner = new Scanner(System.in);

		scanner.nextLine();
	}

}
