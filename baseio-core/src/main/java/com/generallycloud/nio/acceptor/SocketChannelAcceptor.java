package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Map;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SessionMEvent;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ReadFuture;

public final class SocketChannelAcceptor extends AbstractChannelAcceptor {
	
	private Logger logger = LoggerFactory.getLogger(SocketChannelAcceptor.class);

	public SocketChannelAcceptor(BaseContext context) {
		super(context);
	}

	protected void initselectableChannel() throws IOException {
		// 打开服务器套接字通道
		this.selectableChannel = ServerSocketChannel.open();
		// 服务器配置为非阻塞
		this.selectableChannel.configureBlocking(false);
		// 检索与此通道关联的服务器套接字
		this.serverSocket = ((ServerSocketChannel) selectableChannel).socket();

	}
	
	protected SelectorLoop newSelectorLoop(SelectorLoop[] selectorLoops) throws IOException {
		return new ServerSocketChannelSelectorLoop(this, selectorLoops);
	}

	protected void bind(BaseContext context, InetSocketAddress socketAddress) throws IOException {

		try {
			// 进行服务的绑定
			this.serverSocket.bind(socketAddress, 50);
		} catch (BindException e) {
			throw new BindException(e.getMessage() + " at " + socketAddress.getPort());
		}

		initSelectorLoops();
	}
	
	public void broadcast(final ReadFuture future) {

		offerSessionMEvent(new SessionMEvent() {

			public void fire(BaseContext context, Map<Integer, SocketSession> sessions) {
				
				Iterator<SocketSession> ss = sessions.values().iterator();
				
				SocketSession session = ss.next();
				
				if (sessions.size() == 1) {
					
					session.flush(future);
					
					return;
				}
				
				ProtocolEncoder encoder = context.getProtocolEncoder();
				
				ByteBufAllocator allocator = UnpooledByteBufAllocator.getInstance();
				
				ChannelWriteFuture writeFuture;
				try {
					writeFuture = encoder.encode(allocator, (ChannelReadFuture) future);
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
					return;
				}
				
				for (; ss.hasNext();) {

					SocketSession s = (SocketSession) ss.next();

					ChannelWriteFuture copy = writeFuture.duplicate();

					s.flush(copy);

				}
				
				ReleaseUtil.release(writeFuture);
			}
		});
	}

}
