package com.gifisan.nio.server.configuration;

import java.nio.charset.Charset;

public class ServerConfiguration {

	private int		SERVER_PORT		= 0;
	private int		SERVER_CORE_SIZE	= 0;
	private Charset	SERVER_ENCODING	= null;
	private boolean	SERVER_UDP_BOOT	= false;
	private boolean	SERVER_DEBUG		= false;
	private String		SERVER_USERNAME	= null;
	private String		SERVER_PASSWORD	= null;
	
	public int getSERVER_PORT() {
		return SERVER_PORT;
	}
	protected void setSERVER_PORT(int SERVER_PORT) {
		this.SERVER_PORT = SERVER_PORT;
	}
	public int getSERVER_CORE_SIZE() {
		return SERVER_CORE_SIZE;
	}
	protected void setSERVER_CORE_SIZE(int SERVER_CORE_SIZE) {
		this.SERVER_CORE_SIZE = SERVER_CORE_SIZE;
	}
	public Charset getSERVER_ENCODING() {
		return SERVER_ENCODING;
	}
	protected void setSERVER_ENCODING(Charset SERVER_ENCODING) {
		this.SERVER_ENCODING = SERVER_ENCODING;
	}
	public boolean isSERVER_UDP_BOOT() {
		return SERVER_UDP_BOOT;
	}
	protected void setSERVER_UDP_BOOT(boolean SERVER_UDP_BOOT) {
		this.SERVER_UDP_BOOT = SERVER_UDP_BOOT;
	}
	public boolean isSERVER_DEBUG() {
		return SERVER_DEBUG;
	}
	protected void setSERVER_DEBUG(boolean SERVER_DEBUG) {
		this.SERVER_DEBUG = SERVER_DEBUG;
	}
	public String getSERVER_USERNAME() {
		return SERVER_USERNAME;
	}
	protected void setSERVER_USERNAME(String SERVER_USERNAME) {
		this.SERVER_USERNAME = SERVER_USERNAME;
	}
	public String getSERVER_PASSWORD() {
		return SERVER_PASSWORD;
	}
	protected void setSERVER_PASSWORD(String SERVER_PASSWORD) {
		this.SERVER_PASSWORD = SERVER_PASSWORD;
	}
	

}
