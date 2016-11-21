package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.concurrent.BufferedArrayList;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public abstract class AbstractSelectorLoop implements SelectorLoop {

	protected ByteBufAllocator					byteBufAllocator	= null;
	protected BaseContext						context			= null;
	protected boolean							isMainSelector		= false;
	protected boolean							isWaitedForRegist	= false;
	protected boolean							isWaitForRegist	= false;
	protected byte[]							isWaitForRegistLock	= new byte[] {};
	protected ProtocolDecoder					protocolDecoder	= null;
	protected ProtocolEncoder					protocolEncoder	= null;
	protected ProtocolFactory					protocolFactory	= null;
	protected SelectableChannel					selectableChannel	= null;
	protected Selector							selector			= null;
	protected boolean							shutdown			= false;
	protected boolean							working			= false;
	protected BufferedArrayList<SelectorLoopEvent>	events			= new BufferedArrayList<SelectorLoopEvent>();

	private static final Logger					logger			= LoggerFactory
																	.getLogger(AbstractSelectorLoop.class);

	protected AbstractSelectorLoop(BaseContext context, SelectableChannel selectableChannel) {

		this.context = context;

		this.selectableChannel = selectableChannel;

		this.protocolFactory = context.getProtocolFactory();

		this.protocolDecoder = protocolFactory.getProtocolDecoder();

		this.protocolEncoder = protocolFactory.getProtocolEncoder();

		this.byteBufAllocator = context.getMcByteBufAllocator().getNextBufAllocator();
	}

	public abstract void accept(SelectionKey key);

	protected void cancelSelectionKey(SelectionKey sk) {

		Object attachment = sk.attachment();

		if (attachment instanceof Channel) {
			CloseUtil.close((Channel) attachment);
		} else {

			IOException e1 = new IOException("cancel sk");

			logger.error(e1.getMessage(), e1);

			try {
				SelectableChannel ch = sk.channel();

				if (ch instanceof ServerSocketChannel) {

					SocketAddress l = ((ServerSocketChannel) ch).getLocalAddress();

					logger.debug("l={},r=null", l);

				} else if (ch instanceof java.nio.channels.SocketChannel) {

					SocketAddress r = ((java.nio.channels.SocketChannel) ch).getRemoteAddress();
					SocketAddress l = ((ServerSocketChannel) ch).getLocalAddress();

					logger.debug("l={},r={}", l, r);
				} else {

					logger.debug("l=null,r=null");
				}

			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	protected void cancelSelectionKey(SelectionKey selectionKey, Throwable t) {

		cancelSelectionKey(selectionKey);

		logger.error(t.getMessage(), t);
	}

	public ByteBufAllocator getByteBufAllocator() {
		return byteBufAllocator;
	}

	public BaseContext getContext() {
		return context;
	}

	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

	public Selector getSelector() {
		return selector;
	}

	public void loop() {

		try {

			working = true;

			if (shutdown) {
				working = false;
				return;
			}

			Selector selector = this.selector;

			if (isWaitForRegist) {

				synchronized (isWaitForRegistLock) {

					if (isWaitForRegist) {

						isWaitedForRegist = true;

						isWaitForRegistLock.wait();
					}
				}
			}

			int selected;

			long last_select = System.currentTimeMillis();

			if (hasTask) {

				selected = selector.selectNow();

			} else {

				selected = selector.select(1000);

				if (selected < 1) {

					selectEmpty(last_select);
				}
			}

			if (selected > 0) {

				Set<SelectionKey> selectionKeys = selector.selectedKeys();

				for (SelectionKey key : selectionKeys) {

					accept(key);
				}

				selectionKeys.clear();
			}

			flush();

			working = false;

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			working = false;
		}
	}

	private void flush() {

		ReentrantLock lock = events.getReentrantLock();

		List<SelectorLoopEvent> eventBuffer;

		lock.lock();

		try {
			eventBuffer = events.getBuffer();

			if (eventBuffer.size() == 0) {

				hasTask = false;

				return;
			}

		} finally {
			lock.unlock();
		}

		for (SelectorLoopEvent event : eventBuffer) {

			try {

				if(event.handle(this)){
					
					events.offer(event);
				}

			} catch (IOException e) {

				CloseUtil.close(event);

				continue;
			}
		}
	}

	private Selector rebuildSelector() {

		Selector selector;
		try {
			selector = buildSelector(selectableChannel);
		} catch (IOException e) {
			throw new Error(e);
		}

		Selector old = this.selector;

		Set<SelectionKey> sks = old.keys();

		if (sks.size() == 0) {
			logger.debug("sk size 0");
			CloseUtil.close(old);
			return selector;
		}

		for (SelectionKey sk : sks) {

			if (!sk.isValid() || sk.attachment() == null) {
				cancelSelectionKey(sk);
				continue;
			}

			try {
				sk.channel().register(selector, SelectionKey.OP_READ);
			} catch (ClosedChannelException e) {
				cancelSelectionKey(sk, e);
			}
		}

		CloseUtil.close(old);

		return selector;
	}

	private void selectEmpty(long last_select) {

		// long past = System.currentTimeMillis() - last_select;

		// if (past < 1000) {
		//
		// if (shutdown || past < 0) {
		// working = false;
		// return;
		// }
		//
		// // JDK bug fired ?
		// IOException e = new IOException("JDK bug fired ?");
		// logger.error(e.getMessage(), e);
		// logger.debug("last={},past={}", last_select, past);
		// this.selector = rebuildSelector();
		// }

		working = false;

	}

	public void startup() throws IOException {

		this.selector = buildSelector(selectableChannel);
	}

	// FIXME 会不会出现这种情况，数据已经接收到本地，但是还没有被EventLoop处理完
	// 执行stop的时候如果确保不会再有数据进来
	public void stop() {

		this.shutdown = true;

		this.selector.wakeup();

		for (; working;) {

			ThreadUtil.sleep(8);
		}

		try {
			this.selector.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void wakeup() {
		selector.wakeup();
	}

	private boolean	hasTask	= false;

	public void fireEvent(SelectorLoopEvent event) {

		ReentrantLock lock = events.getReentrantLock();

		lock.lock();

		try {

			events.offer(event);

			if (!hasTask) {

				hasTask = true;

				wakeup();
			}

		} finally {
			lock.unlock();
		}
	}

}
