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
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLHandshakeException;

import com.generallycloud.baseio.ClosedChannelException;
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
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.SslFuture;

/**
 * @author wangkai
 *
 */
//FIXME 使用ThreadLocal
public class NioEventLoop extends AbstractEventLoop implements Attributes {

    private static final Logger                 logger           = LoggerFactory
            .getLogger(NioEventLoop.class);
    private ByteBufAllocator                    allocator;
    private Map<Object, Object>                 attributes       = new HashMap<>();
    private ByteBuf                             buf;
    private BufferedArrayList<NioEventLoopTask> events           = new BufferedArrayList<>();
    private NioEventLoopGroup                   group;
    private volatile boolean                    hasTask          = false;
    private final int                           index;
    private long                                lastIdleTime     = 0;
    private AtomicBoolean                       selecting        = new AtomicBoolean();
    private SelectionKeySet                     selectionKeySet;
    private Selector                            selector;
    private IntObjectHashMap<SocketSession>     sessions         = new IntObjectHashMap<>();
    private final int                           sessionSizeLimit = 1024 * 64;
    private SslFuture                           sslTemporary;
    private AtomicBoolean                       wakener          = new AtomicBoolean();      // true eventLooper, false offerer
    private ByteBuffer[]                        writeBuffers;
    private List<ChannelFuture>                 readFutures;
    private final boolean                       sharable;
    private ChannelContext                      context;                                     // use when not sharable 
    private final boolean                       isAcceptor;

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
                        finishRegistChannel(ch.getSession(), e);
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
                    finishRegistChannel(ch.getSession(), e);
                }
                closeSocketChannel(ch, e);
            }
        }
    }

    public ByteBufAllocator allocator() {
        return allocator;
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
                    public void close() throws IOException {}

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
                    //立刻返回但是selected却为0，导致CPU100%
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
                        public void close() throws IOException {
                            finishConnect(context,new IOException("closed nio eventloop"));
                        }

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

    @Override
    public void clearAttributes() {
        this.attributes.clear();
    }

    public void close() throws IOException {
        CloseUtil.close(selector);
    }

    private void closeEvents(BufferedArrayList<NioEventLoopTask> events) {
        for (NioEventLoopTask event : events.getBuffer()) {
            CloseUtil.close(event);
        }
    }

    private void closeSessions() {
        for (SocketSession session : sessions.values()) {
            CloseUtil.close(session);
        }
    }

    private void closeSocketChannel(NioSocketChannel channel, Throwable t) {
        logger.error(t.getMessage() + " channel:" + channel, t);
        CloseUtil.close(channel);
    }

    public void dispatch(NioEventLoopTask event) {
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
        this.writeBuffers = new ByteBuffer[group.getWriteBuffers()];
        this.readFutures = new ArrayList<>(group.getReadFutures());
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
        closeSessions();
        CloseUtil.close(selector);
        ReleaseUtil.release(sslTemporary, this);
        ReleaseUtil.release(buf, buf.getReleaseVersion());
    }

    public void finishRegistChannel(SocketSession session, Throwable e) {
        ChannelContext context = session.getContext();
        ChannelService service = context.getChannelService();
        if (service instanceof ChannelConnector) {
            ((ChannelConnector) service).finishConnect(session, e);
        }
    }

    public void finishConnect(ChannelContext context, Throwable e) {
        ChannelService service = context.getChannelService();
        if (service instanceof ChannelConnector) {
            ((ChannelConnector) service).finishConnect(null, e);
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
    public NioEventLoopGroup getGroup() {
        return group;
    }

    public int getIndex() {
        return index;
    }

    public Selector getSelector() {
        return selector;
    }

    public SslHandler getSslHandler() {
        return (SslHandler) attributes.get(SslHandler.SSL_HANDlER_EVENT_LOOP_KEY);
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
            CloseUtil.close(event);
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
                    sessionIdle(now);
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

    protected void putSession(SocketSession session) throws RejectedExecutionException {
        IntObjectHashMap<SocketSession> sessions = this.sessions;
        Integer sessionId = session.getSessionId();
        SocketSession old = sessions.get(sessionId);
        if (old != null) {
            CloseUtil.close(old);
        }
        if (sessions.size() >= sessionSizeLimit) {
            throw new RejectedExecutionException(
                    "session size limit:" + sessionSizeLimit + ",current:" + sessions.size());
        }
        sessions.put(sessionId.intValue(), session);
        session.getContext().getSessionManager().putSession(session);
    }

    private NioSocketChannel registChannel(SocketChannel javaChannel, NioEventLoop eventLoop,
            ChannelContext context, int channelId) throws IOException {
        SelectionKey sk = javaChannel.register(eventLoop.selector, SelectionKey.OP_READ);
        // 绑定SocketChannel到SelectionKey
        NioSocketChannel channel = (NioSocketChannel) sk.attachment();
        if (channel != null) {
            return channel;
        }
        channel = new NioSocketChannel(eventLoop, sk, context, channelId);
        sk.attach(channel);
        // fire session open event
        channel.fireOpend();
        if (!channel.isEnableSsl()) {
            finishRegistChannel(channel.getSession(), null);
        }
        return channel;
    }

    private SelectionKey registSelector(NioEventLoop eventLoop, ChannelContext context)
            throws IOException {
        ChannelService channelService = context.getChannelService();
        SelectableChannel channel = channelService.getSelectableChannel();
        if (context.isEnableSsl()) {
            eventLoop.setAttribute(SslHandler.SSL_HANDlER_EVENT_LOOP_KEY,
                    context.getSslContext().newSslHandler());
        }
        if (channelService instanceof ChannelAcceptor) {
            //FIXME 使用多eventLoop accept是否导致卡顿 是否要区分accept和read
            return channel.register(eventLoop.selector, SelectionKey.OP_ACCEPT, context);
        } else {
            return channel.register(eventLoop.selector, SelectionKey.OP_CONNECT, context);
        }
    }

    public void registSelector(final ChannelContext context) throws IOException {
        if (sharable && !isAcceptor) {
            throw new IOException("not acceptor event loop");
        }
        if (inEventLoop()) {
            registSelector(this, context);
        } else {
            final Waiter waiter = new Waiter();
            dispatch(new NioEventLoopTask() {
                @Override
                public void close() throws IOException {
                    waiter.response(null);
                }

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

    @Override
    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    protected void removeSession(SocketSession session) {
        sessions.remove(session.getSessionId());
        session.getContext().getSessionManager().removeSession(session);
    }

    public SocketSession getSession(int sessionId) {
        return sessions.get(sessionId);
    }

    private void sessionIdle(long currentTime) {
        long lastIdleTime = this.lastIdleTime;
        this.lastIdleTime = currentTime;
        IntObjectHashMap<SocketSession> sessions = this.sessions;
        if (sessions.size() == 0) {
            return;
        }
        if (sharable) {
            for (SocketSession session : sessions.values()) {
                ChannelContext context = session.getContext();
                List<SessionIdleEventListener> ls = context.getSessionIdleEventListeners();
                if (ls.size() == 1) {
                    try {
                        ls.get(0).sessionIdled(session, lastIdleTime, currentTime);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    for (SessionIdleEventListener l : ls) {
                        try {
                            l.sessionIdled(session, lastIdleTime, currentTime);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        } else {
            List<SessionIdleEventListener> ls = context.getSessionIdleEventListeners();
            for (SessionIdleEventListener l : ls) {
                for (SocketSession session : sessions.values()) {
                    l.sessionIdled(session, lastIdleTime, currentTime);
                }
            }
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

    public List<ChannelFuture> getReadFutures() {
        return readFutures;
    }

    private String desc;

    @Override
    public String toString() {
        if (desc == null) {
            desc = MessageFormatter.arrayFormat("NioEventLoop(idx:{},sharable:{},isAcceptor:{})",
                    new Object[] { index, sharable, isAcceptor });
        }
        return desc;
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
