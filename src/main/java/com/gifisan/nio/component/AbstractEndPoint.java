package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.NetworkException;
import com.gifisan.nio.server.NIOContext;

public abstract class AbstractEndPoint implements EndPoint {

	private Attachment			attachment	= null;
	private NIOContext			context		= null;
	private InetSocketAddress	local		= null;
	private InetSocketAddress	remote		= null;
	private Socket				socket		= null;
	private Long				endPointID	= null;
	private static AtomicLong	autoEndPointID = new AtomicLong(10000);
	protected boolean			endConnect	= false;
	

	public AbstractEndPoint(NIOContext context){
		this.context = context;
		this.endPointID = autoEndPointID.getAndIncrement();
	}

	public void attach(Attachment attachment) {
		this.attachment = attachment;
	}

	public Attachment attachment() {
		return attachment;
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

	public int getMaxIdleTime() throws SocketException {
		return socket.getSoTimeout();
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

	public boolean isEndConnect() {
		return endConnect;
	}

	public ByteBuffer read(int limit) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(limit);
		this.read(buffer);
		if (buffer.position() < limit) {
			throw new NetworkException("poor network ");
		}
		return buffer;
	}

	public NIOContext getContext() {
		return context;
	}

	public String toString() {
		return new StringBuilder("[edp(id:")
				.append(endPointID)
				.append(") remote /")
				.append(this.getRemoteHost())
				.append("(")
				.append(this.getRemoteAddr())
				.append("):")
				.append(this.getRemotePort())
				.append("]")
				.toString();
	}

	public Long getEndPointID() {
		return endPointID;
	}
}
