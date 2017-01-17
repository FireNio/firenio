/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.test.nio.udp;

import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Encoding;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.DatagramChannelContext;
import com.generallycloud.nio.component.DatagramChannelContextImpl;
import com.generallycloud.nio.component.DatagramPacketAcceptor;
import com.generallycloud.nio.component.DatagramSession;
import com.generallycloud.nio.component.LoggerDatagramSEListener;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.DatagramChannelConnector;
import com.generallycloud.nio.protocol.DatagramPacket;

public class TestUDPClient {

	public static void main(String[] args) throws Exception {
		
		DatagramPacketAcceptor acceptor = new DatagramPacketAcceptor() {
			
			@Override
			public void accept(DatagramSession session, DatagramPacket packet) throws IOException {
				System.out.println(packet.getDataString(Encoding.UTF8));
				
			}
		};

		DatagramChannelContext context = new DatagramChannelContextImpl(new ServerConfiguration("localhost", 18500));

		DatagramChannelConnector connector = new DatagramChannelConnector(context);

		context.addSessionEventListener(new LoggerDatagramSEListener());
		
		context.setDatagramPacketAcceptor(acceptor);

		DatagramSession session = connector.connect();
		
		DatagramPacket packet = new DatagramPacket("hello world!".getBytes());
		
		session.sendPacket(packet);

		ThreadUtil.sleep(30);

		CloseUtil.close(connector);

	}

}
