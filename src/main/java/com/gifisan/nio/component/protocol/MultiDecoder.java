package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOReadFuture;
import com.gifisan.nio.component.MultiReadFuture;
import com.gifisan.nio.component.OutputStreamAcceptor;

public class MultiDecoder extends AbstractDecoder {

	public MultiDecoder(Charset charset) {
		super(charset);
	}
	
	public IOReadFuture decode(EndPoint endPoint, byte[] header) throws IOException {
		
		byte sessionID = gainSessionID(header);
		
		int textLength = gainTextLength(header);
		
		int dataLength = gainStreamLength(header);

		AbstractSession session = (AbstractSession) endPoint.getSession(sessionID);
		
		ByteBuffer textBuffer = ByteBuffer.allocate(textLength);
		
		String serviceName = gainServiceName(endPoint, header);
		
		MultiReadFuture future = new MultiReadFuture(textBuffer, session, serviceName,dataLength);
		
		OutputStreamAcceptor outputStreamAcceptor = session.getOutputStreamAcceptor();
		
		try {
			outputStreamAcceptor.accept(session, future);
		} catch (Exception e) {
			DebugUtil.debug(e);
		}
		
		if (!future.hasOutputStream()) {
			endPoint.endConnect();
		}
		
		return future;
	}
}
