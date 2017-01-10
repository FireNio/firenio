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
package com.generallycloud.nio.component;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.protocol.DatagramPacket;

public class DatagramChannelSelectionReader implements SelectionAcceptor {

	private DatagramChannelContext		context		= null;
	private DatagramSelectorEventLoopImpl	selectorLoop	= null;
	private ByteBuf					cacheBuffer	= null;
	private Logger						logger		= LoggerFactory
			.getLogger(DatagramChannelSelectionReader.class);

	public DatagramChannelSelectionReader(DatagramSelectorEventLoopImpl selectorLoop) {
		this.selectorLoop = selectorLoop;
		this.context = selectorLoop.getContext();
		this.cacheBuffer = UnpooledByteBufAllocator.getInstance().allocate(DatagramPacket.PACKET_MAX);
	}

	@Override
	public void accept(SelectionKey selectionKey) throws Exception {

		DatagramChannelContext context = this.context;

		ByteBuf buf = this.cacheBuffer;

		buf.clear();

		DatagramChannel channel = (DatagramChannel) selectionKey.channel();

		ByteBuffer nioBuffer = buf.nioBuffer();
		
		InetSocketAddress remoteSocketAddress = (InetSocketAddress) channel.receive(nioBuffer);
		
		buf.skipBytes(nioBuffer.position());

		DatagramPacket packet = new DatagramPacket(buf);

		DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();

		if (acceptor == null) {
			logger.debug("______________ none acceptor for context");
			return;
		}
		
		DatagramSessionManager manager = context.getSessionManager();

		acceptor.accept(manager.getSession(selectorLoop, channel, remoteSocketAddress), packet);

	}
	
}
