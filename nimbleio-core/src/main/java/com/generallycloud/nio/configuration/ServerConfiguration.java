package com.generallycloud.nio.configuration;

import java.nio.charset.Charset;

import com.generallycloud.nio.Encoding;

//FIXME 校验参数
public class ServerConfiguration {

	private int		SERVER_TCP_PORT;
	private int		SERVER_UDP_PORT;
	private String		SERVER_HOST				= "localhost";
	private int		SERVER_CORE_SIZE			= Runtime.getRuntime().availableProcessors();
	private Charset	SERVER_ENCODING			= Encoding.UTF8;
	private boolean	SERVER_DEBUG				= false;
	private int		SERVER_CHANNEL_QUEUE_SIZE	= 1024 * 512;
	private long		SERVER_SESSION_IDLE_TIME		= 30 * 1000;
	private int		SERVER_MEMORY_POOL_UNIT		= 1024;
	private int		SERVER_MEMORY_POOL_CAPACITY	= 1024;

	public int getSERVER_CHANNEL_QUEUE_SIZE() {
		return SERVER_CHANNEL_QUEUE_SIZE;
	}

	public void setSERVER_CHANNEL_QUEUE_SIZE(int SERVER_CHANNEL_QUEUE_SIZE) {
		if (SERVER_CHANNEL_QUEUE_SIZE == 0) {
			return;
		}
		this.SERVER_CHANNEL_QUEUE_SIZE = SERVER_CHANNEL_QUEUE_SIZE;
	}

	public int getSERVER_TCP_PORT() {
		return SERVER_TCP_PORT;
	}

	public void setSERVER_TCP_PORT(int SERVER_TCP_PORT) {
		if (SERVER_TCP_PORT == 0) {
			return;
		}
		this.SERVER_TCP_PORT = SERVER_TCP_PORT;
	}

	public int getSERVER_UDP_PORT() {
		return SERVER_UDP_PORT;
	}

	public void setSERVER_UDP_PORT(int SERVER_UDP_PORT) {
		if (SERVER_UDP_PORT == 0) {
			return;
		}
		this.SERVER_UDP_PORT = SERVER_UDP_PORT;
	}

	public int getSERVER_CORE_SIZE() {
		return SERVER_CORE_SIZE;
	}

	public void setSERVER_CORE_SIZE(int SERVER_CORE_SIZE) {
		if (SERVER_CORE_SIZE == 0) {
			return;
		}
		this.SERVER_CORE_SIZE = SERVER_CORE_SIZE;
	}

	public Charset getSERVER_ENCODING() {
		return SERVER_ENCODING;
	}

	public void setSERVER_ENCODING(Charset SERVER_ENCODING) {
		this.SERVER_ENCODING = SERVER_ENCODING;
	}

	public boolean isSERVER_DEBUG() {
		return SERVER_DEBUG;
	}

	public void setSERVER_DEBUG(boolean SERVER_DEBUG) {
		this.SERVER_DEBUG = SERVER_DEBUG;
	}

	public String getSERVER_HOST() {
		return SERVER_HOST;
	}

	public void setSERVER_HOST(String SERVER_HOST) {
		this.SERVER_HOST = SERVER_HOST;
	}

	public long getSERVER_SESSION_IDLE_TIME() {
		return SERVER_SESSION_IDLE_TIME;
	}

	public void setSERVER_SESSION_IDLE_TIME(long SERVER_SESSION_IDLE_TIME) {
		
		if (SERVER_SESSION_IDLE_TIME == 0) {
			return;
		}
		
		this.SERVER_SESSION_IDLE_TIME = SERVER_SESSION_IDLE_TIME;
	}

	public int getSERVER_MEMORY_POOL_UNIT() {
		return SERVER_MEMORY_POOL_UNIT;
	}

	public void setSERVER_MEMORY_POOL_UNIT(int SERVER_MEMORY_POOL_UNIT) {
		if (SERVER_MEMORY_POOL_UNIT == 0) {
			return;
		}
		this.SERVER_MEMORY_POOL_UNIT = SERVER_MEMORY_POOL_UNIT;
	}

	public int getSERVER_MEMORY_POOL_CAPACITY() {
		return SERVER_MEMORY_POOL_CAPACITY * getSERVER_CORE_SIZE();
	}

	public void setSERVER_MEMORY_POOL_CAPACITY(int SERVER_MEMORY_POOL_CAPACITY) {
		if (SERVER_MEMORY_POOL_CAPACITY == 0) {
			return;
		}
		this.SERVER_MEMORY_POOL_CAPACITY = SERVER_MEMORY_POOL_CAPACITY;
	}
	
}
