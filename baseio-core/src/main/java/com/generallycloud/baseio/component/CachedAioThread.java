package com.generallycloud.baseio.component;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;

public class CachedAioThread extends Thread implements SocketChannelThreadContext {

    public CachedAioThread(AioSocketChannelContext context, ThreadGroup group, Runnable r,
            String string, int i) {

        super(group, r, string, i);

        this.channelContext = context;
        this.writeCompletionHandler = new WriteCompletionHandler();
        this.executorEventLoop = channelContext.getExecutorEventLoopGroup().getNext();
        this.byteBufAllocator = channelContext.getByteBufAllocatorManager().getNextBufAllocator();
        this.readCompletionHandler = new ReadCompletionHandler(
                channelContext.getChannelByteBufReader());
        if (context.isEnableSSL()) {
            sslHandler = context.getSslContext().newSslHandler(context);
        }
    }

    private ExecutorEventLoop       executorEventLoop      = null;

    private AioSocketChannelContext channelContext         = null;

    private ByteBufAllocator        byteBufAllocator       = null;

    private ReadCompletionHandler   readCompletionHandler  = null;

    private WriteCompletionHandler  writeCompletionHandler = null;

    private SslHandler              sslHandler             = null;

    @Override
    public AioSocketChannelContext getChannelContext() {
        return channelContext;
    }

    @Override
    public ByteBufAllocator getByteBufAllocator() {
        return byteBufAllocator;
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

    @Override
    public SslHandler getSslHandler() {
        return sslHandler;
    }

    @Override
    public SocketSessionManager getSocketSessionManager() {
        return channelContext.getSessionManager();
    }

}
