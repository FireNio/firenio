package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

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

	protected Selector			selector			= null;
	protected BaseContext		context			= null;
	protected ChannelFlusher	channelFlusher		= null;
	protected EventLoopThread	channelFlushThread	= null;
	protected SelectableChannel	selectableChannel	= null;

	protected AbstractSelectorLoop(BaseContext context,SelectableChannel	selectableChannel) {

		this.context = context;

		this.selectableChannel = selectableChannel;
		
		this.channelFlusher = new ChannelFlusherImpl(context);

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

			int selected = selector.select(64);

			if (selected < 1) {
				
				if (System.currentTimeMillis() == last_select) {
					//JDK bug fired
					this.selector = rebuildSelector();
				}

				working = false;

				return;
			}

			Set<SelectionKey> selectionKeys = selector.selectedKeys();

			Iterator<SelectionKey> iterator = selectionKeys.iterator();

			for (; iterator.hasNext();) {

				SelectionKey selectionKey = iterator.next();

				iterator.remove();

				accept(selectionKey);
			}

			working = false;

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			working = false;
		}
	}
	
	private Selector rebuildSelector(){
		
		Selector selector;
		try {
			 selector = buildSelector(selectableChannel);
		} catch (IOException e) {
			throw new Error(e);
		}
		
		Selector old = this.selector;
		
		Set<SelectionKey> sks = old.selectedKeys();
		
		if (sks.size() == 0) {
			CloseUtil.close(old);
			return selector;
		}
		
		for (SelectionKey sk : sks) {
			
			if (!sk.isValid()) {
				cancelSelectionKey(sk);
				continue;
			}

			try {
				sk.channel().register(selector, SelectionKey.OP_READ);
			} catch (ClosedChannelException e) {
				logger.error(e.getMessage(),e);
				cancelSelectionKey(sk);
			}
		}
		
		CloseUtil.close(old);
		
		return selector;
	}
	
	private void cancelSelectionKey(SelectionKey sk){
		
		Object attachment = sk.attachment();
		
		if (attachment instanceof Channel) {
			CloseUtil.close((Channel)attachment);
		}
	}

	public void startup() throws IOException {

		this.channelFlushThread.start();

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

}
