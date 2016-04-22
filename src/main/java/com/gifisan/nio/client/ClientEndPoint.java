package com.gifisan.nio.client;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.client.session.ClientSessionFactory;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.NIOEndPoint;
import com.gifisan.nio.component.EndPoint;

//FIXME 处理客户端网速慢的情况
public class ClientEndPoint extends NIOEndPoint implements EndPoint {

	public ClientEndPoint(SelectionKey selectionKey, ClientSessionFactory	clientSessionFactory) throws SocketException {
		super(selectionKey);
		this.clientSessionFactory = clientSessionFactory;
	}
	
	

	private ClientSessionFactory	clientSessionFactory	= null;
	private EndPointInputStream	inputStream			= null;
//	private ReentrantLock lock = new ReentrantLock();
//	private Condition writeWakeup = lock.newCondition();
//	private Condition readWakeup = lock.newCondition();
	

	public EndPointInputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(EndPointInputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void register(Selector selector, int option) throws ClosedChannelException {
		channel.register(selector, option);
	}

	public int sessionSize() {
		return clientSessionFactory.getSessionSize();
	}

	public int write(ByteBuffer buffer) throws IOException {
		channel.write(buffer);

		for (; buffer.hasRemaining();) {

			int length = channel.write(buffer);
			
			if (length == 0) {
//				ReentrantLock lock = this.lock;
//				
//				lock.lock();
//				
//				try {
//					writeWakeup.await();
//				} catch (InterruptedException e) {
//					DebugUtil.debug(e);
//					writeWakeup.signal();
//				}
//				
//				lock.unlock();
			}

		}
		return buffer.limit();
	}

	public int read(ByteBuffer buffer) throws IOException {
		channel.read(buffer);

		for (; buffer.hasRemaining();) {

			int length =  channel.read(buffer);
			
			if (length == 0) {
//				ReentrantLock lock = this.lock;
//				
//				lock.lock();
//				
//				try {
//					readWakeup.await();
//				} catch (InterruptedException e) {
//					DebugUtil.debug(e);
//					readWakeup.signal();
//				}
//				
//				lock.unlock();
			}

		}
		return buffer.limit();
	}

	public void write(byte[] beat) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(beat);
		this.write(buffer);
	}
	
//	public void readWakeup(){
//		
//		ReentrantLock lock = this.lock;
//		
//		lock.lock();
//		
//		readWakeup.signal();
//		
//		lock.unlock();
//	}
}
