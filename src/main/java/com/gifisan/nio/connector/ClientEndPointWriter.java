package com.gifisan.nio.connector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.WriterOverflowException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.concurrent.LinkedList;
import com.gifisan.nio.component.concurrent.LinkedListM2O;
import com.gifisan.nio.component.future.IOWriteFuture;

public class ClientEndPointWriter implements EndPointWriter {

	private ClientTCPEndPoint		endPoint		;
	private ReentrantLock			lock			= new ReentrantLock();
	private Condition				networkWeak	= lock.newCondition();
	private LinkedList<IOWriteFuture>	writers		= new LinkedListM2O<IOWriteFuture>(1024 * 64);
	private Logger					logger		= LoggerFactory.getLogger(ClientEndPointWriter.class);

	public void collect() {

	}

	public void offer(IOWriteFuture future) {

		if (!this.writers.offer(future)) {

			future.onException(WriterOverflowException.INSTANCE);
		}
	}

	public void loop() {

		IOWriteFuture writer = writers.poll(16);

		if (writer == null) {

			if (endPoint.isEndConnect()) {
				CloseUtil.close(endPoint);
			}

			return;
		}

		TCPEndPoint endPoint = writer.getEndPoint();

		if (endPoint.isEndConnect()) {

			if (endPoint.isOpened()) {

				CloseUtil.close(endPoint);
			}

			writer.onException(DisconnectException.INSTANCE);

			return;
		}

		try {

			loopWrite(writer);

		} catch (IOException e) {
			logger.debug(e);

			writer.onException(e);

		} catch (Exception e) {
			logger.debug(e);

			writer.onException(new IOException(e));
		}
	}

	private void loopWrite(IOWriteFuture writer) throws IOException {

		for (;;) {

			if (writer.write()) {

				endPoint.decrementWriter();

				writer.onSuccess();

				break;

			} else {

				waitWrite(writer, endPoint);
			}
		}
	}

	private void waitWrite(IOWriteFuture writer, TCPEndPoint endPoint) {

		if (!writer.isNetworkWeak()) {
			return;
		}

		for (;;) {

			ReentrantLock lock = this.lock;

			lock.lock();

			try {
				networkWeak.await(1, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				logger.debug(e);
				networkWeak.signal();
			}

			lock.unlock();

			if (endPoint.isNetworkWeak()) {

				continue;
			}
			break;
		}
	}
	
	public void stop() {
		
	}
	
	public String toString() {
		return "Client-EndPoint-Writer";
	}

	protected void setEndPoint(ClientTCPEndPoint endPoint) {
		this.endPoint = endPoint;
	}

}
