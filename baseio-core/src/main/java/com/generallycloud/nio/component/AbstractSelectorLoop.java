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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.AbstractEventLoop;
import com.generallycloud.nio.component.concurrent.BufferedArrayList;

public abstract class AbstractSelectorLoop extends AbstractEventLoop implements SelectorEventLoop {

	private static final Logger					logger				= LoggerFactory
			.getLogger(AbstractSelectorLoop.class);
	protected ByteBufAllocator					byteBufAllocator		= null;
	protected boolean							hasTask				= false;
	private boolean							isMainSelector			= false;
	private boolean							isWaitForRegist		= false;
	private ReentrantLock						isWaitForRegistLock		= new ReentrantLock();
	protected BufferedArrayList<SelectorLoopEvent>	negativeEvents			= new BufferedArrayList<SelectorLoopEvent>();
	protected BufferedArrayList<SelectorLoopEvent>	positiveEvents			= new BufferedArrayList<SelectorLoopEvent>();
	protected ReentrantLock						runLock				= new ReentrantLock();
	protected int								runTask				= 0;
	protected SelectableChannel					selectableChannel		= null;
	protected SelectorEventLoopGroup				selectorEventLoopGroup	= null;
	protected AtomicBoolean						selecting				= new AtomicBoolean();
	protected Selector							selector				= null;
	private SessionManager						sessionManager			= null;

	protected AbstractSelectorLoop(ChannelService service, SelectorEventLoopGroup group) {

		ChannelContext context = service.getContext();

		this.selectorEventLoopGroup = group;

		this.sessionManager = context.getSessionManager();

		this.selectableChannel = service.getSelectableChannel();

		this.byteBufAllocator = context.getMcByteBufAllocator().getNextBufAllocator();
	}

	protected void cancelSelectionKey(SelectionKey selectionKey) {

		Object attachment = selectionKey.attachment();

		if (attachment instanceof Channel) {

			CloseUtil.close((Channel) attachment);
		}
	}

	protected void cancelSelectionKey(SocketChannel channel, Throwable t) {

		logger.error(t.getMessage() + " channel:" + channel, t);

		CloseUtil.close(channel);
	}

	@Override
	public void dispatch(SelectorLoopEvent event) throws RejectedExecutionException {

		if (inEventLoop()) {

			if (!isRunning()) {
				CloseUtil.close(event);
				return;
			}

			handleEvent(event);

			return;
		}

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

	@Override
	protected void doLoop() {

		try {

			Selector selector = getSelector();

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

			if (isMainSelector()) {
				sessionManager.loop();
			}

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void doStartup() throws IOException {
		this.selector = buildSelector(selectableChannel);
	}

	public void fireEvent(SelectorLoopEvent event) {

		positiveEvents.offer(event);

		if (positiveEvents.getBufferSize() < 3) {

			wakeup();
		}
	}

	@Override
	public ByteBufAllocator getByteBufAllocator() {
		return byteBufAllocator;
	}

	@Override
	public ReentrantLock getIsWaitForRegistLock() {
		return isWaitForRegistLock;
	}

	@Override
	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

	@Override
	public Selector getSelector() {
		return selector;
	}

	public void handleEvent(SelectorLoopEvent event) {

		try {

			if (!event.handle(this)) {
				return;
			}

			// FIXME xiaolv hui jiangdi
			if (event.isPositive()) {
				positiveEvents.offer(event);
			} else {
				negativeEvents.offer(event);
			}

		} catch (IOException e) {

			CloseUtil.close(event);
		}
	}

	private void handleEvents(List<SelectorLoopEvent> eventBuffer) {

		for (SelectorLoopEvent event : eventBuffer) {

			handleEvent(event);
		}
	}

	protected void handleNegativeEvents() {

		List<SelectorLoopEvent> eventBuffer = negativeEvents.getBuffer();

		if (eventBuffer.size() == 0) {
			return;
		}

		handleEvents(eventBuffer);
	}

	protected void handlePositiveEvents(boolean refresh) {

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

	@Override
	public boolean isMainSelector() {
		return isMainSelector;
	}

	@Override
	public boolean isWaitForRegist() {
		return isWaitForRegist;
	}

	@Override
	public void rebuildSelector() {
		this.selector = rebuildSelector0();
	}

	private Selector rebuildSelector0() {

		Selector selector;
		try {
			selector = buildSelector(selectableChannel);
		} catch (IOException e) {
			throw new Error(e);
		}

		// Selector old = this.selector;

		// Set<SelectionKey> sks = old.keys();
		//
		// if (sks.size() == 0) {
		// logger.debug("sk size 0");
		// CloseUtil.close(old);
		// return selector;
		// }
		//
		// for (SelectionKey sk : sks) {
		//
		// if (!sk.isValid() || sk.attachment() == null) {
		// cancelSelectionKey(sk);
		// continue;
		// }
		//
		// try {
		// sk.channel().register(selector, SelectionKey.OP_READ);
		// } catch (ClosedChannelException e) {
		// cancelSelectionKey(sk, e);
		// }
		// }
		//
		// CloseUtil.close(old);

		return selector;
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

			looper.rebuildSelector();
		}
	}

	@Override
	public void setMainSelector(boolean isMainSelector) {
		this.isMainSelector = isMainSelector;
	}

	@Override
	public void setWaitForRegist(boolean isWaitForRegist) {
		this.isWaitForRegist = isWaitForRegist;
	}

	private void waitForRegist() {

		ReentrantLock lock = getIsWaitForRegistLock();

		lock.lock();

		lock.unlock();
	}

	// FIXME 会不会出现这种情况，数据已经接收到本地，但是还没有被EventLoop处理完
	// 执行stop的时候如果确保不会再有数据进来
	@Override
	public void wakeup() {

		if (selecting.compareAndSet(false, true)) {
			selecting.set(false);
			return;
		}

		selector.wakeup();

		super.wakeup();
	}

}
