package com.generallycloud.nio.common.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import com.generallycloud.nio.buffer.ByteBuf;
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

	private void wrapNonAppData(IOSession session, SSLEngine engine) throws IOException {

		ByteBuffer buffer = ByteBuf.EMPTY_BUFFER;

		ByteBuf buf = allocate(102400);

		try {
			for (;;) {

				SSLEngineResult result = engine.wrap(buffer, buf.getMemory());

				Status status = result.getStatus();
				HandshakeStatus handshakeStatus = result.getHandshakeStatus();

				logger.info("_________________________wrapNonAppData");
				logger.info("_________________________" + status.name());
				logger.info("_________________________" + handshakeStatus.name());

				switch (handshakeStatus) {
				case NEED_UNWRAP:
					break;
				case NEED_WRAP:
					break;
				case NEED_TASK:
					runDelegatedTasks(engine);
					break;
				case FINISHED:
					break;
				case NOT_HANDSHAKING:
					break;
				default:

				}

				int bytesProduced = result.bytesProduced();

				if (bytesProduced == 0) {
					break;
				} else {

					buf.position(buf.position() + bytesProduced);
				}
			}
		} catch (Throwable e) {
			
			ReleaseUtil.release(buf);
			
			if (e instanceof IOException) {
				
				throw (IOException)e;
			}
			
			throw new IOException(e);
		}

		buf.flip();

		ReadFuture future = EmptyReadFuture.getEmptyReadFuture(context);

		IOWriteFuture f = new IOWriteFutureImpl(future, buf, false);

		session.flush(f);
	}

	private ByteBuf allocate(int capacity) {
		return context.getHeapByteBufferPool().allocate(capacity);
	}

	public ByteBuf wrap(SSLEngine engine, ByteBuf buf) throws IOException {
		ByteBuf out = allocate(buf.limit() * 100);
		try {
			for (;;) {

				SSLEngineResult result = engine.wrap(buf.getMemory(), out.getMemory());

				if (result.getStatus() == Status.CLOSED) {
					// SSLEngine has been closed already.
					// Any further write attempts should be denied.
					// pendingUnencryptedWrites.removeAndFailAll(SSLENGINE_CLOSED);
					return null;
				} else {

					switch (result.getHandshakeStatus()) {
					case NEED_TASK:
						runDelegatedTasks(engine);
						break;
					case FINISHED:
						// deliberate fall-through
					case NOT_HANDSHAKING:

						int capacity = result.bytesProduced();

						out.position(out.position() + capacity);

						out.flip();

						ByteBuf out2 = allocate(capacity);

						out2.read(out.getMemory());

						ReleaseUtil.release(out);

						out2.flip();

						return out2;

					case NEED_WRAP:
						break;
					case NEED_UNWRAP:
						return null;
					default:
						throw new IllegalStateException("Unknown handshake status: "
								+ result.getHandshakeStatus());
					}
				}
			}
		} catch (Throwable e) {
			
			ReleaseUtil.release(out);
			
			if (e instanceof IOException) {
				
				throw (IOException)e;
			}
			
			throw new IOException(e);
		}
	}

	public ByteBuf unwrap(IOSession session, ByteBuffer packet) throws IOException {

		logger.info("__________________________________________________start");

		int length = SslUtils.getEncryptedPacketLength(packet.array(), packet.position());

		ByteBuf buf = allocate(length);

		boolean release = true;

		SSLEngine sslEngine = session.getSSLEngine();

		boolean notifyClosure = false;

		try {
			for (;;) {

				SSLEngineResult result = sslEngine.unwrap(packet, buf.getMemory());

				int bytesConsumed = result.bytesConsumed();
				int bytesProduced = result.bytesProduced();

				Status status = result.getStatus();
				HandshakeStatus handshakeStatus = result.getHandshakeStatus();

				logger.info("_________________________unwrap");
				logger.info("_________________________" + status.name());
				logger.info("_________________________" + handshakeStatus.name());

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
					break;
				case NEED_WRAP:

					wrapNonAppData(session, sslEngine);

					return null;
				case NEED_TASK:
					runDelegatedTasks(sslEngine);
					break;
				case FINISHED:
					return null;
				case NOT_HANDSHAKING:

					if (bytesProduced > 0) {

						release = false;

						buf.position(buf.position() + bytesProduced);

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

				if (bytesConsumed == 0) {
					break;
				}
			}

			return null;

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
