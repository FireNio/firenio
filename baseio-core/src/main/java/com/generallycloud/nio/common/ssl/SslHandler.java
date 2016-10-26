package com.generallycloud.nio.common.ssl;

import java.io.IOException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.EmptyMemoryBlockV3;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.EmptyReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ReadFuture;

public class SslHandler {

	private BaseContext	context	= null;

	private Logger		logger	= LoggerFactory.getLogger(SslHandler.class);

	public SslHandler(BaseContext context) {
		this.context = context;
	}

	private ByteBuf allocate(int capacity) {
		return context.getHeapByteBufferPool().allocate(capacity);
	}

	public ByteBuf wrap(SSLEngine engine, ByteBuf buf) throws IOException {

		ByteBuf out;

		if (buf.capacity() == 0) {

			out = allocate(102400);
		} else {

			out = allocate(buf.limit() * 100);
		}

		try {

			for (;;) {

				SSLEngineResult result = engine.wrap(buf.getMemory(), out.getMemory());
				
				Status status = result.getStatus();
				HandshakeStatus handshakeStatus = result.getHandshakeStatus();
				
				int bytesConsumed = result.bytesConsumed();
				int bytesProduced = result.bytesProduced();
				
				logger.info("_________________________wrap");
				logger.info("_________________________bytesConsumed:" + bytesConsumed);
				logger.info("_________________________bytesProduced:" + bytesProduced);
				logger.info("_________________________" + status.name());
				logger.info("_________________________" + handshakeStatus.name());
				
				if (bytesConsumed > 0) {
					buf.position(buf.position() + bytesConsumed);
				}
				
				if (bytesProduced > 0) {
					out.position(out.position() + bytesProduced);
				}

				if (status == Status.CLOSED) {
					return null;
				} else {

					switch (handshakeStatus) {
					case NEED_TASK:
						runDelegatedTasks(engine);
						break;
					case FINISHED:
						// deliberate fall-through
					case NOT_HANDSHAKING:
						break;
					case NEED_WRAP:
						break;
					case NEED_UNWRAP:
						break;
					default:
						throw new IllegalStateException("Unknown handshake status: "
								+ result.getHandshakeStatus());
					}

					if (bytesProduced == 0) {

						out.flip();

						ByteBuf out2 = allocate(out.limit());

						out2.read(out.getMemory());

						ReleaseUtil.release(out);

						out2.flip();

						return out2;
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

	public ByteBuf unwrap(IOSession session, ByteBuf packet) throws IOException {

		logger.info("__________________________________________________start");

		ByteBuf buf = allocate(packet.capacity() * 2);

		boolean release = true;

		SSLEngine sslEngine = session.getSSLEngine();

		boolean notifyClosure = false;

		try {
			for (;;) {

				SSLEngineResult result = sslEngine.unwrap(packet.getMemory(), buf.getMemory());
				
				int bytesConsumed = result.bytesConsumed();
				int bytesProduced = result.bytesProduced();

				Status status = result.getStatus();
				HandshakeStatus handshakeStatus = result.getHandshakeStatus();

				logger.info("_________________________unwrap");
				logger.info("_________________________bytesConsumed:" + bytesConsumed);
				logger.info("_________________________bytesProduced:" + bytesProduced);
				logger.info("_________________________" + status.name());
				logger.info("_________________________" + handshakeStatus.name());
				
				if (bytesConsumed > 0) {
					packet.position(packet.position() + bytesConsumed);
				}
				
				if (bytesProduced > 0) {
					buf.position(buf.position() + bytesProduced);
				}


				switch (status) {
				case BUFFER_OVERFLOW:
					logger.error("buffer overflow");
					return null;
				case CLOSED:
					notifyClosure = true;
					break;
				default:
					break;
				}

				switch (handshakeStatus) {
				case NEED_UNWRAP:
					return null;
				case NEED_WRAP:

					// wrapNonAppData(session, sslEngine);
					ReadFuture future = EmptyReadFuture.getEmptyReadFuture(context);

					IOWriteFuture f = new IOWriteFutureImpl(future, EmptyMemoryBlockV3.EMPTY_BYTEBUF);

					session.flush(f);

					return null;
				case NEED_TASK:
					runDelegatedTasks(sslEngine);
					break;
				case FINISHED:
					return null;
				case NOT_HANDSHAKING:

					if (bytesProduced > 0) {

						release = false;

						buf.flip();

						return buf;
					}

					return null;
				default:
					throw new IllegalStateException("unknown handshake status: " + handshakeStatus);
				}

				if (notifyClosure) {
					sslEngine.closeInbound();
					sslEngine.closeOutbound();
					CloseUtil.close(session);
				}
				
				if (result.bytesConsumed() ==0 && handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
					return null;
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
