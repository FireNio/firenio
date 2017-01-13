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
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.concurrent.BufferedArrayList;
import com.generallycloud.nio.component.concurrent.ExecutorEventLoop;
import com.generallycloud.nio.component.concurrent.LineEventLoop;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class SocketSelectorEventLoopImpl extends AbstractSelectorLoop
		implements SocketSelectorEventLoop {

	private static final Logger				logger			= LoggerFactory
			.getLogger(SocketSelectorEventLoopImpl.class);

	private ByteBuf						buf				= null;

	private ChannelByteBufReader				byteBufReader		= null;

	private SocketChannelContext				context			= null;

	private ProtocolDecoder					protocolDecoder	= null;

	private ProtocolEncoder					protocolEncoder	= null;

	private ProtocolFactory					protocolFactory	= null;

	private ExecutorEventLoop				executorEventLoop	= null;

	private boolean						isWaitForRegist	= false;

	private SessionManager					sessionManager		= null;

	private SocketSelectorEventLoopGroup		eventLoopGroup		= null;

	private SocketSelectorBuilder				selectorBuilder	= null;

	private SocketSelector					selector			= null;

	private ReentrantLock					runLock			= new ReentrantLock();

	private int							runTask			= 0;

	private boolean						hasTask			= false;

	private int							eventQueueSize		= 0;

	private BufferedArrayList<SelectorLoopEvent>	negativeEvents		= new BufferedArrayList<SelectorLoopEvent>();

	private BufferedArrayList<SelectorLoopEvent>	positiveEvents		= new BufferedArrayList<SelectorLoopEvent>();

	private AtomicBoolean					selecting			= new AtomicBoolean();

	private ReentrantLock					isWaitForRegistLock	= new ReentrantLock();

	public SocketSelectorEventLoopImpl(SocketSelectorEventLoopGroup group, int eventQueueSize,
			int coreIndex) {

		super(group.getChannelContext(), coreIndex);

		this.eventLoopGroup = group;

		this.context = group.getChannelContext();

		this.selectorBuilder = context.getChannelService().getSelectorBuilder();

		this.executorEventLoop = context.getExecutorEventLoopGroup().getNext();

		this.protocolFactory = context.getProtocolFactory();

		this.protocolDecoder = protocolFactory.getProtocolDecoder();

		this.protocolEncoder = protocolFactory.getProtocolEncoder();

		this.byteBufReader = context.getChannelByteBufReader();

		this.sessionManager = context.getSessionManager();

		this.eventQueueSize = eventQueueSize;

		int readBuffer = context.getServerConfiguration().getSERVER_CHANNEL_READ_BUFFER();

		// FIXME 使用direct
		this.buf = UnpooledByteBufAllocator.getInstance().allocate(readBuffer);
	}

	@Override
	public ReentrantLock getIsWaitForRegistLock() {
		return isWaitForRegistLock;
	}

	@Override
	public boolean isWaitForRegist() {
		return isWaitForRegist;
	}

	@Override
	public void setWaitForRegist(boolean isWaitForRegist) {
		this.isWaitForRegist = isWaitForRegist;
	}

	@Override
	public SocketSelector getSelector() {
		return selector;
	}

	@Override
	public void rebuildSelector() throws IOException {
		this.selector = rebuildSelector0();
	}

	private void waitForRegist() {

		ReentrantLock lock = getIsWaitForRegistLock();

		lock.lock();

		lock.unlock();
	}

	@Override
	public void accept(SocketChannel channel) {

		if (!channel.isOpened()) {
			return;
		}

		try {

			accept0(channel);

		} catch (Throwable e) {

			cancelSelectionKey(channel, e);
		}
	}

	public void accept0(SocketChannel channel) throws Exception {

		ByteBuf buf = this.buf;

		buf.clear();

		buf.nioBuffer();

		int length = buf.read(channel);

		if (length < 1) {

			if (length == -1) {
				CloseUtil.close(channel);
			}
			return;
		}

		channel.active();

		byteBufReader.accept(channel, buf.flip());
	}

	@Override
	public void doStartup() throws IOException {

		if (executorEventLoop instanceof LineEventLoop) {
			((LineEventLoop) executorEventLoop).setMonitor(this);
		}

		super.doStartup();
	}

	@Override
	protected void doStop() {

		ReentrantLock lock = this.runLock;

		lock.lock();

		try {

			List<SelectorLoopEvent> eventBuffer = positiveEvents.getBuffer();

			for (SelectorLoopEvent event : eventBuffer) {

				CloseUtil.close(event);
			}

		} finally {

			lock.unlock();
		}

		CloseUtil.close(selector);

		ReleaseUtil.release(buf);
	}

	@Override
	public SocketChannelContext getChannelContext() {
		return context;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	public ExecutorEventLoop getExecutorEventLoop() {
		return executorEventLoop;
	}

	@Override
	protected void doLoop() {

		try {

			SocketSelector selector = getSelector();

			int selected;

			// long last_select = System.currentTimeMillis();

			if (hasTask) {

				if (runTask-- > 0) {

					handlePositiveEvents(false);

					return;
				}

				selected = selector.selectNow();
			} else {

				if (selecting.compareAndSet(false, true)) {

					selected = selector.select(16);// FIXME try

					selecting.set(false);
				} else {

					selected = selector.selectNow();
				}
			}

			if (isWaitForRegist()) {

				waitForRegist();
			}

			if (selected < 1) {

				handleNegativeEvents();

				// selectEmpty(last_select);
			} else {

				List<SocketChannel> selectedChannels = selector.selectedChannels();

				for (SocketChannel channel : selectedChannels) {

					accept(channel);
				}

				selector.clearSelectedChannels();
			}

			handlePositiveEvents(true);

			if (isMainEventLoop()) {
				sessionManager.loop();
			}

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);
		}
	}

	private SocketSelector rebuildSelector0() throws IOException {
		SocketSelector selector = selectorBuilder.build(this);

//		Selector old = this.selector;
//
//		Set<SelectionKey> sks = old.keys();
//
//		if (sks.size() == 0) {
//			logger.debug("sk size 0");
//			CloseUtil.close(old);
//			return selector;
//		}
//
//		for (SelectionKey sk : sks) {
//
//			if (!sk.isValid() || sk.attachment() == null) {
//				cancelSelectionKey(sk);
//				continue;
//			}
//
//			try {
//				sk.channel().register(selector, SelectionKey.OP_READ);
//			} catch (ClosedChannelException e) {
//				cancelSelectionKey(sk, e);
//			}
//		}
//
//		CloseUtil.close(old);

		return selector;
	}

	@Override
	public SocketSelectorEventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}

	// FIXME 会不会出现这种情况，数据已经接收到本地，但是还没有被EventLoop处理完
	// 执行stop的时候如果确保不会再有数据进来
	@Override
	public void wakeup() {

		if (selecting.compareAndSet(false, true)) {
			selecting.set(false);
			return;
		}

		getSelector().wakeup();

		super.wakeup();
	}

	@Override
	public void dispatch(SelectorLoopEvent event) throws RejectedExecutionException {

		//FIXME 找出这里出问题的原因
		//		if (inEventLoop()) {
		//
		//			if (!isRunning()) {
		//				CloseUtil.close(event);
		//				return;
		//			}
		//
		//			handleEvent(event);
		//
		//			return;
		//		}

		ReentrantLock lock = this.runLock;

		lock.lock();

		try {

			dispatch0(event);

		} finally {

			lock.unlock();
		}
	}

	private void dispatch0(SelectorLoopEvent event) {

		if (!isRunning()) {
			CloseUtil.close(event);
			return;
		}

		fireEvent(event);
	}

	private void fireEvent(SelectorLoopEvent event) {

		if (positiveEvents.getBufferSize() > eventQueueSize) {
			throw new RejectedExecutionException();
		}

		positiveEvents.offer(event);

		if (positiveEvents.getBufferSize() < 3) {

			wakeup();
		}
	}

	private void handleEvent(SelectorLoopEvent event) {

		try {

			if (!event.handle(this)) {
				return;
			}

			// FIXME xiaolv hui jiangdi
			if (event.isPositive()) {
				positiveEvents.offer(event);
				return;
			}

			negativeEvents.offer(event);

		} catch (IOException e) {

			CloseUtil.close(event);
		}
	}

	private void handleEvents(List<SelectorLoopEvent> eventBuffer) {

		for (SelectorLoopEvent event : eventBuffer) {

			handleEvent(event);
		}
	}

	private void handleNegativeEvents() {

		List<SelectorLoopEvent> eventBuffer = negativeEvents.getBuffer();

		if (eventBuffer.size() == 0) {
			return;
		}

		handleEvents(eventBuffer);
	}

	private void handlePositiveEvents(boolean refresh) {

		List<SelectorLoopEvent> eventBuffer = positiveEvents.getBuffer();

		if (eventBuffer.size() == 0) {

			hasTask = false;

			return;
		}

		handleEvents(eventBuffer);

		hasTask = positiveEvents.getBufferSize() > 0;

		if (hasTask && refresh) {
			runTask = 5;
		}
	}

	protected void selectEmpty(SelectorEventLoop looper, long last_select) {

		long past = System.currentTimeMillis() - last_select;

		if (past < 1000) {

			if (!looper.isRunning() || past < 0) {
				return;
			}

			// JDK bug fired ?
			IOException e = new IOException("JDK bug fired ?");
			logger.error(e.getMessage(), e);
			logger.debug("last={},past={}", last_select, past);

			try {
				looper.rebuildSelector();
			} catch (IOException e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}

}
