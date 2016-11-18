package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.concurrent.EventLoopThread;

public abstract class AbstractSelectorLoop implements SelectorLoop {

	private Logger				logger			= LoggerFactory.getLogger(AbstractSelectorLoop.class);
	private boolean			working			= false;
	private boolean			shutdown			= false;

	protected boolean			isMainSelector		= false;
	protected Selector			selector			= null;
	protected BaseContext		context			= null;
	protected ChannelFlusher	channelFlusher		= null;
	protected EventLoopThread	channelFlushThread	= null;
	protected SelectableChannel	selectableChannel	= null;
	protected ByteBufAllocator	byteBufAllocator	= null;

	protected AbstractSelectorLoop(BaseContext context, SelectableChannel selectableChannel) {

		this.context = context;

		this.selectableChannel = selectableChannel;

		this.channelFlusher = new ChannelFlusherImpl(context);

		this.byteBufAllocator = context.getMcByteBufAllocator().getNextBufAllocator();

		this.channelFlushThread = new EventLoopThread(channelFlusher, channelFlusher.toString());
	}

	public void loop() {

		try {
			working = true;

			if (shutdown) {
				working = false;
				return;
			}

			Selector selector = this.selector;

			long last_select = System.currentTimeMillis();

			// FIXME 这里select(big number) 比如60s的话会停顿60秒且有数据进来
			int selected = selector.select(16);

			if (selected < 1) {
				
				selectEmpty(last_select);
				
				return;
			}

			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			
			for(SelectionKey key: selectionKeys){
				
				accept(key);
			}

			selectionKeys.clear();

			working = false;

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			working = false;
		}
	}
	
	private void selectEmpty(long last_select){
		
		long past = System.currentTimeMillis() - last_select;
		
		if (past < 16) {

			if (shutdown || past < 0) {
				working = false;
				return;
			}

			// JDK bug fired ?
			IOException e = new IOException("JDK bug fired ?");
			logger.error(e.getMessage(), e);
			logger.debug("last={},past={}", last_select, past);
			this.selector = rebuildSelector();
		}

		working = false;
		
	}
	
	public abstract void accept(SelectionKey key);

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

	protected void cancelSelectionKey(SelectionKey selectionKey, Throwable t) {

		cancelSelectionKey(selectionKey);

		logger.error(t.getMessage(), t);
	}

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

				} else if (ch instanceof SocketChannel) {

					SocketAddress r = ((SocketChannel) ch).getRemoteAddress();
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

	public void startup() throws IOException {

		this.channelFlushThread.startup();

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

		LifeCycleUtil.stop(channelFlushThread);
	}

	public Selector getSelector() {
		return selector;
	}

	public ChannelFlusher getChannelFlusher() {
		return channelFlusher;
	}

	public BaseContext getContext() {
		return context;
	}

	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

	public ByteBufAllocator getByteBufAllocator() {
		return byteBufAllocator;
	}

	public boolean isMainSelector() {
		return isMainSelector;
	}

	public void setMainSelector(boolean isMainSelector) {
		this.isMainSelector = isMainSelector;
	}

}
