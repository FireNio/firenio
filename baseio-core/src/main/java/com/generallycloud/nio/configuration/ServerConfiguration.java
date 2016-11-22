package com.generallycloud.nio.configuration;

import java.nio.charset.Charset;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.component.BaseContext;

//FIXME 校验参数
public class ServerConfiguration {

	private int		SERVER_PORT;
	private String		SERVER_HOST					= "localhost";
	private int		SERVER_CORE_SIZE				= Runtime.getRuntime().availableProcessors();
	private Charset	SERVER_ENCODING				= Encoding.UTF8;
	private int		SERVER_IO_EVENT_QUEUE			= 0;
	private long		SERVER_SESSION_IDLE_TIME			= 30 * 1000;
	private int		SERVER_MEMORY_POOL_UNIT;
	private boolean	SERVER_MEMORY_POOL_DIRECT;
	private boolean	SERVER_ENABLE_SSL;
	private boolean	SERVER_WORK_EVENT_LOOP;
	private int		SERVER_MEMORY_POOL_CAPACITY;
	private int		SERVER_READ_BUFFER				= 1024 * 100;
	private double	SERVER_MEMORY_POOL_CAPACITY_RATE	= 1d;

	public ServerConfiguration() {
	}

	public ServerConfiguration(int SERVER_PORT) {
		this.SERVER_PORT = SERVER_PORT;
	}

	public ServerConfiguration(String SERVER_HOST, int SERVER_PORT) {
		this.SERVER_PORT = SERVER_PORT;
		this.SERVER_HOST = SERVER_HOST;
	}

	public int getSERVER_PORT() {
		return SERVER_PORT;
	}

	public void setSERVER_PORT(int SERVER_PORT) {
		if (SERVER_PORT == 0) {
			return;
		}
		this.SERVER_PORT = SERVER_PORT;
	}

	public boolean isSERVER_ENABLE_SSL() {
		return SERVER_ENABLE_SSL;
	}

	public void setSERVER_ENABLE_SSL(boolean SERVER_ENABLE_SSL) {
		this.SERVER_ENABLE_SSL = SERVER_ENABLE_SSL;
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
		return (int) (SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_CAPACITY_RATE);
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
		if (SERVER_MEMORY_POOL_CAPACITY_RATE == 0) {
			return;
		}
		this.SERVER_MEMORY_POOL_CAPACITY_RATE = SERVER_MEMORY_POOL_CAPACITY_RATE;
	}

	public boolean isSERVER_MEMORY_POOL_DIRECT() {
		return SERVER_MEMORY_POOL_DIRECT;
	}

	public void setSERVER_MEMORY_POOL_DIRECT(boolean SERVER_MEMORY_POOL_DIRECT) {
		this.SERVER_MEMORY_POOL_DIRECT = SERVER_MEMORY_POOL_DIRECT;
	}

	public void initializeDefault(BaseContext context) {

		if (SERVER_MEMORY_POOL_UNIT == 0) {
			SERVER_MEMORY_POOL_UNIT = 512;
		}

		if (SERVER_MEMORY_POOL_CAPACITY == 0) {

			long total = Runtime.getRuntime().maxMemory();

			SERVER_MEMORY_POOL_CAPACITY = (int) (total / (SERVER_MEMORY_POOL_UNIT * SERVER_CORE_SIZE * 16));
		}
		
		if (SERVER_IO_EVENT_QUEUE == 0) {
			
			SERVER_IO_EVENT_QUEUE = getSERVER_MEMORY_POOL_CAPACITY() * 2;
		}
		
	}

	public boolean isSERVER_WORK_EVENT_LOOP() {
		return SERVER_WORK_EVENT_LOOP;
	}

	public void setSERVER_WORK_EVENT_LOOP(boolean SERVER_WORK_EVENT_LOOP) {
		this.SERVER_WORK_EVENT_LOOP = SERVER_WORK_EVENT_LOOP;
	}

}
