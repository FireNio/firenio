package com.gifisan.nio.component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.ServerEndpointFactory;
import com.gifisan.nio.server.session.InnerSession;
import com.gifisan.nio.server.session.NIOSession;

public class ServerNIOEndPoint extends AbstractEndPoint implements ServerEndPoint {

	private Attachment			attachment	= null;
	private ServerContext		context		= null;
	private long				endPointID	= 0;
	private ServerEndpointFactory	factory		= null;
	private int				mark			= 0;
	private SelectionKey		selectionKey	= null;
	private InnerSession[]		sessions		= new InnerSession[4];
	private int				sessionSize	= 0;

	public ServerNIOEndPoint(ServerContext context, SelectionKey selectionKey, long endPointID) throws SocketException {
		super(selectionKey);
		this.context = context;
		this.factory = context.getServerEndpointFactory();
		this.endPointID = endPointID;
		this.selectionKey = selectionKey;

	}

	public void attach(Attachment attachment) {
		this.attachment = attachment;
	}

	public Attachment attachment() {
		return attachment;
	}

	public void close() throws IOException {
		this.selectionKey.attach(null);

		for (InnerSession session : sessions) {
			if (session == null) {
				continue;
			}
			session.destroyImmediately();
		}

		this.factory.remove(this);

		super.close();
	}

	public ServerContext getContext() {
		return context;
	}

	public long getEndPointID() {
		return endPointID;
	}

	public int getMark() {
		return mark;
	}

	public InnerSession getSession(byte sessionID) {

		InnerSession session = sessions[sessionID];

		if (session == null) {
			session = new NIOSession(this, sessionID);
			sessions[sessionID] = session;
			sessionSize = sessionID;
		}

		return session;
	}

	public void removeSession(byte sessionID) {
		InnerSession session = sessions[sessionID];

		sessions[sessionID] = null;
		if (session != null) {
			session.destroyImmediately();
		}
	}

	public int sessionSize() {
		return sessionSize;
	}

	public void setMark(int mark) {
		this.mark = mark;
	}

	public boolean flushServerOutputStream(ByteBuffer buffer) throws IOException {
		InnerSession session = this.getCurrentSession();

		OutputStream outputStream = session.getServerOutputStream();
		
		if (outputStream == null) {
			throw new IOException("why did you not close this endpoint and did not handle it when a stream in.");
		}

		buffer.clear();

		int length = read(buffer);

		outputStream.write(buffer.array(), 0, length);
		
		readed += length;

		return readed == streamAvailable;
	}

	private int	streamAvailable	= 0;
	private int	readed			= 0;

	public void setStreamAvailable(int streamAvailable) {
		this.streamAvailable = streamAvailable;

	}

	public boolean inStream() {
		return readed < streamAvailable;
	}

	public void resetServerOutputStream() {
		this.readed = 0;
		this.streamAvailable = 0;
	}
	
	
}
