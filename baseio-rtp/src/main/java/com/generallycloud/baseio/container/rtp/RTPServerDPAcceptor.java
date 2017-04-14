/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.container.rtp;

import java.io.IOException;

import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.baseio.common.ByteUtil;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.component.DatagramSession;
import com.generallycloud.baseio.component.Parameters;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SocketSessionManager;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.ApplicationContextUtil;
import com.generallycloud.baseio.container.LoginCenter;
import com.generallycloud.baseio.container.authority.AuthorityManager;
import com.generallycloud.baseio.container.rtp.server.RTPRoom;
import com.generallycloud.baseio.container.rtp.server.RTPSessionAttachment;
import com.generallycloud.baseio.protocol.DatagramPacket;
import com.generallycloud.baseio.protocol.DatagramRequest;

public class RTPServerDPAcceptor extends ServerDatagramPacketAcceptor {
	
	public static final String BIND_SESSION = "BIND_SESSION";
	
	public static final String BIND_SESSION_CALLBACK = "BIND_SESSION_CALLBACK";
	
	public static final String SERVICE_NAME = RTPServerDPAcceptor.class.getSimpleName();
	
	private Logger logger = LoggerFactory.getLogger(RTPServerDPAcceptor.class);
	
	private RTPContext context = null;
	
	protected RTPServerDPAcceptor(RTPContext context) {
		this.context = context;
	}

	@Override
	public void doAccept(DatagramSession dSession, DatagramPacket packet,SocketSession session) throws IOException {

		AuthorityManager authorityManager = ApplicationContextUtil.getAuthorityManager(session);
		
		if (authorityManager == null) {
			logger.debug("___________________null authority,packet:{}",packet);
			return;
		}
		
		if (!authorityManager.isInvokeApproved(getSERVICE_NAME())) {
			logger.debug("___________________not approved,packet:{}",packet);
			return;
		}
		
		RTPSessionAttachment attachment = (RTPSessionAttachment)session.getAttribute(context.getPluginKey());
		
		RTPRoom room = attachment.getRtpRoom();
		
		if (room != null) {
			room.broadcast(dSession, packet);
		}else{
			logger.debug("___________________null room,packet:{}",packet);
		}
	}
	
	@Override
	protected void execute(DatagramSession dSession,DatagramRequest request) {
		
		String serviceName = request.getFutureName();

		if (BIND_SESSION.equals(serviceName)) {
			
			Parameters parameters = request.getParameters();
			
			ApplicationContext context = ApplicationContext.getInstance();
			
			LoginCenter loginCenter = context.getLoginCenter();
			
			if (!loginCenter.isValidate(parameters)) {
				return;
			}
			
			//FIXME udp
			
			SocketChannelContext channelContext = context.getChannelContext();
			
			SocketSessionManager sessionManager = channelContext.getSessionManager();
			
//			Session session = factory.getSession(username);
			
			SocketSession session = null;  
			
			if (session == null) {
				return ;
			}
			
//			session.setDatagramChannel(channel); //FIXME udp 
			
			ProtobaseReadFuture future = new ProtobaseReadFutureImpl(session.getContext(),BIND_SESSION_CALLBACK);
			
			future.setIoEventHandle(session.getContext().getIoEventHandleAdaptor());
			
			logger.debug("___________________bind___session___{}",session);
			
			future.write(ByteUtil.TRUE);
			
			session.flush(future);
			
		}else{
			logger.debug(">>>> {}",request.getFutureName());
		}
	}

	protected String getSERVICE_NAME() {
		return SERVICE_NAME;
	}
}
