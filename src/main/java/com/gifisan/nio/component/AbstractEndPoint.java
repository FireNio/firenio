package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

//TODO 单connection 多session时 response write 使用队列
public abstract class AbstractEndPoint implements EndPoint {

	private SocketChannel		channel			= null;
	private InputStream			inputStream		= null;
	private InetSocketAddress	local			= null;
	private int				maxIdleTime		= 0;
	private InetSocketAddress	remote			= null;
	private Socket				socket			= null;
	private SlowlyNetworkReader 	accept 			= null;
	private boolean			endConnect		= false;
	
	public void setSchedule(SlowlyNetworkReader accept) {
		this.accept = accept;
	}
	
	public SlowlyNetworkReader getSchedule() {
		return accept;
	}
	
	public AbstractEndPoint(SelectionKey selectionKey) throws SocketException {
		this.channel = (SocketChannel) selectionKey.channel();
		socket = channel.socket();
		if (socket == null) {
			throw new SocketException("socket is empty");
		}
		maxIdleTime = socket.getSoTimeout();
	}

	// TODO 处理网速较慢的时候
	public void completedRead(ByteBuffer buffer) throws NIOException{
		
		int limit = buffer.limit();
		
		SocketChannel channel = this.channel;

		try {

			int _length = channel.read(buffer);
			
			int length = _length;

			long _last = 0;
			
			boolean _slowly = false;
			
			for (; length < limit;) {
				
				if (_length < 0) {
					throw new NIOException("bad network");
				}
				
				if (_length == 0) {
					
					long _past = System.currentTimeMillis() - _last;
					
					if (_past > 160000) {
						
						if (_slowly) {

//							throw new NIOException("network is weak");
							System.out.println("network is weak");
							
						}else{
							
							_last = System.currentTimeMillis();
							
							_slowly = true;
						}
					}
				}else{
					
					_slowly = false;
					
					_last = System.currentTimeMillis();
				}
				
				_length = channel.read(buffer);
				
				length += _length;
			}
		} catch (IOException e) {
			throw handleException(e);
		}
	}

	// TODO 处理网速较慢的时候
	public void completedWrite(ByteBuffer buffer) throws NIOException {
		
		int limit = buffer.limit();

		try {
			
			int _length = channel.write(buffer);
			
			int length = _length;
			
			long _last = 0;
			
			boolean _slowly = false;

			for (; length < limit;) {
				
				if (_length < 0) {
					throw new NIOException("bad network");
				}
				
				if (_length == 0) {
					
					long _past = System.currentTimeMillis() - _last;
					
					if (_past > 160000) {
						
						if (_slowly) {
							
//							throw new NIOException("network is weak");
							System.out.println("network is weak");
							
						}else{
							
							_last = System.currentTimeMillis();
							
							_slowly = true;
						}
					}
				}else{
					
					_slowly = false;
					
					_last = System.currentTimeMillis();
				}
				
				_length = channel.write(buffer);
				
				length += _length;
			}

		} catch (IOException e) {
			throw handleException(e);
		}
	}
	
	public boolean isEndConnect() {
		return endConnect;
	}
	
	public void endConnect() {
		this.endConnect = true;
	}

	public String getLocalAddr() {
		if (local == null) {
			local = (InetSocketAddress) socket.getLocalSocketAddress();
		}
		return local.getAddress().getCanonicalHostName();
	}

	public String getLocalHost() {
		return local.getHostName();
	}

	public int getLocalPort() {
		return local.getPort();
	}


	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	public String getRemoteAddr() {
		if (remote == null) {
			remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		}
		return remote.getAddress().getCanonicalHostName();
	}

	public String getRemoteHost() {
		if (remote == null) {
			remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		}
		return remote.getAddress().getHostName();
	}

	public int getRemotePort() {
		if (remote == null) {
			remote = (InetSocketAddress) socket.getRemoteSocketAddress();
		}
		return remote.getPort();
	}


	public abstract NIOException handleException(IOException exception) throws NIOException ;

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public boolean isOpened() {
		return this.channel.isOpen();
	}

	public int read(ByteBuffer buffer) throws IOException {
		try {
			return this.channel.read(buffer);
		} catch (IOException e) {
			throw handleException(e);
		}
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public int write(byte b) throws NIOException {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		buffer.put(b);
		return write(buffer);
	}

	public int write(byte[] bytes) throws NIOException {
		return write(ByteBuffer.wrap(bytes));
	}

	public int write(byte[] bytes, int offset, int length) throws NIOException {
		return write(ByteBuffer.wrap(bytes, offset, length));
	}

	public int write(ByteBuffer buffer) throws NIOException {
		try {
			return channel.write(buffer);
		} catch (IOException e) {
			throw handleException(e);
		}
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public ByteBuffer completedRead(int limit) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(limit);

		completedRead(buffer);
		
		return buffer;
	}

	public boolean inStream() {
		return inputStream != null && !inputStream.complete();
	}

	
	public void close() throws IOException {
		this.channel.close();
	}
	
}
