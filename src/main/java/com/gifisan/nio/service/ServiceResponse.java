package com.gifisan.nio.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.FlushedException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.CatchWriteException;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.ProtocolEncoder;
import com.gifisan.nio.server.InnerResponse;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.session.InnerSession;
import com.gifisan.nio.server.session.NIOSession;

public class ServiceResponse implements InnerResponse {

	private int				dataLength		= 0;
	private ServerEndPoint		endPoint			= null;
	private boolean			flushed			= false;
	private BufferedOutputStream	textBuffer		= new BufferedOutputStream();
	private NIOSession			session			= null;
	private boolean			scheduled			= false;
	private ProtocolEncoder		encoder			= null;
	private byte				sessionID			= 0;
	private CatchWriteException	catchWriteException	= null;
	private ByteBuffer			buffer			= null;
	private InputStream			inputStream		= null;
	private int				writedLength		= 0;
	private ByteBuffer			streamBuffer		= null;
	private EndPointWriter		endPointWriter		= null;
	private WriterJob			writerJob			= null;

	public ServiceResponse(ServerEndPoint endPoint, NIOSession session) {
		this.endPoint = endPoint;
		this.session = session;
		this.sessionID = session.getSessionID();
		this.encoder = session.getServerContext().getProtocolEncoder();
		this.endPointWriter = session.getServerContext().getEndPointWriter();
	}

	public void flush() throws IOException {
		if (flushed) {
			throw new FlushedException("flushed already");
		}

		if (!endPoint.isOpened()) {
			throw new IOException("channel closed");
		}

		this.flushed = true;

		this.scheduled = true;

		this.buffer = encoder.encode(sessionID, textBuffer.toByteArray(), dataLength);

		this.buffer.flip();

		if (dataLength > 0) {

			this.writerJob = MULTI_WriterJob;
		
			this.endPointWriter.offer(this);
			
			return;
		}

		this.writerJob = TEXT_WriterJob;

		this.endPointWriter.offer(this);
		
	}

	public void setInputStream(InputStream inputStream) throws IOException {
		this.inputStream = inputStream;
		this.dataLength = inputStream.available();
		this.streamBuffer = ByteBuffer.allocate(1024 * 100);
		this.streamBuffer.position(this.streamBuffer.limit());
	}

	public void write(String content) {
		byte[] bytes = content.getBytes(Encoding.DEFAULT);
		textBuffer.write(bytes);
	}

	public boolean flushed() {
		return flushed;
	}

	public void schdule() {
		this.scheduled = true;
	}

	public boolean schduled() {
		return scheduled;
	}

	public void write(String content, Charset encoding) {
		byte[] bytes = content.getBytes(encoding);
		textBuffer.write(bytes);
	}

	public void write(byte b) throws IOException {
		textBuffer.write(b);
	}

	public void write(byte[] bytes) throws IOException {
		textBuffer.write(bytes);
	}

	public void write(byte[] bytes, int offset, int length) throws IOException {
		textBuffer.write(bytes, offset, length);
	}

	public EndPoint getEndPoint() {
		return endPoint;
	}

	public byte getSessionID() {
		return sessionID;
	}

	public boolean complete() {
		return writerJob.complete(this);
	}

	public void doWrite() throws IOException {
		this.writerJob.doWrite(this);
	}

	public boolean complete1() {
		return !buffer.hasRemaining() && writedLength == dataLength;
	}

	public void doWrite1() throws IOException {
		ByteBuffer buffer = this.buffer;
		EndPoint endPoint = this.endPoint;
		if (buffer.hasRemaining()) {
			endPoint.write(buffer);
			if (buffer.hasRemaining()) {
				return;
			}
		}

		if (writedLength < dataLength) {
			buffer = streamBuffer;
			if (buffer.hasRemaining()) {
				int length = endPoint.write(buffer);
				writedLength += length;
			} else {
				fill(inputStream, buffer);
				int length = endPoint.write(buffer);
				writedLength += length;
			}
		}
	}

	private void fill(InputStream inputStream, ByteBuffer buffer) throws IOException {
		byte[] array = buffer.array();
		int pos = 0;
		for (; pos < array.length;) {
			int n = inputStream.read(array, pos, array.length - pos);
			if (n <= 0)
				break;
			pos += n;
		}
		buffer.limit(pos);
		buffer.flip();
	}

	public void catchException(Request request, Response response, IOException exception) {
		// this.c
		// FIXME ........
		if (catchWriteException != null) {
			catchWriteException.catchException(request, response, exception);
		}
	}

	public void catchException(CatchWriteException catchWriteException) {
		this.catchWriteException = catchWriteException;

	}

	public InnerSession getInnerSession() {
		return session;
	}

	static interface WriterJob {

		boolean complete(ServiceResponse response);

		void doWrite(ServiceResponse response) throws IOException;
	}

	static class TextWriterJob implements WriterJob {

		public boolean complete(ServiceResponse response) {
			return !response.buffer.hasRemaining();
		}

		public void doWrite(ServiceResponse response) throws IOException {
			ByteBuffer buffer = response.buffer;
			EndPoint endPoint = response.endPoint;
			if (buffer.hasRemaining()) {
				endPoint.write(buffer);
				if (buffer.hasRemaining()) {
					return;
				}
			}
		}
	}

	static class MultiWriterJob implements WriterJob {

		public boolean complete(ServiceResponse response) {
			if(!response.buffer.hasRemaining() && response.writedLength == response.dataLength){
				CloseUtil.close(response.inputStream);
				return true;
			}
			return false;
		}

		public void doWrite(ServiceResponse response) throws IOException {
			ByteBuffer buffer = response.buffer;
			EndPoint endPoint = response.endPoint;
			if (buffer.hasRemaining()) {
				endPoint.write(buffer);
				if (buffer.hasRemaining()) {
					return;
				}
			}

			if (response.writedLength < response.dataLength) {
				buffer = response.streamBuffer;
				if (buffer.hasRemaining()) {
					int length = endPoint.write(buffer);
					response.writedLength += length;
				} else {
					fill(response.inputStream, buffer);
					int length = endPoint.write(buffer);
					response.writedLength += length;
				}
			}
		}

		private void fill(InputStream inputStream, ByteBuffer buffer) throws IOException {
			byte[] array = buffer.array();
			int pos = 0;
			for (; pos < array.length;) {
				int n = inputStream.read(array, pos, array.length - pos);
				if (n <= 0)
					break;
				pos += n;
			}
			buffer.limit(pos);
			buffer.flip();
		}
	}

	private static WriterJob	TEXT_WriterJob		= new TextWriterJob();
	private static WriterJob	MULTI_WriterJob		= new MultiWriterJob();
}
