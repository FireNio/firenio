package com.generallycloud.nio.common.ssl;

import java.io.IOException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.v4.EmptyMemoryBlockV3;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.EmptyReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ReadFuture;

public class SslHandler {

	private BaseContext	context	= null;

//	private Logger		logger	= LoggerFactory.getLogger(SslHandler.class);

	public SslHandler(BaseContext context) {
		this.context = context;
	}

	private ByteBuf allocate(int capacity) {
		return context.getHeapByteBufferPool().allocate(capacity);
	}

	public ByteBuf wrap(SocketSession session,ByteBuf buf) throws IOException {
		
		SSLEngine engine = session.getSSLEngine();
		
		ByteBuf out = allocate(engine.getSession().getPacketBufferSize() * 2);

		try {

			for (;;) {

				SSLEngineResult result = engine.wrap(buf.getMemory(), out.getMemory());

				Status status = result.getStatus();
				HandshakeStatus handshakeStatus = result.getHandshakeStatus();

				int bytesConsumed = result.bytesConsumed();
				int bytesProduced = result.bytesProduced();

//				logger.info("_________________________wrap");
//				logger.info("_________________________bytesConsumed:" + bytesConsumed);
//				logger.info("_________________________bytesProduced:" + bytesProduced);
//				logger.info("_________________________" + status.name());
//				logger.info("_________________________" + handshakeStatus.name());

				if (bytesConsumed > 0) {
					buf.position(buf.position() + bytesConsumed);
				}

				if (bytesProduced > 0) {
					out.position(out.position() + bytesProduced);
				}

				if (status == Status.CLOSED) {
					return gc(out);
				} else {
					switch (handshakeStatus) {
					case NEED_UNWRAP:
						return gc(out);
					case NOT_HANDSHAKING:
						return gc(out);
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

			ReleaseUtil.release(out);

			if (e instanceof IOException) {

				throw (IOException) e;
			}

			throw new IOException(e);
		}
	}

	//FIXME 部分buf不需要gc
	private ByteBuf gc(ByteBuf out) throws IOException {

		out.flip();

		ByteBuf out2 = allocate(out.limit());

		try {

			out2.read(out.getMemory());

		} catch (IOException e) {

			ReleaseUtil.release(out2);

			throw e;
		}

		ReleaseUtil.release(out);

		out2.flip();

		return out2;
	}

	public ByteBuf unwrap(SocketSession session, ByteBuf packet) throws IOException {

//		logger.info("__________________________________________________start");

		ByteBuf buf = allocate(packet.capacity() * 2);

		boolean release = true;

		SSLEngine sslEngine = session.getSSLEngine();

		try {
			for (;;) {

				SSLEngineResult result = sslEngine.unwrap(packet.getMemory(), buf.getMemory());

				int bytesConsumed = result.bytesConsumed();
				int bytesProduced = result.bytesProduced();

//				Status status = result.getStatus();
				HandshakeStatus handshakeStatus = result.getHandshakeStatus();

//				logger.info("_________________________unwrap");
//				logger.info("_________________________bytesConsumed:" + bytesConsumed);
//				logger.info("_________________________bytesProduced:" + bytesProduced);
//				logger.info("_________________________" + status.name());
//				logger.info("_________________________" + handshakeStatus.name());

				if (bytesConsumed > 0) {
					packet.position(packet.position() + bytesConsumed);
				}

				if (bytesProduced > 0) {
					buf.position(buf.position() + bytesProduced);
				}

				switch (handshakeStatus) {
				case NEED_UNWRAP:
					return null;
				case NEED_WRAP:

					ReadFuture future = EmptyReadFuture.getEmptyReadFuture(context);

					IOWriteFuture f = new IOWriteFutureImpl(future, EmptyMemoryBlockV3.EMPTY_BYTEBUF);

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

					buf.flip();

					return buf;

				default:
					throw new IllegalStateException("unknown handshake status: " + handshakeStatus);
				}
			}

		} finally {

			if (release) {
				ReleaseUtil.release(buf);
			}
		}
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
