package com.generallycloud.nio.component;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.component.concurrent.ExecutorEventLoop;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class CachedAioThread extends Thread implements SocketChannelThreadContext {

	public CachedAioThread(AioSocketChannelContext context, ThreadGroup group, Runnable r,
			String string, int i) {

		super(group, r, string, i);

		this.channelContext = context;
		this.protocolFactory = channelContext.getProtocolFactory();
		this.protocolEncoder = channelContext.getProtocolEncoder();
		this.protocolDecoder = protocolFactory.getProtocolDecoder();

		this.writeCompletionHandler = new WriteCompletionHandler();
		this.executorEventLoop = channelContext.getExecutorEventLoopGroup().getNext();
		this.byteBufAllocator = channelContext.getByteBufAllocatorManager().getNextBufAllocator();
		this.readCompletionHandler = new ReadCompletionHandler(
				channelContext.getChannelByteBufReader());
	}

	private ProtocolDecoder			protocolDecoder		= null;

	private ProtocolEncoder			protocolEncoder		= null;

	private ProtocolFactory			protocolFactory		= null;

	private ExecutorEventLoop		executorEventLoop		= null;

	private AioSocketChannelContext	channelContext			= null;

	private ByteBufAllocator			byteBufAllocator		= null;

	private ReadCompletionHandler		readCompletionHandler	= null;

	private WriteCompletionHandler	writeCompletionHandler	= null;

	@Override
	public AioSocketChannelContext getChannelContext() {
		return channelContext;
	}

	@Override
	public ByteBufAllocator getByteBufAllocator() {
		return byteBufAllocator;
	}

	@Override
	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	@Override
	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	@Override
	public ExecutorEventLoop getExecutorEventLoop() {
		return executorEventLoop;
	}

	@Override
	public boolean inEventLoop() {
		return Thread.currentThread() == this;
	}

	public ReadCompletionHandler getReadCompletionHandler() {
		return readCompletionHandler;
	}

	public WriteCompletionHandler getWriteCompletionHandler() {
		return writeCompletionHandler;
	}

}
