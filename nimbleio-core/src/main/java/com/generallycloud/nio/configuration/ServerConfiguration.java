package com.generallycloud.nio.configuration;

import java.nio.charset.Charset;

import com.generallycloud.nio.Encoding;

//FIXME 校验参数
public class ServerConfiguration {

	private int		SERVER_TCP_PORT;
	private int		SERVER_UDP_PORT;
	private String		SERVER_HOST				= "localhost";
	private int		SERVER_CORE_SIZE			= Runtime.getRuntime().availableProcessors();
	private Charset	SERVER_ENCODING			= Encoding.DEFAULT;
	private boolean	SERVER_DEBUG				= false;
	private int		SERVER_CHANNEL_QUEUE_SIZE	= 1024 * 512;
	private long		SERVER_SESSION_IDLE_TIME		= 30 * 1000;
	private boolean	SERVER_IS_ACCEPT_BEAT		= false;
	private int		SERVER_SESSION_ATTACH_SIZE	= 1;

	public int getSERVER_CHANNEL_QUEUE_SIZE() {
		return SERVER_CHANNEL_QUEUE_SIZE;
	}

	public void setSERVER_CHANNEL_QUEUE_SIZE(int SERVER_CHANNEL_QUEUE_SIZE) {
		this.SERVER_CHANNEL_QUEUE_SIZE = SERVER_CHANNEL_QUEUE_SIZE;
	}

	public int getSERVER_TCP_PORT() {
		return SERVER_TCP_PORT;
	}

	public void setSERVER_TCP_PORT(int SERVER_TCP_PORT) {
		this.SERVER_TCP_PORT = SERVER_TCP_PORT;
	}

	public int getSERVER_UDP_PORT() {
		return SERVER_UDP_PORT;
	}

	public void setSERVER_UDP_PORT(int SERVER_UDP_PORT) {
		this.SERVER_UDP_PORT = SERVER_UDP_PORT;
	}

	public int getSERVER_CORE_SIZE() {
		return SERVER_CORE_SIZE;
	}

	public void setSERVER_CORE_SIZE(int SERVER_CORE_SIZE) {
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
		
		if (SERVER_SESSION_IDLE_TIME < 1) {
			throw new IllegalArgumentException("illegal SERVER_SESSION_IDLE_TIME:" + SERVER_SESSION_IDLE_TIME);
		}

		this.SERVER_SESSION_IDLE_TIME = SERVER_SESSION_IDLE_TIME;
	}

	public boolean isSERVER_IS_ACCEPT_BEAT() {
		return SERVER_IS_ACCEPT_BEAT;
	}

	public void setSERVER_IS_ACCEPT_BEAT(boolean SERVER_IS_ACCEPT_BEAT) {
		this.SERVER_IS_ACCEPT_BEAT = SERVER_IS_ACCEPT_BEAT;
	}

	public int getSERVER_SESSION_ATTACH_SIZE() {
		return SERVER_SESSION_ATTACH_SIZE;
	}

	public void setSERVER_SESSION_ATTACH_SIZE(int SERVER_SESSION_ATTACH_SIZE) {
		this.SERVER_SESSION_ATTACH_SIZE = SERVER_SESSION_ATTACH_SIZE;
	}

	
}
