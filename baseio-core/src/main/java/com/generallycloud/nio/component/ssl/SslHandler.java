package com.generallycloud.nio.component.ssl;

import java.io.IOException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.EmptyByteBuf;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.EmptyReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ReadFuture;

public class SslHandler {

	private SocketChannelContext	context	= null;

//	private Logger		logger	= LoggerFactory.getLogger(SslHandler.class);

	public SslHandler(SocketChannelContext context) {
		this.context = context;
	}

	private ByteBuf allocate(Session session, int capacity) {
		return session.getByteBufAllocator().allocate(capacity);
	}

	public ByteBuf wrap(SocketSession session,ByteBuf src) throws IOException {
		
		SSLEngine engine = session.getSSLEngine();
		
		ByteBuf dst = allocate(session,engine.getSession().getPacketBufferSize() * 2);

		try {

			for (;;) {

				SSLEngineResult result = engine.wrap(src.nioBuffer(), dst.nioBuffer());

				Status status = result.getStatus();
				HandshakeStatus handshakeStatus = result.getHandshakeStatus();

//				logger.debug("_________________________wrap");
//				logger.debug("_________________________,{}" , status.name());
//				logger.debug("_________________________,{}" , handshakeStatus.name());
				
				synchByteBuf(result, src, dst);

				if (status == Status.CLOSED) {
					return gc(session,dst);
				} else {
					switch (handshakeStatus) {
					case NEED_UNWRAP:
					case NOT_HANDSHAKING:
						return gc(session,dst);
					case NEED_TASK:
						runDelegatedTasks(engine);
						break;
					case FINISHED:
						session.finishHandshake(null);
						break;
					default:
						// throw new
						// IllegalStateException("unknown handshake status: "
						// + handshakeStatus);
						break;
					}
				}
			}
		} catch (Throwable e) {

			ReleaseUtil.release(dst);

			if (e instanceof IOException) {

				throw (IOException) e;
			}

			throw new IOException(e);
		}
	}

	//FIXME 部分buf不需要gc
	private ByteBuf gc(Session session,ByteBuf buf) throws IOException {

		buf.flip();

		ByteBuf out = allocate(session,buf.limit());

		try {

			out.read(buf);

		} catch (Exception e) {

			ReleaseUtil.release(out);

			throw e;
		}

		ReleaseUtil.release(buf);

		out.flip();

		return out;
	}

	public ByteBuf unwrap(SocketSession session, ByteBuf src) throws IOException {

//		logger.debug("__________________________________________________start");

		ByteBuf dst = allocate(session,src.capacity() * 2);

		boolean release = true;

		SSLEngine sslEngine = session.getSSLEngine();

		try {
			for (;;) {

				SSLEngineResult result = sslEngine.unwrap(src.nioBuffer(), dst.nioBuffer());
				
//				Status status = result.getStatus();
				HandshakeStatus handshakeStatus = result.getHandshakeStatus();

//				logger.debug("_________________________unwrap");
//				logger.debug("_________________________,{}" , status.name());
//				logger.debug("_________________________,{}" , handshakeStatus.name());
				
				synchByteBuf(result, src, dst);

				switch (handshakeStatus) {
				case NEED_UNWRAP:
					return null;
				case NEED_WRAP:

					ReadFuture future = EmptyReadFuture.getEmptyReadFuture(context);

					ChannelWriteFuture f = new ChannelWriteFutureImpl(future, EmptyByteBuf.EMPTY_BYTEBUF);

					session.flush(f);

					return null;
				case NEED_TASK:
					runDelegatedTasks(sslEngine);
					continue;
				case FINISHED:
					session.finishHandshake(null);
					return null;
				case NOT_HANDSHAKING:

					release = false;

					dst.flip();

					return dst;

				default:
					throw new IllegalStateException("unknown handshake status: " + handshakeStatus);
				}
			}

		} finally {

			if (release) {
				ReleaseUtil.release(dst);
			}
		}
	}
	
	private void synchByteBuf(SSLEngineResult result,ByteBuf src,ByteBuf dst){
		
		int bytesConsumed = result.bytesConsumed();
		int bytesProduced = result.bytesProduced();
		
		if (bytesConsumed > 0) {
			src.skipBytes(bytesConsumed);
		}

		if (bytesProduced > 0) {
			dst.skipBytes(bytesProduced);
		}

//		logger.debug("_________________________bytesConsumed:{}" , bytesConsumed);
//		logger.debug("_________________________bytesProduced:{}" , bytesProduced);
		
	}
	

	private void runDelegatedTasks(SSLEngine engine) {

		for (;;) {

			Runnable task = engine.getDelegatedTask();

			if (task == null) {
				break;
			}

			task.run();
		}
	}
}
