package com.gifisan.nio.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.WriterOverflowException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.IOWriteFuture;
import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListM2O;

public class ClientEndPointWriter extends AbstractLifeCycle implements EndPointWriter {

	private Thread					owner		= null;
	private boolean				running		= false;
	private ClientTCPEndPoint			endPoint		= null;
	private ReentrantLock			lock			= new ReentrantLock();
	private Condition				networkWeak	= lock.newCondition();
	private LinkedList<IOWriteFuture>	writers		= new LinkedListM2O<IOWriteFuture>(1024 * 8 * 16);
	
	public void collect() {

		ReentrantLock lock = this.lock;

		lock.lock();

		networkWeak.signal();

		lock.unlock();

	}

	public void offer(IOWriteFuture future) {

		if (!this.writers.offer(future)) {

			future.onException(WriterOverflowException.INSTANCE);
		}
	}

	public void run() {

		for (; running;) {

			IOWriteFuture writer = writers.poll(16);

			if (writer == null) {
				
				if (endPoint.isEndConnect()) {
					CloseUtil.close(endPoint);
				}

				continue;
			}

			TCPEndPoint endPoint = writer.getEndPoint();
			
			if (endPoint.isEndConnect()) {
				
				if (endPoint.isOpened()) {
					
					CloseUtil.close(endPoint);
				}
				
				writer.onException(DisconnectException.INSTANCE);
				
				continue;
			}

			try {

				for (;;) {

					if (writer.write()) {

						endPoint.decrementWriter();

						writer.onSuccess();
						
						break;

					} else {
						
						waitWrite(writer);
					}
				}
			} catch (IOException e) {
				DebugUtil.debug(e);

				writer.onException(e);

			} catch (Exception e) {
				DebugUtil.debug(e);

				writer.onException(new IOException(e));
			}
		}
	}
	
	private void waitWrite(IOWriteFuture writer){
		
		for (;;) {

			if (writer.isNetworkWeak()) {

				ReentrantLock lock = this.lock;

				lock.lock();

				try {
					networkWeak.await(16, TimeUnit.MILLISECONDS);
				} catch (Exception e) {
					DebugUtil.debug(e);
					networkWeak.signal();
				}

				lock.unlock();

				if (endPoint.isNetworkWeak()) {

					continue;
				}
				break;
			}
		}
	}
		
	public void doStart() throws Exception {
		this.running = true;
		this.owner = new Thread(this, "Client-EndPoint-Writer");
		this.owner.start();

	}
	
	protected void setEndPoint(ClientTCPEndPoint endPoint) {
		this.endPoint = endPoint;
	}

	public void doStop() throws Exception {
		running = false;
	}
}
