/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.BufferedArrayList;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.concurrent.LineEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
//FIXME 使用ThreadLocal
public class SocketSelectorEventLoop extends AbstractSelectorLoop
		implements SocketChannelThreadContext {

	private static final Logger				logger				= LoggerFactory
			.getLogger(SocketSelectorEventLoop.class);

	private ByteBuf						buf					= null;

	private ChannelByteBufReader				byteBufReader			= null;

	private NioSocketChannelContext			context				= null;

	private ExecutorEventLoop				executorEventLoop		= null;

	private SocketSessionManager				sessionManager			= null;

	private SocketSelectorEventLoopGroup		eventLoopGroup			= null;

	private SocketSelectorBuilder				selectorBuilder		= null;

	private SocketSelector					selector				= null;

	private int							runTask				= 0;

	private boolean						hasTask				= false;

	private SslHandler						sslHandler			= null;

	private AtomicBoolean					selecting				= new AtomicBoolean();

	private UnpooledByteBufAllocator			unpooledByteBufAllocator	= null;

	private BufferedArrayList<SelectorLoopEvent>	negativeEvents			= new BufferedArrayList<>();

	private BufferedArrayList<SelectorLoopEvent>	positiveEvents			= new BufferedArrayList<>();

	public SocketSelectorEventLoop(SocketSelectorEventLoopGroup group, int coreIndex) {

		super(group.getChannelContext(), coreIndex);

		this.eventLoopGroup = group;

		this.context = group.getChannelContext();

		this.selectorBuilder = ((NioChannelService) context.getChannelService())
				.getSelectorBuilder();

		this.executorEventLoop = context.getExecutorEventLoopGroup().getNext();

		this.byteBufReader = context.getChannelByteBufReader();

		this.sessionManager = context.getSessionManager();

		this.sessionManager = new NioSocketSessionManager(context,this);

		// FIXME 使用direct
		this.unpooledByteBufAllocator = new UnpooledByteBufAllocator(true);
		
		if (context.isEnableSSL()) {
			sslHandler = context.getSslContext().newSslHandler(context);
		}
	}

	public SocketSelector getSelector() {
		return selector;
	}

	@Override
	public void rebuildSelector() throws IOException {
		this.selector = rebuildSelector0();
	}

	public void accept(NioSocketChannel channel) {
		if (!channel.isOpened()) {
			return;
		}
		try {
			ByteBuf buf = this.buf;
			buf.clear();
			buf.nioBuffer();
			int length = channel.read(buf);
			if (length < 1) {
				if (length == -1) {
					CloseUtil.close(channel);
				}
				return;
			}
			channel.active();
			byteBufReader.accept(channel, buf.flip());
		} catch (Throwable e) {
			cancelSelectionKey(channel, e);
		}
	}

	@Override
	public void doStartup() throws IOException {

		if (executorEventLoop instanceof LineEventLoop) {
			((LineEventLoop) executorEventLoop).setMonitor(this);
		}

		LifeCycleUtil.start(unpooledByteBufAllocator);

		int readBuffer = context.getServerConfiguration().getSERVER_CHANNEL_READ_BUFFER();

		this.buf = unpooledByteBufAllocator.allocate(readBuffer);

		super.doStartup();
	}

	@Override
	protected void doStop() {
		ThreadUtil.sleep(8);
		closeEvents(positiveEvents);
		closeEvents(negativeEvents);
		CloseUtil.close(selector);
		ReleaseUtil.release(buf);
		LifeCycleUtil.stop(unpooledByteBufAllocator);
	}

	private void closeEvents(BufferedArrayList<SelectorLoopEvent> bufferedList) {
		List<SelectorLoopEvent> events = bufferedList.getBuffer();
		for (SelectorLoopEvent event : events) {
			CloseUtil.close(event);
		}
	}

	@Override
	public NioSocketChannelContext getChannelContext() {
		return context;
	}

	public ExecutorEventLoop getExecutorEventLoop() {
		return executorEventLoop;
	}

	@Override
	protected void doLoop() throws IOException {

		SocketSelector selector = getSelector();

		int selected;

		// long last_select = System.currentTimeMillis();

		if (hasTask) {
			if (runTask > 0) {
				runTask--;
				handlePositiveEvents();
				checkTask(false);
				return;
			}
			selected = selector.selectNow();
		} else {
			if (selecting.compareAndSet(false, true)) {
				selected = selector.select(4);// FIXME try
				selecting.set(false);
			} else {
				selected = selector.selectNow();
			}
		}

		if (selected < 1) {
			handleNegativeEvents();
			// selectEmpty(last_select);
		} else {
			accept(selector.selectedKeys());
		}

		handlePositiveEvents();

		sessionManager.loop();

		checkTask(true);
	}
	
	private void accept(Set<SelectionKey> sks){
		for (SelectionKey k : sks) {
			if (!k.isValid()) {
				continue;
			}
			NioSocketChannel channel = (NioSocketChannel) k.attachment();
			if (channel == null) {
				// channel为空说明该链接未打开
				try {
					selector.buildChannel(k);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
				continue;
			}
			accept(channel);
		}
		sks.clear();
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

		//FIXME 有一定几率select(16)ms
		if (selecting.compareAndSet(false, true)) {
			selecting.set(false);
			return;
		}

		getSelector().wakeup();

		super.wakeup();
	}

	public void dispatch(SelectorLoopEvent event) {

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

		if (!isRunning()) {
			CloseUtil.close(event);
			return;
		}

		positiveEvents.offer(event);
		
		// 这里再次判断一下，防止判断isRunning为true后的线程切换停顿
		// 如果切换停顿，这里判断可以确保event要么被close了，要么被执行了
		if (!isRunning()) {
			CloseUtil.close(event);
			return;
		}

		wakeup();

	}

	private void handleEvent(SelectorLoopEvent event) {

		try {

			event.fireEvent(this);

			if (event.isComplete()) {
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

	private void handlePositiveEvents() {

		List<SelectorLoopEvent> eventBuffer = positiveEvents.getBuffer();

		if (eventBuffer.size() == 0) {

			hasTask = false;

			return;
		}

		handleEvents(eventBuffer);
	}

	private void checkTask(boolean refresh) {

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
	
	@Override
	public SslHandler getSslHandler() {
		return sslHandler;
	}

	@Override
	public SocketSessionManager getSocketSessionManager() {
		return sessionManager;
	}
}
