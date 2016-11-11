package com.generallycloud.nio.configuration;

import java.math.BigDecimal;
import java.nio.charset.Charset;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.component.BaseContext;

//FIXME 校验参数
public class ServerConfiguration {

	private int		SERVER_TCP_PORT;
	private int		SERVER_UDP_PORT;
	private String		SERVER_HOST					= "localhost";
	private int		SERVER_CORE_SIZE				= Runtime.getRuntime().availableProcessors();
	private Charset	SERVER_ENCODING				= Encoding.UTF8;
	private int		SERVER_IO_EVENT_QUEUE			= 0;
	private long		SERVER_SESSION_IDLE_TIME			= 30 * 1000;
	private int		SERVER_MEMORY_POOL_UNIT;
	private int		SERVER_MEMORY_POOL_CAPACITY;
	private int		SERVER_READ_BUFFER				= 1024 * 100;
	private double	SERVER_MEMORY_POOL_CAPACITY_RATE	= 1d;

	public ServerConfiguration() {
	}

	public ServerConfiguration(int SERVER_TCP_PORT) {
		this.SERVER_TCP_PORT = SERVER_TCP_PORT;
	}

	public ServerConfiguration(String SERVER_HOST, int SERVER_TCP_PORT) {
		this.SERVER_TCP_PORT = SERVER_TCP_PORT;
		this.SERVER_HOST = SERVER_HOST;
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

	public int getSERVER_IO_EVENT_QUEUE() {
		return SERVER_IO_EVENT_QUEUE;
	}

	public void setSERVER_IO_EVENT_QUEUE(int SERVER_IO_EVENT_QUEUE) {
		if (SERVER_IO_EVENT_QUEUE == 0) {
			return;
		}
		this.SERVER_IO_EVENT_QUEUE = SERVER_IO_EVENT_QUEUE;
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

	public int getSERVER_READ_BUFFER() {
		return SERVER_READ_BUFFER;
	}

	public void setSERVER_READ_BUFFER(int SERVER_READ_BUFFER) {
		if (SERVER_READ_BUFFER == 0) {
			return;
		}
		this.SERVER_READ_BUFFER = SERVER_READ_BUFFER;
	}

	public double getSERVER_MEMORY_POOL_CAPACITY_RATE() {
		return SERVER_MEMORY_POOL_CAPACITY_RATE;
	}

	public void setSERVER_MEMORY_POOL_CAPACITY_RATE(double SERVER_MEMORY_POOL_CAPACITY_RATE) {
		this.SERVER_MEMORY_POOL_CAPACITY_RATE = SERVER_MEMORY_POOL_CAPACITY_RATE;
	}

	public void initializeDefault(BaseContext context) {

		if (SERVER_MEMORY_POOL_UNIT == 0) {
			SERVER_MEMORY_POOL_UNIT = 256;
		}

		if (SERVER_MEMORY_POOL_CAPACITY == 0) {

			long total = Runtime.getRuntime().maxMemory();

			SERVER_MEMORY_POOL_CAPACITY = (int) (total / (SERVER_MEMORY_POOL_UNIT * SERVER_CORE_SIZE * 4));

			SERVER_MEMORY_POOL_CAPACITY *= SERVER_MEMORY_POOL_CAPACITY_RATE;
		}

		if (SERVER_IO_EVENT_QUEUE == 0) {

			double MEMORY_POOL_SIZE = new BigDecimal(SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_UNIT).divide(
					new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).doubleValue();

			SERVER_IO_EVENT_QUEUE = (int) (MEMORY_POOL_SIZE * 1024 * 4);
		}
	}

}
