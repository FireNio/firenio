/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.SelectorEventLoop.SelectorLoopEvent;

public abstract class AbstractChannel implements Channel {

	protected String			edp_description;
	protected Integer			channelID;
	protected InetSocketAddress	local;
	protected InetSocketAddress	remote;
	protected long			lastAccess;
	protected ByteBufAllocator	byteBufAllocator;
	protected SelectorEventLoop	selectorEventLoop;
	protected boolean			opened		= true;
	protected boolean			closing		= false;
	protected long			creationTime	= System.currentTimeMillis();
	protected ReentrantLock		channelLock	= new ReentrantLock();

	public AbstractChannel(SelectorEventLoop selectorEventLoop) {
		ChannelContext context = selectorEventLoop.getContext();
		this.selectorEventLoop = selectorEventLoop;
		this.byteBufAllocator = selectorEventLoop.getByteBufAllocator();
		// 认为在第一次Idle之前，连接都是畅通的
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
	public void close() throws IOException {

		ReentrantLock lock = getChannelLock();

		lock.lock();

		try {

			if (!isOpened()) {
				return;
			}

			if (inSelectorLoop()) {
				physicalClose();
				return;
			}

			if (isClosing()) {
				return;
			}

			closing = true;

			fireClose();
		} finally {
			lock.unlock();
		}
	}

	public void fireEvent(SelectorLoopEvent event) {
		this.selectorEventLoop.dispatch(event);
	}

	private void fireClose() {

		fireEvent(new SelectorLoopEventAdapter() {

			@Override
			public boolean handle(SelectorEventLoop selectLoop) throws IOException {
				CloseUtil.close(AbstractChannel.this);
				return false;
			}
		});
	}

	@Override
	public boolean inSelectorLoop() {
		return selectorEventLoop.inEventLoop();
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
			edp_description = new StringBuilder("[").append(getMarkPrefix()).append("(id:")
					.append(getIdHexString(channelID)).append(") R /").append(getRemoteAddr()).append(":")
					.append(getRemotePort()).append("; Lp:").append(getLocalPort()).append("]").toString();
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
