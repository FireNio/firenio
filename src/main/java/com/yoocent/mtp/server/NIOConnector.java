package com.yoocent.mtp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

import com.yoocent.mtp.AbstractLifeCycle;
import com.yoocent.mtp.common.LifeCycleUtil;
import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.server.selector.SelectorManagerTask;

public final class NIOConnector extends AbstractLifeCycle implements Connector{

	private int port = 8600;
	
	private int localPort = 0;
	
	private MTPServer server = null;
	
	private ServerSocketChannel serverSocketChannel = null;
	
	private ServerSocket serverSocket = null;
	
	private SelectorManagerTask selectorManagerTask = new SelectorManagerTask();
	
	private String host = "127.0.0.1";
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getLocalPort() {
		return this.localPort;
	}

	private InetSocketAddress getInetSocketAddress(){
		if (StringUtil.isBlankOrNull(host)) {
			return new InetSocketAddress(this.port);
		}
		return new InetSocketAddress(this.host,this.port);
	}
	
	public void open() throws IOException {
		synchronized (this) {
			// 打开服务器套接字通道
			serverSocketChannel = ServerSocketChannel.open();
			// 服务器配置为非阻塞
			serverSocketChannel.configureBlocking(false);
			// 检索与此通道关联的服务器套接字
			serverSocket = serverSocketChannel.socket();
			localPort = serverSocket.getLocalPort();
			// 进行服务的绑定
			serverSocket.bind(getInetSocketAddress());
			
		}
	}

	public void setServer(MTPServer server) {
		this.server = server;
	}

	public void close() throws IOException {
		 if (serverSocketChannel.isOpen()){
			 serverSocketChannel.close();
		 }
	}

	public int getPort() {
		return this.port;
	}

	public MTPServer getServer() {
		return this.server;
	}

	protected void doStart() throws Exception {
		if (this.server == null){
            throw new IllegalStateException("No server");
		}
        this.open();

        this.selectorManagerTask.register(serverSocketChannel);
        
        this.selectorManagerTask.start();
        

	}

	protected void doStop() throws Exception {
		
		LifeCycleUtil.stop(selectorManagerTask);
		
		this.close();
		
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
