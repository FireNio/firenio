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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLHandshakeException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.collection.Attributes;
import com.generallycloud.baseio.collection.IntObjectHashMap;
import com.generallycloud.baseio.common.ClassUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.MessageFormatter;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.AbstractEventLoop;
import com.generallycloud.baseio.concurrent.BufferedArrayList;
import com.generallycloud.baseio.concurrent.Waiter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.SslFuture;

/**
 * @author wangkai
 *
 */
//FIXME 使用ThreadLocal
public final class NioEventLoop extends AbstractEventLoop implements Attributes {

    private static final AtomicInteger indexedVariablesIndex   = new AtomicInteger(0);
    private static final Logger        logger                  = LoggerFactory
            .getLogger(NioEventLoop.class);
    private static final int           maxIndexedVariablesSize = 16;

    public static int nextIndexedVariablesIndex() {
        if (indexedVariablesIndex.get() >= maxIndexedVariablesSize) {
            return -1;
        }
        int index = indexedVariablesIndex.getAndIncrement();
        if (index >= maxIndexedVariablesSize) {
            return -1;
        }
        return index;
    }

    private ByteBufAllocator                    allocator;
    private Map<Object, Object>                 attributes       = new HashMap<>();
    private ByteBuf                             buf;
    private IntObjectHashMap<NioSocketChannel>  channels         = new IntObjectHashMap<>();
    private final int                           channelSizeLimit = 1024 * 64;
    private Map<Charset, CharsetDecoder>        charsetDecoders  = new IdentityHashMap<>();
    private Map<Charset, CharsetEncoder>        charsetEncoders  = new IdentityHashMap<>();
    private ChannelContext                      context;                                               // use when not sharable 
    private String                              desc;
    private BufferedArrayList<NioEventLoopTask> events           = new BufferedArrayList<>();
    private NioEventLoopGroup                   group;
    private volatile boolean                    hasTask          = false;
    private final int                           index;
    private Object[]                            indexedVariables = new Object[maxIndexedVariablesSize];
    private final boolean                       isAcceptor;
    private long                                lastIdleTime     = 0;
    private AtomicBoolean                       selecting        = new AtomicBoolean();
    private SelectionKeySet                     selectionKeySet;
    private Selector                            selector;
    private boolean                             selectorRegisted;
    private final boolean                       sharable;
    private SslHandler                          sslHandler;
    private SslFuture                           sslTemporary;
    private AtomicBoolean                       wakener          = new AtomicBoolean();                // true eventLooper, false offerer
    private ByteBuffer[]                        writeBuffers;

    NioEventLoop(NioEventLoopGroup group, int index, boolean isAcceptor) {
        if (!group.isSharable()) {
            this.context = group.getContext();
        }
        this.index = index;
        this.group = group;
        this.isAcceptor = isAcceptor;
        this.sharable = group.isSharable();
        this.allocator = group.getAllocatorGroup().getNext();
    }

