package com.gifisan.nio.extend.configuration;

import java.nio.charset.Charset;

public class ServerConfiguration {

	private String		SERVER_HOST		;
	private int		SERVER_TCP_PORT	;
	private int		SERVER_UDP_PORT	;
	private int		SERVER_CORE_SIZE	;
	private Charset	SERVER_ENCODING	;
	private boolean	SERVER_DEBUG		= false;
	
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

}
