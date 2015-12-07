package com.yoocent.mtp.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.yoocent.mtp.server.InnerEndPoint;

public class NIOConnectorEndPoint extends EndPointImpl implements InnerEndPoint{
	
	private MTPRequestInputStream inputStream = null;

	private boolean inSchedule = false;
	
	private MTPParser parser = null;
	
	private int comment = 0;
	
	
	public NIOConnectorEndPoint(SelectionKey selectionKey, SocketChannel channel) throws SocketException {
		super(selectionKey, channel);
	}

	public MTPParser genParser(){
		this.parser = new MTPParser(this);
		return parser;
	}
	
	public MTPRequestInputStream getInputStream() {
		return inputStream;
	}
	
	public MTPParser getParser() {
		return parser;
	}
	
	public boolean inSchedule() {
		return inSchedule;
	}

	public boolean inStream() {
		if (inputStream == null) {
			return false;
		}
		return !inputStream.complete();
	}
	

	public byte[] readHead() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(12);
		int length = this.read(buffer);
		
		if (length != 12) {
			//如果一次读取不到12个byte
			//这样的连接持续下去也是无法进行业务操作
			//还有一种情况是有人在恶意攻击服务器
			this.endConnect();
			return null;
		}else{
			byte[] header = buffer.array();
			return header;
		}
		
//		if (length < 1) {
//			this.endConnect = true;
//			return null;
//		}else{
//			while(length < 12){
//				int _length = this.read(buffer);
//				length += _length;
//			}
//			byte[] header = buffer.array();
//			return header;
//		}
		
	}
	

	public void setMTPRequestInputStream(MTPRequestInputStream inputStream) {
		this.inputStream = inputStream;
	}

	public int comment() {
		return this.comment;
	}

	public void setComment(int comment) {
		this.comment = comment;
		
	}

	
	private Object attachment = null;
	
	public Object attachment() {
		return attachment;
	}

	
	public void attach(Object attachment) {
		
		this.attachment = attachment;
	}
	
}
