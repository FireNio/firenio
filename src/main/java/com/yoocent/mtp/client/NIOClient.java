package com.yoocent.mtp.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.yoocent.mtp.common.ByteBufferUtil;
import com.yoocent.mtp.common.CloseUtil;
import com.yoocent.mtp.common.StringUtil;

public class NIOClient implements Closeable{

	private String host = null;
	
	private int port = 0;
	
	private boolean closed = true;
	
	private Selector selector = null;
	
	private InetSocketAddress serverAddress = null;
	
	private String sessionID = null;
	
	private SocketChannel socketChannel = null;

	public NIOClient(String host, int port,String sessionID) {
		this.host = host;
		this.port = port;
		this.sessionID = sessionID;
	}
	
	
	public void close() throws IOException{
		if (closed) {
			return;
		}
		selector.select();
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectionKeys.iterator();
		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();
			iterator.remove();
			if (selectionKey.isWritable() || selectionKey.isWritable()) {
				SocketChannel client = (SocketChannel)selectionKey.channel();
				CloseUtil.close(client);
				selectionKey.cancel();
				selector.close();
				closed = true;
			}
		}
	}
	
	public void connect() throws IOException{
		if (closed) {
			this.serverAddress = new InetSocketAddress(host, port);
			this.socketChannel = SocketChannel.open();
			this.socketChannel.configureBlocking(false);
			this.selector = Selector.open();
			this.socketChannel.register(selector, SelectionKey.OP_CONNECT);
			this.socketChannel.connect(serverAddress);
			this.closed = false;
		}
	}
	
	
	private ByteBuffer getBuffer(String sessionID,String serviceKey,String paramsJSON,int avaiable) throws IOException{
		byte [] keyBytes = serviceKey.getBytes("UTF-8");
		if (keyBytes.length > Byte.MAX_VALUE) {
			throw new IOException("key is too long");
		}
		return ByteBufferUtil.getByteBuffer(sessionID, serviceKey, paramsJSON, avaiable);
		
	}
	
	private void sendCommand(String serviceKey,String paramsJSON,long timeout) throws IOException{
		selector.select();
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectionKeys.iterator();
		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();
			iterator.remove();
			SocketChannel client = (SocketChannel) selectionKey.channel();
			if(selectionKey.isConnectable()){
				if (client.isConnectionPending()) {
					client.finishConnect();
				}
			}
			ByteBuffer buffer = getBuffer(sessionID,serviceKey, paramsJSON,0);
			buffer.flip();
			write(client, buffer);
			socketChannel.register(selector, SelectionKey.OP_READ);
		}
		
	}
	
	private String acceptString(SocketChannel client,int length) throws IOException{
		ByteBuffer buffer = read(client, length);
		return new String(buffer.array(),"UTF-8");
	}
	

	private int getLength(byte [] header){
		int v0 = (header[1] & 0xff);  
	    int v1 = (header[2] & 0xff) << 8;  
	    int v2 = (header[3] & 0xff) << 16;  
	    int v3 = (header[4] & 0xff) << 24; 
	    return v0 | v1 | v2 | v3;
	}
	
	private Response acceptResponse(SelectionKey selectionKey,long timeout) throws IOException{
		if (selectionKey.isReadable()) {
			Response response = null;
			SocketChannel client = (SocketChannel) selectionKey.channel();
			ByteBuffer buffer = ByteBuffer.allocate(5);
			client.read(buffer);
			byte [] header = buffer.array();
			
			byte type = header[0];
			int length = getLength(header);
			
			if (type == Response.TEXT) {
				String content = acceptString(client, length);
				response = new Response(content,type);
			}else if (type == Response.ERROR) {
				String content = acceptString(client, length);
				response = new Response(content,type);
			} else if(type == Response.STREAM){
				ClientInputStream inputStream = new ClientInputStream(socketChannel, length);
				response = new Response(inputStream);
			} else{
				throw new IOException("unknow header: "+type);
			}
			client.register(selector, SelectionKey.OP_WRITE);
			return response;
		}
		return null;
	}
	
	private Response acceptResponse(long timeout) throws IOException{
		Response response = null;
		boolean finished = false;
		selector.select(timeout);
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectionKeys.iterator();
		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();
			response = acceptResponse(selectionKey, timeout);
			if (response != null) {
				finished = true;
			}
		}
		if (finished) {
			return response;
		}
		throw new IOException("read time out");
	}
	
	
	public Response request(String serviceKey,String paramsJSON,long timeout) throws IOException{
		if (StringUtil.isBlankOrNull(serviceKey)) {
			return null;
		}
		this.sendCommand(serviceKey, paramsJSON, timeout);
		
		return acceptResponse(timeout);
	}
	
	private int write(SocketChannel client,ByteBuffer buffer) throws IOException{
		int length = client.write(buffer);
		while(length == 0){
			length = client.write(buffer);
		}
		return length;
	}
	
	private ByteBuffer read(SocketChannel client,int length) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocate(length);
		int _length = client.read(buffer);
		while (_length < length) {
			int __length = client.read(buffer);
			_length += __length;
		}
		return buffer;
	}
	
	private int BLOCK = 102400;
	
	private void sendCommand(String serviceKey,String paramsJSON,InputStream inputStream,long timeout) throws IOException{
		selector.select();
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectionKeys.iterator();
		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();
			iterator.remove();
			SocketChannel client = (SocketChannel) selectionKey.channel();
			if(selectionKey.isConnectable()){
				if (client.isConnectionPending()) {
					client.finishConnect();
				}
			}
			int avaiable = inputStream.available();
			ByteBuffer buffer = getBuffer(sessionID,serviceKey, paramsJSON,avaiable);
			buffer.flip();
			write(client, buffer);
			byte [] bytes = new byte[BLOCK];
			int length = inputStream.read(bytes);
			while(length == BLOCK){
				buffer = ByteBuffer.wrap(bytes);
				write(client, buffer);
				length = inputStream.read(bytes);
			}
			if (length > 0) {
				buffer = ByteBuffer.wrap(bytes, 0, length);
				write(client, buffer);
			}
			socketChannel.register(selector, SelectionKey.OP_READ);
		}
	}
	
	public Response request(String serviceKey,String paramsJSON,InputStream inputStream,long timeout) throws IOException{
		if (StringUtil.isBlankOrNull(serviceKey)) {
			return null;
		}
		this.sendCommand(serviceKey, paramsJSON, inputStream, timeout);
		
		return acceptResponse(timeout);
	}
	
}
