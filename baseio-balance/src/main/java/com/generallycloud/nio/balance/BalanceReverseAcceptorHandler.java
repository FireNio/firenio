package com.generallycloud.nio.balance;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.balance.router.BalanceRouter;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.SocketSessionManager.SocketSessionManagerEvent;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ReadFuture;

public class BalanceReverseAcceptorHandler extends IoEventHandleAdaptor {

	private Logger			logger	= LoggerFactory.getLogger(BalanceReverseAcceptorHandler.class);
	private BalanceContext	balanceContext;
	private BalanceRouter	balanceRouter;

	public BalanceReverseAcceptorHandler(BalanceContext balanceContext) {
		this.balanceContext = balanceContext;
		this.balanceRouter = balanceContext.getBalanceRouter();
	}

	private void broadcast(final BalanceReadFuture future) {

		BalanceFacadeAcceptor balanceFacadeAcceptor = balanceContext.getBalanceFacadeAcceptor();

		SocketChannelAcceptor acceptor = balanceFacadeAcceptor.getAcceptor();

		acceptor.offerSessionMEvent(new SocketSessionManagerEvent() {

			public void fire(SocketChannelContext context, Map<Integer, SocketSession> sessions) {

				BalanceReadFuture f = future.translate();

				Iterator<SocketSession> ss = sessions.values().iterator();

				SocketSession session = ss.next();

				if (sessions.size() == 1) {

					session.flush(f);

					return;
				}

				ProtocolEncoder encoder = context.getProtocolEncoder();

				ByteBufAllocator allocator = UnpooledByteBufAllocator.getInstance();

				ChannelWriteFuture writeFuture;
				try {
					writeFuture = encoder.encode(allocator, (ChannelReadFuture) f);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					return;
				}

				for (; ss.hasNext();) {

					BalanceFacadeSocketSession s = (BalanceFacadeSocketSession) ss.next();

					if (!s.isReceiveBroadcast()) {
						continue;
					}

					ChannelWriteFuture copy = writeFuture.duplicate();

					try {

						s.flush(copy);

					} catch (Exception e) {

						logger.error(e.getMessage(), e);
					}
				}

				ReleaseUtil.release(writeFuture);
			}
		});
	}

	public void accept(SocketSession session, ReadFuture future) throws Exception {

		BalanceReadFuture f = (BalanceReadFuture) future;

		if (f.isBroadcast()) {

			broadcast(f);

			logger.info("广播报文：F：{}，报文：{}", session.getRemoteSocketAddress(), future);

			return;
		}

		int sessionID = f.getFrontSessionID();

		SocketSession response = balanceRouter.getClientSession(sessionID);

		if (response == null || response.isClosed()) {

			logger.info("连接丢失：F：{}，报文：{}", session.getRemoteSocketAddress(), future);

			return;
		}

		response.flush(f.translate());

		logger.info("回复报文：F：[{}]，T：[{}]，报文：{}",new Object[] { 
				session.getRemoteSocketAddress(), 
				response.getRemoteSocketAddress(), 
				f
		});
	}

	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {

		String msg = future.toString();

		if (msg.length() > 100) {
			msg = msg.substring(0, 100);
		}

		logger.error("exceptionCaught,msg=" + msg, cause);
	}

}
