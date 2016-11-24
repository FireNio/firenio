package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.Map;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.AbstractChannelService;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SessionMEvent;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class AbstractChannelAcceptor extends AbstractChannelService  implements ChannelAcceptor{

	protected ServerSocket serverSocket;
	
	private Logger			logger		= LoggerFactory.getLogger(AbstractChannelAcceptor.class);
	
	public AbstractChannelAcceptor(BaseContext context) {
		super(context);
	}
	
	public void bind() throws IOException {
		
		this.service();
	}
	
	protected void initService(ServerConfiguration configuration) throws IOException {
		
		this.serverAddress = new InetSocketAddress(configuration.getSERVER_PORT());

		this.bind(context, getServerSocketAddress());
		
		LoggerUtil.prettyNIOServerLog(logger, "监听已启动 @{}",getServerSocketAddress());
	}

	protected abstract void bind(BaseContext context, InetSocketAddress socketAddress) throws IOException;

	public void broadcast(final ReadFuture future) {

		offerSessionMEvent(new SessionMEvent() {

			public void fire(BaseContext context, Map<Integer, Session> sessions) {
				
				Iterator<Session> ss = sessions.values().iterator();
				
				Session session = ss.next();
				
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

					Session s = ss.next();

					ChannelWriteFuture copy = writeFuture.duplicate();

					s.flush(copy);

				}
				
				ReleaseUtil.release(writeFuture);
			}
		});
	}

	public boolean isActive() {
		return active;
	}

	public void offerSessionMEvent(SessionMEvent event) {
		context.getSessionManager().offerSessionMEvent(event);
	}
	
	public int getManagedSessionSize() {
		return context.getSessionManager().getManagedSessionSize();
	}

	public void unbind() {
		cancelService();
	}

}
