package com.generallycloud.nio.component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.StringUtil;

public abstract class AbstractChannel implements Channel {

	protected String			edp_description;
	protected Integer			channelID;
	protected InetSocketAddress	local;
	protected InetSocketAddress	remote;
	protected long			lastAccess;
	protected ByteBufAllocator	byteBufAllocator;
	protected SelectorLoop		selectorLoop;
	protected boolean			opened		= true;
	protected boolean			closing		= false;
	protected long			creationTime	= System.currentTimeMillis();
	protected ReentrantLock		channelLock	= new ReentrantLock();

	public AbstractChannel(SelectorLoop selectorLoop) {
		ChannelContext context = selectorLoop.getContext();
		this.selectorLoop = selectorLoop;
		this.byteBufAllocator = selectorLoop.getByteBufAllocator();
		// 这里认为在第一次Idle之前，连接都是畅通的
		this.lastAccess = this.creationTime + context.getSessionIdleTime();
		this.channelID = context.getSequence().AUTO_CHANNEL_ID.getAndIncrement();
	}

	@Override
	public Integer getChannelID() {
		return channelID;
	}

	@Override
	public String getLocalAddr() {

		InetAddress address = getLocalSocketAddress().getAddress();

		if (address == null) {
			return "127.0.0.1";
		}

		return address.getHostAddress();
	}

	@Override
	public boolean inSelectorLoop() {
		return Thread.currentThread() == selectorLoop.getMonitor();
	}

	@Override
	public String getLocalHost() {
		return getLocalSocketAddress().getHostName();
	}

	@Override
	public ByteBufAllocator getByteBufAllocator() {
		return byteBufAllocator;
	}

	@Override
	public int getLocalPort() {
		return getLocalSocketAddress().getPort();
	}

	@Override
	public abstract InetSocketAddress getLocalSocketAddress();

	protected abstract String getMarkPrefix();

	@Override
	public String getRemoteAddr() {

		InetSocketAddress address = getRemoteSocketAddress();

		if (address == null) {

			return "closed";
		}

		return address.getAddress().getHostAddress();
	}

	/**
	 * 请勿使用,可能出现阻塞
	 * 
	 * @see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6487744
	 */
	@Override
	@Deprecated
	public String getRemoteHost() {

		InetSocketAddress address = getRemoteSocketAddress();

		if (address == null) {

			return "closed";
		}

		return address.getAddress().getHostName();
	}

	@Override
	public int getRemotePort() {

		InetSocketAddress address = getRemoteSocketAddress();

		if (address == null) {

			return -1;
		}

		return address.getPort();
	}

	@Override
	public String toString() {

		if (edp_description == null) {
			edp_description = new StringBuilder("[")
					.append(getMarkPrefix())
					.append("(id:")
					.append(getIdHexString(channelID))
					.append(") R /")
					.append(getRemoteAddr())
					.append(":")
					.append(getRemotePort())
					.append("; Lp:")
					.append(getLocalPort())
					.append("]")
					.toString();
		}

		return edp_description;
	}

	private String getIdHexString(Integer channelID) {

		String id = Long.toHexString(channelID);

		return "0x" + StringUtil.getZeroString(8 - id.length()) + id;
	}

	@Override
	public ReentrantLock getChannelLock() {
		return channelLock;
	}

	@Override
	public void active() {
		this.lastAccess = System.currentTimeMillis();
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public long getLastAccessTime() {
		return lastAccess;
	}

}
