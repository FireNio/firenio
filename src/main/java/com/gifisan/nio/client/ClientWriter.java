package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public interface ClientWriter {

	public abstract void writeStream(ClientEndPoint endPoint, InputStream inputStream, int block) throws IOException;

	public abstract void writeText(ClientEndPoint endPoint, ByteBuffer buffer) throws IOException;

	public abstract void writeBeat(ClientEndPoint endPoint) throws IOException;

}