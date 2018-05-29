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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLHandshakeException;

import com.generallycloud.baseio.acceptor.ChannelAcceptor;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.ClassUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.AbstractEventLoop;
import com.generallycloud.baseio.concurrent.BufferedArrayList;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.concurrent.FixedAtomicInteger;
import com.generallycloud.baseio.concurrent.LineEventLoop;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.connector.AbstractChannelConnector;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolCodec;
import com.generallycloud.baseio.protocol.SslFuture;

/**
 * @author wangkai
 *
 */
//FIXME 使用ThreadLocal
public class SelectorEventLoop extends AbstractEventLoop implements ChannelThreadContext {

    private static final Logger                  logger     = LoggerFactory.getLogger(SelectorEventLoop.class);
    private ByteBufAllocator                     allocator;
    private Map<Object, Object>                  attributes = new HashMap<>();
    private ByteBuf                              buf;
    protected ChannelBuilder                     channelBuilder;
    private NioSocketChannelContext              context;
    private SelectorEventLoopGroup               eventLoopGroup;
    private BufferedArrayList<SelectorLoopEvent> events     = new BufferedArrayList<>();
    private ExecutorEventLoop                    executorEventLoop;
    private final ForeFutureAcceptor             foreFutureAcceptor;
    private volatile boolean                     hasTask    = false;
    private final int                            index;
    private IoEventHandleAdaptor                 ioEventHandle;
    private final boolean                        isEnableSsl;
    private AtomicBoolean                        selecting  = new AtomicBoolean();
    private SelectionKeySet                      selectionKeySet;
    private Selector                             selector;
    private SocketSessionManager                 sessionManager;
    private SslHandler                           sslHandler;
    private SslFuture                            sslTemporary;
    private AtomicBoolean                        wakener    = new AtomicBoolean();      // true eventLooper, false offerer
    private ByteBuffer[]                         writeBuffers;

    SelectorEventLoop(SelectorEventLoopGroup group, int index) {
        this.index = index;
        this.eventLoopGroup = group;
        this.context = group.getChannelContext();
        this.ioEventHandle = context.getIoEventHandleAdaptor();
        this.executorEventLoop = context.getExecutorEventLoopGroup().getNext();
        this.sessionManager = new NioSocketSessionManager(context);
        this.foreFutureAcceptor = context.getForeFutureAcceptor();
        this.allocator = context.getByteBufAllocatorManager().getNextBufAllocator();
        this.isEnableSsl = context.isEnableSsl();
        if (isEnableSsl) {
            this.sslHandler = context.getSslContext().newSslHandler();
        }
    }

