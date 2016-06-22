package com.gifisan.nio.server.configuration;

import java.nio.charset.Charset;

public class ServerConfiguration {

	private String		SERVER_HOST		= null;
	private int		SERVER_PORT		= 0;
	private int		SERVER_CORE_SIZE	= 0;
	private Charset	SERVER_ENCODING	= null;
	private boolean	SERVER_UDP_BOOT	= false;
	private boolean	SERVER_DEBUG		= false;

	public int getSERVER_PORT() {
		return SERVER_PORT;
	}

	public void setSERVER_PORT(int SERVER_PORT) {
		this.SERVER_PORT = SERVER_PORT;
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

	public boolean isSERVER_UDP_BOOT() {
		return SERVER_UDP_BOOT;
	}

	public void setSERVER_UDP_BOOT(boolean SERVER_UDP_BOOT) {
		this.SERVER_UDP_BOOT = SERVER_UDP_BOOT;
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
