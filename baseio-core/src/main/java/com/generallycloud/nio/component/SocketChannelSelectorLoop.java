package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.component.concurrent.LineEventLoop;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public abstract class SocketChannelSelectorLoop extends AbstractSelectorLoop {
	
	private	Logger				logger			= LoggerFactory.getLogger(SocketChannelSelectorLoop.class);

	protected ByteBuf				buf				= null;

	protected ChannelByteBufReader	byteBufReader		= null;

	protected SocketChannelContext	context			= null;

	protected ProtocolDecoder		protocolDecoder	= null;

	protected ProtocolEncoder		protocolEncoder	= null;

	protected ProtocolFactory		protocolFactory	= null;

	protected EventLoop			eventLoop			= null;

	public SocketChannelSelectorLoop(ChannelService service, SelectorLoop[] selectorLoops) {

		super(service, selectorLoops);

		this.context = (SocketChannelContext) service.getContext();
		
		this.eventLoop = context.getEventLoopGroup().getNext();

		this.protocolFactory = context.getProtocolFactory();

		this.protocolDecoder = protocolFactory.getProtocolDecoder();

		this.protocolEncoder = protocolFactory.getProtocolEncoder();

		this.selectorLoops = selectorLoops;

		this.byteBufReader = context.getChannelByteBufReader();

		int readBuffer = context.getServerConfiguration().getSERVER_CHANNEL_READ_BUFFER();

		this.buf = UnpooledByteBufAllocator.getInstance().allocate(readBuffer);// FIXME 使用direct
	}

	public void accept(SelectionKey selectionKey) {

		if (!selectionKey.isValid()) {
			cancelSelectionKey(selectionKey);
			return;
		}

		try {

			if (!selectionKey.isReadable()) {

				acceptPrepare(selectionKey);
				return;
			}

			accept((SocketChannel) selectionKey.attachment());

		} catch (Throwable e) {

			cancelSelectionKey(selectionKey, e);
		}

	}

	public void accept(SocketChannel channel) throws Exception {

		if (channel == null || !channel.isOpened()) {
			// 该channel已经被关闭
			return;
		}

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

	protected abstract void acceptPrepare(SelectionKey selectionKey) throws IOException;

	public SocketChannel buildSocketChannel(SelectionKey selectionKey) throws SocketException {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel != null) {

			return channel;
		}

		channel = new NioSocketChannel(this, selectionKey);

		selectionKey.attach(channel);

		return channel;
	}
	
	public void doStartup() throws IOException {
		
		if (eventLoop instanceof LineEventLoop) {
			((LineEventLoop) eventLoop).setMonitor(getMonitor());
		}
		
		super.doStartup();
	}

	protected void doStop() {
		
		synchronized (runLock) {
			selectorLoopStrategy.stop();
		}

		try {
			this.selector.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		ReleaseUtil.release(buf);
	}

	public SocketChannelContext getContext() {
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

	public EventLoop getEventLoop() {
		return eventLoop;
	}

}