    private void accept(SelectionKey k) {
        if (!k.isValid()) {
            k.cancel();
            return;
        }
        int readyOps = k.readyOps();
        if (sharable) {
            if (isAcceptor) {
                if ((readyOps & SelectionKey.OP_ACCEPT) != 0
                        || (readyOps & SelectionKey.OP_CONNECT) != 0) {
                    // 说明该链接未打开
                    try {
                        registChannel(k);
                    } catch (Exception e) {
                        k.channel();
                        k.attach(null);
                        logger.error(e.getMessage(), e);
                    }
                    return;
                }
            } else {
                NioSocketChannel ch = (NioSocketChannel) k.attachment();
                if (ch == null || !ch.isOpened()) {
                    return;
                }
                if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                    try {
                        ch.write();
                    } catch (Throwable e) {
                        closeSocketChannel(ch, e);
                    }
                    return;
                }
                try {
                    ch.read(buf);
                } catch (Throwable e) {
                    if (e instanceof SSLHandshakeException) {
                        finishRegistChannel(ch, e);
                    }
                    closeSocketChannel(ch, e);
                }
            }
        } else {
            if ((readyOps & SelectionKey.OP_CONNECT) != 0
                    || (readyOps & SelectionKey.OP_ACCEPT) != 0) {
                // 说明该链接未打开
                try {
                    registChannel(k);
                } catch (Exception e) {
                    k.channel();
                    k.attach(null);
                    logger.error(e.getMessage(), e);
                }
                return;
            }
            NioSocketChannel ch = (NioSocketChannel) k.attachment();
            if (ch == null || !ch.isOpened()) {
                return;
            }
            if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                try {
                    ch.write();
                } catch (Throwable e) {
                    closeSocketChannel(ch, e);
                }
                return;
            }
            try {
                ch.read(buf);
            } catch (Throwable e) {
                if (e instanceof SSLHandshakeException) {
                    finishRegistChannel(ch, e);
                }
                closeSocketChannel(ch, e);
            }
        }
    }

    public ByteBufAllocator allocator() {
        return allocator;
    }

    @Override
    public Map<Object, Object> attributes() {
        return attributes;
    }

    private void channelIdle(long currentTime) {
        long lastIdleTime = this.lastIdleTime;
        this.lastIdleTime = currentTime;
        IntObjectHashMap<NioSocketChannel> channels = this.channels;
        if (channels.size() == 0) {
            return;
        }
        if (sharable) {
            for (NioSocketChannel channel : channels.values()) {
                ChannelContext context = channel.getContext();
                List<ChannelIdleEventListener> ls = context.getChannelIdleEventListeners();
                if (ls.size() == 1) {
                    try {
                        ls.get(0).channelIdled(channel, lastIdleTime, currentTime);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    for (ChannelIdleEventListener l : ls) {
                        try {
                            l.channelIdled(channel, lastIdleTime, currentTime);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        } else {
            List<ChannelIdleEventListener> ls = context.getChannelIdleEventListeners();
            for (ChannelIdleEventListener l : ls) {
                for (NioSocketChannel channel : channels.values()) {
                    l.channelIdled(channel, lastIdleTime, currentTime);
                }
            }
        }
    }

    @Override
    public void clearAttributes() {
        this.attributes.clear();
    }

    public void close() throws IOException {
        CloseUtil.close(selector);
    }

    private void closeChannels() {
        for (NioSocketChannel channel : channels.values()) {
            CloseUtil.close(channel);
        }
    }

    private void closeEvents(BufferedArrayList<NioEventLoopTask> events) {
        for (NioEventLoopTask event : events.getBuffer()) {
            if (event instanceof Closeable) {
                CloseUtil.close((Closeable) event);
            } else {
                try {
                    event.fireEvent(this);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private void closeSocketChannel(NioSocketChannel channel, Throwable t) {
        logger.error(t.getMessage() + " channel:" + channel, t);
        CloseUtil.close(channel);
    }

    protected final void dispatch(NioEventLoopTask event) {
        if (!isRunning()) {
            throw new RejectedExecutionException();
        }
        /* ----------------------------------------------------------------- */
        // 这里不需要再次判断了，因为close方法会延迟执行，
        // 可以确保event要么被执行，要么被close
        /* ----------------------------------------------------------------- */
        events.offer(event);
        wakeup();
    }

    public final void dispatchAfterLoop(NioEventLoopTask event) {
        if (inEventLoop()) {
            events.unsafeOffer(event);
        } else {
            if (!isRunning()) {
                throw new RejectedExecutionException();
            }
            events.offer(event);
        }
    }

    @Override
    protected void doStartup() throws IOException {
        this.writeBuffers = new ByteBuffer[group.getWriteBuffers()];
        this.buf = UnpooledByteBufAllocator.getDirect().allocate(group.getChannelReadBuffer());
        if (group.isEnableSsl()) {
            ByteBuf buf = UnpooledByteBufAllocator.getHeap().allocate(1024 * 64);
            this.sslTemporary = new SslFuture(buf, 1024 * 64);
        }
        this.selector = openSelector();
    }

    @Override
    protected void doStop() {
        ThreadUtil.sleep(8);
        closeEvents(events);
        closeEvents(events);
        closeChannels();
        CloseUtil.close(selector);
        ReleaseUtil.release(sslTemporary, this);
        ReleaseUtil.release(buf, buf.getReleaseVersion());
    }

    public final void finishConnect(ChannelContext context, Throwable e) {
        ChannelService service = context.getChannelService();
        if (service instanceof ChannelConnector) {
            ((ChannelConnector) service).finishConnect(null, e);
        }
    }

    public final void finishRegistChannel(NioSocketChannel channel, Throwable e) {
        ChannelContext context = channel.getContext();
        ChannelService service = context.getChannelService();
        if (service instanceof ChannelConnector) {
            ((ChannelConnector) service).finishConnect(channel, e);
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

    public NioSocketChannel getChannel(int channelId) {
        return channels.get(channelId);
    }

    public CharsetDecoder getCharsetDecoder(Charset charset) {
        CharsetDecoder decoder = charsetDecoders.get(charset);
        if (decoder == null) {
            decoder = charset.newDecoder();
            charsetDecoders.put(charset, decoder);
        }
        return decoder;
    }

    public CharsetEncoder getCharsetEncoder(Charset charset) {
        CharsetEncoder encoder = charsetEncoders.get(charset);
        if (encoder == null) {
            encoder = charset.newEncoder();
            charsetEncoders.put(charset, encoder);
        }
        return encoder;
    }

    @Override
    public NioEventLoopGroup getGroup() {
        return group;
    }

    public int getIndex() {
        return index;
    }

    public Object getIndexedVariable(int index) {
        return indexedVariables[index];
    }

    public Selector getSelector() {
        return selector;
    }

    public SslHandler getSslHandler() {
        return sslHandler;
    }

    public SslFuture getSslTemporary() {
        return sslTemporary;
    }

    public ByteBuffer[] getWriteBuffers() {
        return writeBuffers;
    }

    private void handleEvent(NioEventLoopTask event) {
        try {
            event.fireEvent(this);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void loop() {
        final long idle = group.getIdleTime();
        final Selector selector = this.selector;
        long nextIdle = 0;
        long selectTime = idle;
        for (;;) {
            if (!running) {
                setStopped(true);
                return;
            }
            try {
                int selected;
                if (hasTask) {
                    selected = selector.selectNow();
                    hasTask = false;
                } else {
                    if (selecting.compareAndSet(false, true)) {
                        // Im not sure events.size if visible immediately by other thread ?
                        // can we use events.getBufferSize() > 0 ?
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
                        keySet.reset();
                    } else {
                        Set<SelectionKey> sks = selector.selectedKeys();
                        for (SelectionKey k : sks) {
                            accept(k);
                        }
                        sks.clear();
                    }
                }
                if (events.size() > 0) {
                    List<NioEventLoopTask> es = events.getBuffer();
                    for (int i = 0; i < es.size(); i++) {
                        handleEvent(es.get(i));
                    }
                }
                long now = System.currentTimeMillis();
                if (now >= nextIdle) {
                    channelIdle(now);
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

    @SuppressWarnings("rawtypes")
    private Selector openSelector() throws IOException {
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

    protected void putChannel(NioSocketChannel channel) throws RejectedExecutionException {
        IntObjectHashMap<NioSocketChannel> channels = this.channels;
        Integer channelId = channel.getChannelId();
        NioSocketChannel old = channels.get(channelId);
        if (old != null) {
            CloseUtil.close(old);
        }
        if (channels.size() >= channelSizeLimit) {
            throw new RejectedExecutionException(
                    "channel size limit:" + channelSizeLimit + ",current:" + channels.size());
        }
        channels.put(channelId.intValue(), channel);
        channel.getContext().getChannelManager().putChannel(channel);
    }

    public void registChannel(SelectionKey k) throws IOException {
        final ChannelContext context = (ChannelContext) k.attachment();
        final ChannelService channelService = context.getChannelService();
        final NioEventLoop thisEventLoop = this;
        final int channelId = group.getChannelIds().getAndIncrement();
        if (channelService instanceof ChannelAcceptor) {
            ChannelAcceptor acceptor = (ChannelAcceptor) channelService;
            ServerSocketChannel serverChannel = acceptor.getSelectableChannel();
            //有时候还未regist selector，但是却能selector到sk
            //如果getLocalAddress为空则不处理该sk
            if (serverChannel.getLocalAddress() == null) {
                return;
            }
            final SocketChannel channel = serverChannel.accept();
            if (channel == null) {
                return;
            }
            NioEventLoop targetEventLoop = group.getNext();
            // 配置为非阻塞
            channel.configureBlocking(false);
            // 注册到selector，等待连接
            if (thisEventLoop == targetEventLoop) {
                registChannel(channel, targetEventLoop, context, channelId);
            } else {
                targetEventLoop.dispatch(new NioEventLoopTask() {

                    @Override
                    public void fireEvent(NioEventLoop eventLoop) throws IOException {
                        registChannel(channel, eventLoop, context, channelId);
                    }
                });
            }
        } else {
            @SuppressWarnings("resource")
            final ChannelConnector connector = (ChannelConnector) channelService;
            final SocketChannel javaChannel = connector.getSelectableChannel();
            try {
                if (!javaChannel.isConnectionPending()) {
                    return;
                }
                if (!javaChannel.finishConnect()) {
                    throw new IOException("connect failed");
                }
                NioEventLoop targetEL = connector.getEventLoop();
                if (targetEL == null) {
                    targetEL = group.getEventLoop(0);
                }
                if (sharable) {
                    //我也不知道为什么要这么做，如果不这么做当eventLoopGroup为共享时
                    //且acceptor eventLoop同时注册了accept和connect时，selector.select()会
                    //立刻返回但是selected却为0，导致CPU100%,如果你知道是什么原因，
                    //还请发起pr，或者邮件我都可以，感谢！
                    SelectionKey sk = javaChannel.keyFor(selector);
                    if (sk != null) {
                        sk.cancel();
                    }
                }
                if (thisEventLoop == targetEL) {
                    registChannel(javaChannel, targetEL, context, channelId);
                } else {
                    targetEL.dispatch(new NioEventLoopTask() {
                        @Override
                        public void fireEvent(NioEventLoop eventLoop) throws IOException {
                            registChannel(javaChannel, eventLoop, context, channelId);
                        }
                    });
                }
            } catch (IOException e) {
                finishConnect(context, e);
            }
        }
    }

    private NioSocketChannel registChannel(SocketChannel javaChannel, NioEventLoop eventLoop,
            ChannelContext context, int channelId) throws IOException {
        SelectionKey sk = javaChannel.register(eventLoop.selector, SelectionKey.OP_READ);
        // 绑定SocketChannel到SelectionKey
        NioSocketChannel channel = (NioSocketChannel) sk.attachment();
        if (channel != null) {
            CloseUtil.close(channel);
        }
        channel = new NioSocketChannel(eventLoop, sk, context, channelId);
        sk.attach(channel);
        // fire channel open event
        channel.fireOpend();
        if (!channel.isEnableSsl()) {
            finishRegistChannel(channel, null);
        }
        return channel;
    }

    protected void registSelector(final ChannelContext context) throws IOException {
        if (sharable) {
            if (!isAcceptor) {
                throw new IOException("not acceptor event loop");
            }
        } else {
            if (selectorRegisted) {
                throw new IOException("selector registed");
            }
            selectorRegisted = true;
        }
        if (sharable && !isAcceptor) {}
        if (inEventLoop()) {
            registSelector(this, context);
        } else {
            final Waiter waiter = new Waiter();
            dispatch(new NioEventLoopTask() {

                @Override
                public void fireEvent(NioEventLoop eventLoop) throws IOException {
                    try {
                        SelectionKey sk = registSelector(eventLoop, context);
                        waiter.response(sk);
                    } catch (Exception e) {
                        waiter.response(e);
                        throw e;
                    }
                }
            });
            waiter.await();
            Object res = waiter.getResponse();
            if (res instanceof IOException) {
                throw (IOException) res;
            }
        }
        //        if (oldSelector != null) {
        //            Selector oldSel = this.selector;
        //            Selector newSel = newSelector;
        //            Set<SelectionKey> sks = oldSel.keys();
        //            for (SelectionKey sk : sks) {
        //                if (!sk.isValid() || sk.attachment() == null) {
        //                    continue;
        //                }
        //                try {
        //                    sk.channel().register(newSel, SelectionKey.OP_READ);
        //                } catch (ClosedChannelException e) {
        //                    Object atta = sk.attachment();
        //                    if (atta instanceof Closeable) {
        //                        CloseUtil.close((Closeable) atta);
        //                    }
        //                }
        //            }
        //            CloseUtil.close(oldSelector);
        //        }
        //        this.selector = newSelector;
    }

    private SelectionKey registSelector(NioEventLoop eventLoop, ChannelContext context)
            throws IOException {
        ChannelService channelService = context.getChannelService();
        SelectableChannel channel = channelService.getSelectableChannel();
        if (context.isEnableSsl()) {
            eventLoop.sslHandler = context.getSslContext().newSslHandler();
        }
        if (channelService instanceof ChannelAcceptor) {
            //FIXME 使用多eventLoop accept是否导致卡顿 是否要区分accept和read
            return channel.register(eventLoop.selector, SelectionKey.OP_ACCEPT, context);
        } else {
            return channel.register(eventLoop.selector, SelectionKey.OP_CONNECT, context);
        }
    }

    @Override
    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    protected void removeChannel(NioSocketChannel channel) {
        channels.remove(channel.getChannelId());
        channel.getContext().getChannelManager().removeChannel(channel);
    }

    @Override
    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }

    public void setIndexedVariable(int index, Object value) {
        indexedVariables[index] = value;
    }

    @Override
    public String toString() {
        if (desc == null) {
            desc = MessageFormatter.arrayFormat("NioEventLoop(idx:{},sharable:{},isAcceptor:{})",
                    new Object[] { index, sharable, isAcceptor });
        }
        return desc;
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