    private void accept(SelectionKey k) {
        if (!k.isValid()) {
            return;
        }
        NioSocketChannel ch = (NioSocketChannel) k.attachment();
        if (ch == null) {
            // channel为空说明该链接未打开
            try {
                buildChannel(this, k);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            return;
        }
        if (!ch.isOpened()) {
            return;
        }
        if (k.isWritable()) {
            try {
                ch.write(this);
            } catch (Throwable e) {
                closeSocketChannel(ch, e);
            }
            return;
        }
        try {
            ByteBuf buf = this.buf;
            buf.clear();
            if (!isEnableSsl) {
                ByteBuf remainingBuf = ch.getRemainingBuf();
                if (remainingBuf != null) {
                    buf.read(remainingBuf);
                }
            }
            java.nio.channels.SocketChannel javaChannel = ch.javaChannel();
            int length = javaChannel.read(buf.nioBuffer());
            if (length < 1) {
                if (length == -1) {
                    CloseUtil.close(ch);
                }
                return;
            }
            buf.reverse();
            buf.flip();
            ch.active();
            if (isEnableSsl) {
                for (;;) {
                    if (!buf.hasRemaining()) {
                        return;
                    }
                    SslFuture future = ch.getSslReadFuture();
                    boolean setFutureNull = true;
                    if (future == null) {
                        future = this.sslTemporary.reset();
                        setFutureNull = false;
                    }
                    if (!future.read(ch, buf)) {
                        if (!setFutureNull) {
                            if (future == this.sslTemporary) {
                                future = future.copy(ch);
                            }
                            ch.setSslReadFuture(future);
                        }
                        return;
                    }
                    if (setFutureNull) {
                        ch.setSslReadFuture(null);
                    }
                    SslHandler sslHandler = ch.getSslHandler();
                    ByteBuf product;
                    try {
                        product = sslHandler.unwrap(ch, future.getByteBuf());
                    } finally {
                        ReleaseUtil.release(future, ch.getChannelThreadContext());
                    }
                    if (product == null) {
                        continue;
                    }
                    accept(ch, product);
                }
            } else {
                accept(ch, buf);
            }
        } catch (Throwable e) {
            if (e instanceof SSLHandshakeException) {
                finishConnect(ch.getSession(), e);
            }
            closeSocketChannel(ch, e);
        }
    }

    private void accept(SocketChannel ch, ByteBuf buffer) throws Exception {
        ProtocolCodec codec = ch.getProtocolCodec();
        for (;;) {
            if (!buffer.hasRemaining()) {
                return;
            }
            ChannelFuture future = ch.getReadFuture();
            boolean setFutureNull = true;
            if (future == null) {
                future = codec.decode(ch, buffer);
                setFutureNull = false;
            }
            try {
                if (!future.read(ch, buffer)) {
                    if (!setFutureNull) {
                        ch.setReadFuture(future);
                    }
                    ByteBuf remainingBuf = ch.getRemainingBuf();
                    if (remainingBuf != null) {
                        remainingBuf.release(remainingBuf.getReleaseVersion());
                        ch.setRemainingBuf(null);
                    }
                    if (buffer.hasRemaining()) {
                        ByteBuf remaining = allocator.allocate(buffer.remaining());
                        remaining.read(buffer);
                        remaining.flip();
                        ch.setRemainingBuf(remaining);
                    }
                    return;
                }
            } catch (Throwable e) {
                future.release(ch.getChannelThreadContext());
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException("exception occurred when do decode," + e.getMessage(), e);
            }
            if (setFutureNull) {
                ch.setReadFuture(null);
            }
            future.release(ch.getChannelThreadContext());
            foreFutureAcceptor.accept(ch.getSession(), future);
        }
    }

    @Override
    public ByteBufAllocator allocator() {
        return allocator;
    }

    public void buildChannel(SelectorEventLoop eventLoop, SelectionKey k) throws IOException {
        channelBuilder.buildChannel(eventLoop, k);
    }

    @Override
    public void clearAttributes() {
        this.attributes.clear();
    }

    public void close() throws IOException {
        CloseUtil.close(selector);
    }

    private void closeEvents(BufferedArrayList<SelectorLoopEvent> events) {
        for (SelectorLoopEvent event : events.getBuffer()) {
            CloseUtil.close(event);
        }
    }

    private void closeSocketChannel(SocketChannel channel, Throwable t) {
        logger.error(t.getMessage() + " channel:" + channel, t);
        CloseUtil.close(channel);
    }

    public void dispatch(SelectorLoopEvent event) {
        //FIXME 找出这里出问题的原因
        if (inEventLoop()) {
            if (!isRunning()) {
                CloseUtil.close(event);
                return;
            }
            handleEvent(event);
            return;
        }
        if (!isRunning()) {
            CloseUtil.close(event);
            return;
        }
        events.offer(event);

        /* ----------------------------------------------------------------- */
        // 这里不需要再次判断了，因为close方法会延迟执行，
        // 可以确保event要么被执行，要么被close
        //        if (!isRunning()) {
        //            CloseUtil.close(event);
        //            return;
        //        }
        /* ----------------------------------------------------------------- */

        wakeup();
    }

    @Override
    protected void doStartup() throws IOException {
        if (executorEventLoop instanceof LineEventLoop) {
            ((LineEventLoop) executorEventLoop).setMonitor(this);
        }
        Configuration cfg = context.getConfiguration();
        int readBuffer = cfg.getChannelReadBuffer();
        this.writeBuffers = new ByteBuffer[cfg.getWriteBuffers()];
        this.buf = UnpooledByteBufAllocator.getDirect().allocate(readBuffer);
        if (isEnableSsl) {
            ByteBuf buf = UnpooledByteBufAllocator.getHeap().allocate(1024 * 64);
            this.sslTemporary = new SslFuture(buf, 1024 * 64);
        }
        this.rebuildSelector();
    }

    @Override
    protected void doStop() {
        ThreadUtil.sleep(8);
        closeEvents(events);
        closeEvents(events);
        sessionManager.stop();
        CloseUtil.close(selector);
        ReleaseUtil.release(sslTemporary, this);
        ReleaseUtil.release(buf, buf.getReleaseVersion());
    }

    public void finishConnect(UnsafeSocketSession session, Throwable e) {
        ChannelService service = context.getChannelService();
        if (service instanceof AbstractChannelConnector) {
            ((AbstractChannelConnector) service).finishConnect(session, e);
        }
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
    public NioSocketChannelContext getChannelContext() {
        return context;
    }

    @Override
    public SelectorEventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public ExecutorEventLoop getExecutorEventLoop() {
        return executorEventLoop;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public IoEventHandle getIoEventHandle() {
        return ioEventHandle;
    }

    @Override
    public SocketSessionManager getSocketSessionManager() {
        return sessionManager;
    }

    @Override
    public SslHandler getSslHandler() {
        return sslHandler;
    }

    @Override
    public SslFuture getSslTemporary() {
        return sslTemporary;
    }

    public ByteBuffer[] getWriteBuffers() {
        return writeBuffers;
    }

    private void handleEvent(SelectorLoopEvent event) {
        try {
            event.fireEvent(this);
        } catch (Throwable e) {
            CloseUtil.close(event);
        }
    }

    @Override
    public boolean isEnableSsl() {
        return isEnableSsl;
    }

    @Override
    public void loop() {
        final long idle = context.getConfiguration().getSessionIdleTime();
        final Selector selector = this.selector;
        long nextIdle = 0;
        long selectTime = idle;
        for (;;) {
            if (!running) {
                stopped = true;
                return;
            }
            try {
                int selected;
                if (hasTask) {
                    selected = selector.selectNow();
                    hasTask = false;
                } else {
                    if (selecting.compareAndSet(false, true)) {
                        // Im not sure selectorLoopEvent.size if visible immediately by other thread ?
                        // can we use selectorLoopEvents.getBufferSize() > 0 ?
                        if (hasTask) {
                            selected = selector.selectNow();
                        } else {
                            // FIXME try
                            selected = selector.select(selectTime);
                        }
                        hasTask = false;
                        selecting.set(false);
                    } else {
                        selected = selector.selectNow();
                        hasTask = false;
                    }
                }
                if (selected > 0) {
                    if (selectionKeySet != null) {
                        SelectionKeySet keySet = selectionKeySet;
                        for (int i = 0; i < keySet.size; i++) {
                            SelectionKey k = keySet.keys[i];
                            keySet.keys[i] = null;
                            accept(k);
                        }
                        selectionKeySet.reset();
                    } else {
                        Set<SelectionKey> sks = selector.selectedKeys();
                        for (SelectionKey k : sks) {
                            accept(k);
                        }
                        sks.clear();
                    }
                } else {
                    //          selectEmpty(last_select);
                }
                if (events.size() > 0) {
                    List<SelectorLoopEvent> es = events.getBuffer();
                    for (int i = 0; i < es.size(); i++) {
                        handleEvent(es.get(i));
                    }
                }
                long now = System.currentTimeMillis();
                if (now >= nextIdle) {
                    sessionManager.sessionIdle(now);
                    nextIdle = now + idle;
                    selectTime = idle;
                } else {
                    selectTime = nextIdle - now;
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private ChannelBuilder newChannelBuilder(SelectorEventLoop selectorEventLoop,
            SelectableChannel channel) {
        NioSocketChannelContext context = selectorEventLoop.getChannelContext();
        if (context.getChannelService() instanceof ChannelAcceptor) {
            return new AcceptorChannelBuilder((ServerSocketChannel) channel,
                    selectorEventLoop.getEventLoopGroup());
        } else {
            return new ConnectorChannelBuilder((java.nio.channels.SocketChannel) channel);
        }
    }

    @SuppressWarnings("rawtypes")
    private Selector openSelector(SelectableChannel channel) throws IOException {
        SelectorProvider provider = SelectorProvider.provider();
        Object res = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    return Class.forName("sun.nio.ch.SelectorImpl");
                } catch (Throwable cause) {
                    return cause;
                }
            }
        });
        final Selector selector = provider.openSelector();
        this.channelBuilder = newChannelBuilder(this, channel);
        if (res instanceof Throwable) {
            return selector;
        }
        final Class selectorImplClass = (Class) res;
        final SelectionKeySet keySet = new SelectionKeySet();
        res = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
                    Field publicSelectedKeysField = selectorImplClass
                            .getDeclaredField("publicSelectedKeys");

                    Throwable cause = ClassUtil.trySetAccessible(selectedKeysField);
                    if (cause != null) {
                        return cause;
                    }
                    cause = ClassUtil.trySetAccessible(publicSelectedKeysField);
                    if (cause != null) {
                        return cause;
                    }

                    selectedKeysField.set(selector, keySet);
                    publicSelectedKeysField.set(selector, keySet);
                    return null;
                } catch (Exception e) {
                    return e;
                }
            }
        });
        if (res instanceof Throwable) {
            return selector;
        }
        selectionKeySet = keySet;
        return selector;
    }

    private void rebuildSelector() throws IOException {
        Selector oldSelector = this.selector;
        Selector newSelector = rebuildSelector0();
        if (oldSelector != null) {
            Selector oldSel = this.selector;
            Selector newSel = newSelector;
            Set<SelectionKey> sks = oldSel.keys();
            for (SelectionKey sk : sks) {
                if (!sk.isValid() || sk.attachment() == null) {
                    continue;
                }
                try {
                    sk.channel().register(newSel, SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    Object atta = sk.attachment();
                    if (atta instanceof Closeable) {
                        CloseUtil.close((Closeable) atta);
                    }
                }

            }
            CloseUtil.close(oldSelector);
        }
        this.selector = newSelector;
    }

    private Selector rebuildSelector0() throws IOException {
        NioSocketChannelContext context = getChannelContext();
        SelectableChannel channel = context.getSelectableChannel();
        Selector selector = openSelector(channel);
        if (context.getChannelService() instanceof ChannelAcceptor) {
            //FIXME 使用多eventLoop accept是否导致卡顿 是否要区分accept和read
            channel.register(selector, SelectionKey.OP_ACCEPT);
        } else {
            channel.register(selector, SelectionKey.OP_CONNECT);
        }
        return selector;
    }
    
    
    
    protected NioSocketChannel regist(java.nio.channels.SocketChannel channel,
            SelectorEventLoop selectorLoop, int channelId) throws IOException {
        Selector nioSelector = selectorLoop.selector;
        SelectionKey sk = channel.register(nioSelector, SelectionKey.OP_READ);
        // 绑定SocketChannel到SelectionKey
        NioSocketChannel socketChannel = (NioSocketChannel) sk.attachment();
        if (socketChannel != null) {
            return socketChannel;
        }
        socketChannel = new NioSocketChannel(selectorLoop, sk, channelId);
        sk.attach(socketChannel);
        // fire session open event
        socketChannel.fireOpend();
        return socketChannel;
    }

    @Override
    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    protected void selectEmpty(long last_select) {
        long past = System.currentTimeMillis() - last_select;
        if (past > 0 || !isRunning()) {
            return;
        }
        // JDK bug fired ?
        IOException be = new IOException("JDK bug fired ?");
        logger.error(be.getMessage(), be);
        logger.info("last={},past={}", last_select, past);
        try {
            rebuildSelector();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }

    // FIXME 会不会出现这种情况，数据已经接收到本地，但是还没有被EventLoop处理完
    // 执行stop的时候如果确保不会再有数据进来
    @Override
    public void wakeup() {
        if (wakener.compareAndSet(false, true)) {
            hasTask = true;
            if (selecting.compareAndSet(false, true)) {
                selecting.set(false);
            } else {
                selector.wakeup();
                super.wakeup();
            }
            wakener.set(false);
        }
    }

    class AcceptorChannelBuilder implements ChannelBuilder {

        private FixedAtomicInteger     channelIdGenerator;
        private SelectorEventLoopGroup eventLoopGroup;
        private ServerSocketChannel    serverSocketChannel;

        public AcceptorChannelBuilder(ServerSocketChannel serverSocketChannel,
                SelectorEventLoopGroup selectorEventLoopGroup) {
            this.serverSocketChannel = serverSocketChannel;
            this.eventLoopGroup = selectorEventLoopGroup;
            this.channelIdGenerator = context.getChannelIds();
        }

        public void buildChannel(SelectorEventLoop eventLoop, SelectionKey k) throws IOException {
            if (serverSocketChannel.getLocalAddress() == null) {
                return;
            }
            final java.nio.channels.SocketChannel channel = serverSocketChannel.accept();
            if (channel == null) {
                return;
            }
            final int channelId = channelIdGenerator.getAndIncrement();
            int eventLoopIndex = channelId % eventLoopGroup.getEventLoopSize();
            SelectorEventLoop targetEventLoop = eventLoopGroup.getEventLoop(eventLoopIndex);
            // 配置为非阻塞
            channel.configureBlocking(false);
            // 注册到selector，等待连接
            if (eventLoop == targetEventLoop) {
                regist(channel, targetEventLoop, channelId);
                return;
            }
            targetEventLoop.dispatch(new SelectorLoopEvent() {

                public void close() throws IOException {}

                public void fireEvent(SelectorEventLoop selectLoop) throws IOException {
                    regist(channel, selectLoop, channelId);
                }
            });
            targetEventLoop.wakeup();
        }

    }

    interface ChannelBuilder {
        void buildChannel(SelectorEventLoop eventLoop, SelectionKey k) throws IOException;
    }

    class ConnectorChannelBuilder implements ChannelBuilder {

        private java.nio.channels.SocketChannel jdkChannel;

        public ConnectorChannelBuilder(java.nio.channels.SocketChannel jdkChannel) {
            this.jdkChannel = jdkChannel;
        }

        public void buildChannel(SelectorEventLoop eventLoop, SelectionKey k) throws IOException {
            try {
                if (!jdkChannel.isConnectionPending()) {
                    return;
                }
                if (!jdkChannel.finishConnect()) {
                    throw new IOException("connect failed");
                }
                SocketChannel channel = regist(jdkChannel, eventLoop, 1);
                if (channel.isEnableSsl()) {
                    return;
                }
                finishConnect(channel.getSession(), null);
            } catch (IOException e) {
                finishConnect(null, e);
            }
        }
    }

    class SelectionKeySet extends AbstractSet<SelectionKey> {

        SelectionKey[] keys;
        int            size;

        SelectionKeySet() {
            keys = new SelectionKey[1024];
        }

        @Override
        public boolean add(SelectionKey o) {
            keys[size++] = o;
            if (size == keys.length) {
                increaseCapacity();
            }
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        private void increaseCapacity() {
            SelectionKey[] newKeys = new SelectionKey[keys.length << 1];
            System.arraycopy(keys, 0, newKeys, 0, size);
            keys = newKeys;
        }

        @Override
        public Iterator<SelectionKey> iterator() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        void reset() {
            size = 0;
        }

        @Override
        public int size() {
            return size;
        }
    }

}
