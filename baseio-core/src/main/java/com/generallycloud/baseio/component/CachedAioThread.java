package com.generallycloud.baseio.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.protocol.SslFuture;

public class CachedAioThread extends Thread implements ChannelThreadContext {

    private Map<Object, Object>     attributes             = new HashMap<>();
    private ByteBufAllocator        byteBufAllocator       = null;
    private AioSocketChannelContext channelContext         = null;
    private ExecutorEventLoop       executorEventLoop      = null;
    private ReadCompletionHandler   readCompletionHandler  = null;
    private SslHandler              sslHandler             = null;
    private WriteCompletionHandler  writeCompletionHandler = null;
    private boolean                 isEnableSsl;
    private IoEventHandleAdaptor    ioEventHandle;
    private SslFuture               sslTemporary;

    public CachedAioThread(AioSocketChannelContext context, ThreadGroup group, Runnable r,
            String string, int i) {
        super(group, r, string, i);
        this.channelContext = context;
        this.ioEventHandle = context.getIoEventHandleAdaptor();
        this.writeCompletionHandler = new WriteCompletionHandler();
        this.executorEventLoop = channelContext.getExecutorEventLoopGroup().getNext();
        this.byteBufAllocator = channelContext.getByteBufAllocatorManager().getNextBufAllocator();
        this.readCompletionHandler = new ReadCompletionHandler(context.getForeFutureAcceptor());
        if (context.isEnableSsl()) {
            this.isEnableSsl = context.isEnableSsl();
            this.sslHandler = context.getSslContext().newSslHandler();
        }
    }

    @Override
    public void clearAttributes() {
        this.attributes.clear();
    }

    @Override
    public Object getAttribute(Object key) {
        return this.attributes.get(key);
    }

    @Override
    public Set<Object> getAttributeNames() {
        return this.attributes.keySet();
    }

    @Override
    public ByteBufAllocator allocator() {
        return byteBufAllocator;
    }

    @Override
    public AioSocketChannelContext getChannelContext() {
        return channelContext;
    }

    @Override
    public ExecutorEventLoop getExecutorEventLoop() {
        return executorEventLoop;
    }

    @Override
    public IoEventHandle getIoEventHandle() {
        return ioEventHandle;
    }

    public ReadCompletionHandler getReadCompletionHandler() {
        return readCompletionHandler;
    }

    @Override
    public SocketSessionManager getSocketSessionManager() {
        return channelContext.getSessionManager();
    }

    @Override
    public SslHandler getSslHandler() {
        return sslHandler;
    }

    public WriteCompletionHandler getWriteCompletionHandler() {
        return writeCompletionHandler;
    }

    @Override
    public boolean inEventLoop() {
        return Thread.currentThread() == this;
    }

    @Override
    public boolean isEnableSsl() {
        return isEnableSsl;
    }

    @Override
    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    @Override
    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }

    @Override
    public SslFuture getSslTemporary() {
        return sslTemporary;
    }

}
