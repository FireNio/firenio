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
package com.generallycloud.nio.container.rtp;

import java.io.IOException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.DatagramSession;
import com.generallycloud.nio.component.DatagramChannelContext;
import com.generallycloud.nio.component.DatagramPacketAcceptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.DatagramPacket;
import com.generallycloud.nio.protocol.DatagramRequest;

public abstract class ServerDatagramPacketAcceptor implements DatagramPacketAcceptor {
	
	private Logger logger = LoggerFactory.getLogger(ServerDatagramPacketAcceptor.class);

	@Override
	public void accept(DatagramSession session, DatagramPacket packet) throws IOException {

		DatagramChannelContext context = session.getContext();

		DatagramRequest request = DatagramRequest.create(packet, context);

		if (request != null) {
			execute(session,request);
			return;
		}
		
//		logger.debug("___________________server receive,packet:{}",packet);
		
//		SocketSession session = channel.getSession();
		
		if (session == null) {
			logger.debug("___________________null session,packet:{}",packet);
			return;
		}
		
//		doAccept(channel, packet,session); //FIXME UDP
	}
	
	protected abstract void doAccept(DatagramSession channel, DatagramPacket packet,SocketSession session) throws IOException ;
	
	protected abstract void execute(DatagramSession channel,DatagramRequest request) ;

}
