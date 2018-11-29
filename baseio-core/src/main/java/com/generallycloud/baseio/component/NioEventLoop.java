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

import static com.generallycloud.baseio.Develop.DEBUG;
import static com.generallycloud.baseio.Develop.printException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLHandshakeException;

import com.generallycloud.baseio.Options;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.ByteBufUtil;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.collection.Attributes;
import com.generallycloud.baseio.collection.IntArray;
import com.generallycloud.baseio.collection.IntMap;
import com.generallycloud.baseio.common.ClassUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.common.ThrowableUtil;
import com.generallycloud.baseio.concurrent.AbstractEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public final class NioEventLoop extends AbstractEventLoop implements Attributes {

    private static final boolean           CHANNEL_READ_FIRST = Options.isChannelReadFirst();
    private static final boolean           ENABLE_SELKEY_SET  = checkEnableSelectionKeySet();
    private static final Logger            logger             = newLogger();
    private static final IOException       NOT_FINISH_CONNECT = NOT_FINISH_CONNECT();
    private static final IOException       OVER_CH_SIZE_LIMIT = OVER_CH_SIZE_LIMIT();

    private final ByteBufAllocator         alloc;
    private final Map<Object, Object>      attributes         = new HashMap<>();
    private ByteBuf                        buf;
    private final IntMap<NioSocketChannel> channels           = new IntMap<>();
    private final int                      chSizeLimit;
    private final Queue<Runnable>          events             = new LinkedBlockingQueue<>();
    private final NioEventLoopGroup        group;
    private volatile boolean               hasTask            = false;
    private final int                      index;
    private long                           lastIdleTime       = 0;
    private final IntArray                 preCloseChIds      = new IntArray();
    private final AtomicInteger            selecting          = new AtomicInteger();
    private final SelectionKeySet          selectionKeySet;
    private Selector                       selector;
    private final boolean                  sharable;
    // true eventLooper, false offerer
    private final AtomicInteger            wakener            = new AtomicInteger();
    private ByteBuffer[]                   writeBuffers;

    NioEventLoop(NioEventLoopGroup group, int index) {
        this.index = index;
        this.group = group;
        this.sharable = group.isSharable();
        this.alloc = group.getAllocatorGroup().getNext();
        this.chSizeLimit = group.getChannelSizeLimit();
        if (ENABLE_SELKEY_SET) {
            this.selectionKeySet = new SelectionKeySet(1024);
        } else {
            this.selectionKeySet = null;
        }
    }

    private void accept(final SelectionKey key) {
        if (!key.isValid()) {
            key.cancel();
            return;
        }
        final Object attach = key.attachment();
        if (attach instanceof NioSocketChannel) {
            final NioSocketChannel ch = (NioSocketChannel) attach;
            final int readyOps = key.readyOps();

            if (CHANNEL_READ_FIRST) {
                if ((readyOps & SelectionKey.OP_READ) != 0) {
                    try {
                        ch.read(buf);
                    } catch (Throwable e) {
                        readExceptionCaught(ch, e);
                    }
                } else if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                    try {
                        ch.write(key.interestOps());
                    } catch (Throwable e) {
                        writeExceptionCaught(ch, e);
                    }
                }
            } else {
                boolean writeComplete = true;
                if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                    try {
                        writeComplete = ch.write(key.interestOps());
                    } catch (Throwable e) {
                        writeExceptionCaught(ch, e);
                    }
                }
                //FIXME 观察这里不写完不让读的模式是否可行
                if (writeComplete && (readyOps & SelectionKey.OP_READ) != 0) {
                    try {
                        ch.read(buf);
                    } catch (Throwable e) {
                        readExceptionCaught(ch, e);
                    }
                }
            }
        } else {
            if (attach instanceof ChannelAcceptor) {
                final ChannelAcceptor acceptor = (ChannelAcceptor) attach;
                ServerSocketChannel serverChannel = acceptor.getSelectableChannel();
                try {
                    //有时候还未regist selector，但是却能selector到sk
                    //如果getLocalAddress为空则不处理该sk
                    if (serverChannel.getLocalAddress() == null) {
                        return;
                    }
                    final SocketChannel ch = serverChannel.accept();
                    if (ch == null) {
                        return;
                    }
                    final NioEventLoopGroup group = acceptor.getProcessorGroup();
                    final NioEventLoop targetEL = group.getNext();
                    ch.configureBlocking(false);
                    targetEL.execute(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                registChannel(ch, targetEL, acceptor);
                            } catch (ClosedChannelException e) {
                                printException(logger, e);
                            }
                        }
                    });
                } catch (Exception e) {
                    printException(logger, e);
                }
            } else {
                final ChannelConnector connector = (ChannelConnector) attach;
                final SocketChannel javaChannel = connector.getSelectableChannel();
                try {
                    if (!javaChannel.finishConnect()) {
                        key.cancel();
                        key.attach(null);
                        finishConnect(null, connector, NOT_FINISH_CONNECT);
                        return;
                    }
                    int ops = key.interestOps();
                    ops &= ~SelectionKey.OP_CONNECT;
                    key.interestOps(ops);
                    //FIXME need this code ?
                    // selector.selectNow();
                    registChannel(javaChannel, this, connector);
                } catch (IOException e) {
                    key.cancel();
                    key.attach(null);
                    finishConnect(null, connector, e);
                }
            }
        }
    }

    private void readExceptionCaught(NioSocketChannel ch, Throwable ex) {
        ch.close();
        if (DEBUG) {
            logger.error(ex.getMessage() + ch, ex);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(ex.getMessage() + ch, ex);
            }
        }
        if (ex instanceof SSLHandshakeException) {
            finishConnect(ch, ch.getContext(), (SSLHandshakeException) ex);
        }
    }

    private void writeExceptionCaught(NioSocketChannel ch, Throwable ex) {
        ch.close();
        if (DEBUG) {
            logger.error(ex.getMessage() + ch, ex);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(ex.getMessage() + ch, ex);
            }
        }
    }

    public ByteBufAllocator alloc() {
        return alloc;
    }

    @Override
    public Map<Object, Object> attributes() {
        return attributes;
    }

    private void channelIdle(ChannelIdleEventListener l, NioSocketChannel ch, long lastIdleTime,
            long currentTime) {
        try {
            l.channelIdled(ch, lastIdleTime, currentTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void channelIdle(long currentTime) {
        long lastIdleTime = this.lastIdleTime;
        this.lastIdleTime = currentTime;
        IntMap<NioSocketChannel> channels = this.channels;
        if (channels.size() == 0) {
            return;
        }
        //FIXME ..optimize sharable group
        if (sharable) {
            for (NioSocketChannel ch : channels.values()) {
                ChannelContext context = ch.getContext();
                List<ChannelIdleEventListener> ls = context.getChannelIdleEventListeners();
                if (ls.size() == 1) {
                    channelIdle(ls.get(0), ch, lastIdleTime, currentTime);
                } else {
                    for (ChannelIdleEventListener l : ls) {
                        channelIdle(l, ch, lastIdleTime, currentTime);
                    }
                }
            }
        } else {
            if (channels.size() > 0) {
                ChannelContext context = group.getContext();
                List<ChannelIdleEventListener> ls = context.getChannelIdleEventListeners();
                for (ChannelIdleEventListener l : ls) {
                    for (NioSocketChannel ch : channels.values()) {
                        channelIdle(l, ch, lastIdleTime, currentTime);
                    }
                }
            }
        }
    }

    @Override
    public void clearAttributes() {
        this.attributes.clear();
    }

    private void closeChannels() {
        for (NioSocketChannel ch : channels.values()) {
            CloseUtil.close(ch);
        }
    }

    @Override
    protected void doStartup() throws IOException {
        this.writeBuffers = new ByteBuffer[group.getWriteBuffers()];
        this.buf = ByteBufUtil.direct(group.getChannelReadBuffer());
        this.selector = openSelector(selectionKeySet);
    }

    @Override
    public void execute(Runnable event) {
        events.offer(event);
        if (inEventLoop()) {
            return;
        }
        if (!isRunning() && events.remove(event)) {
            group.getRejectedExecutionHandle().reject(this, event);
            return;
        }
        wakeup();
    }

    public void executeAfterLoop(Runnable event) {
        events.offer(event);
        if (!isRunning() && events.remove(event)) {
            group.getRejectedExecutionHandle().reject(this, event);
        }
    }

    private final void finishConnect(NioSocketChannel ch, ChannelContext context, IOException e) {
        if (context instanceof ChannelConnector) {
            ((ChannelConnector) context).finishConnect(ch, e);
        }
    }

    protected void flush(NioSocketChannel ch) {
        events.offer(ch);
    }

    protected void flushAndWakeup(NioSocketChannel ch) {
        events.offer(ch);
        wakeup();
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

    @Override
    public NioEventLoopGroup getGroup() {
        return group;
    }

    public int getIndex() {
        return index;
    }

    protected Selector getSelector() {
        return selector;
    }

    protected ByteBuffer[] getWriteBuffers() {
        return writeBuffers;
    }

    private void handleEvents(Queue<Runnable> events) {
        if (events.size() > 0) {
            for (;;) {
                Runnable event = events.poll();
                if (event == null) {
                    break;
                }
                try {
                    event.run();
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private void registChannel(SocketChannel jch, NioEventLoop el, ChannelContext context)
            throws ClosedChannelException {
        int channelId = el.getGroup().getChannelIds().getAndIncrement();
        SelectionKey sk = jch.register(el.getSelector(), SelectionKey.OP_READ);
        CloseUtil.close((NioSocketChannel) sk.attachment());
        NioSocketChannel ch = new NioSocketChannel(el, sk, context, channelId);
        sk.attach(ch);
        IntMap<NioSocketChannel> channels = el.channels;
        CloseUtil.close(channels.get(ch.getChannelId()));
        if (channels.size() >= el.chSizeLimit) {
            printException(logger, OVER_CH_SIZE_LIMIT);
            if (!ch.isEnableSsl()) {
                finishConnect(ch, context, OVER_CH_SIZE_LIMIT);
            }
            CloseUtil.close(ch);
            return;
        }
        channels.put(channelId, ch);
        context.getChannelManager().putChannel(ch);
        if (ch.isEnableSsl()) {
            // fire open event later
            if (context.getSslContext().isClient()) {
                ch.flush(EmptyByteBuf.get());
            }
        } else {
            // fire open event immediately when plain ch
            ch.fireOpend();
            finishConnect(ch, context, null);
        }
    }

    protected void registSelector(ChannelContext context, int op) throws IOException {
        assertInEventLoop("registSelector must in event loop");
        //FIXME 使用多eventLoop accept是否导致卡顿
        //FIXME OP_ACCEPT & OP_CONNECT 不能在注册在一个EL吗?
        //目前注册在一起会出现select到key但是key为空?
        context.getSelectableChannel().register(selector, op, context);
        //        if (oldSelector != null) {
        //            Selector oldSel = this.selector;
        //            Selector newSel = newSelector;
        //            Set<SelectionKey> sks = oldSel.keys();
        //            for (SelectionKey sk : sks) {
        //                if (!sk.isValid() || sk.attachment() == null) {
        //                    continue;
        //                }
        //                try {
        //                    sk.ch().register(newSel, SelectionKey.OP_READ);
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

    protected void removeChannel(int chId) {
        preCloseChIds.add(chId);
    }

    private void removeClosedChannels() {
        IntArray list = preCloseChIds;
        if (list.size() == 0) {
            return;
        }
        for (int i = 0, count = list.size(); i < count; i++) {
            NioSocketChannel ch = channels.remove(list.get(i));
            ch.getContext().getChannelManager().removeChannel(ch);
        }
        list.clear();
    }

    @Override
    public void run() {
        // does it useful to set variables locally ?
        final long idle = group.getIdleTime();
        final Selector selector = this.selector;
        final AtomicInteger selecting = this.selecting;
        final SelectionKeySet keySet = this.selectionKeySet;
        final Queue<Runnable> events = this.events;
        long nextIdle = 0;
        long selectTime = idle;
        for (;;) {
            // when this event loop is going to shutdown,we handle the last events 
            // and set stopped to true, to tell the waiter "I was stopped!"
            // now you can shutdown it safely
            if (!isRunning()) {
                handleEvents(events);
                closeChannels();
                CloseUtil.close(selector);
                ReleaseUtil.release(buf);
                return;
            }
            try {
                // the method selector.wakeup is a weight operator, so we use flag "hasTask"
                // and race flag "selecting" to reduce execution times of wake up
                // I am not sure events.size if visible immediately by other thread ?
                // can we use events.getBufferSize() > 0 instead of hasTask ?
                // example method selector.select(...) may throw an io exception 
                // and if we need to try with the method who may will throw an io exception ?
                int selected;
                if (hasTask) {
                    selected = selector.selectNow();
                    hasTask = false;
                } else {
                    if (selecting.compareAndSet(0, 1)) {
                        if (hasTask) {
                            selected = selector.selectNow();
                        } else {
                            selected = selector.select(selectTime);
                        }
                        hasTask = false;
                        selecting.set(0);
                    } else {
                        selected = selector.selectNow();
                        hasTask = false;
                    }
                }
                if (selected > 0) {
                    if (ENABLE_SELKEY_SET) {
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
                long now = System.currentTimeMillis();
                if (now >= nextIdle) {
                    channelIdle(now);
                    nextIdle = now + idle;
                    selectTime = idle;
                } else {
                    selectTime = nextIdle - now;
                }
                handleEvents(events);
                removeClosedChannels();
            } catch (Throwable e) {
                printException(logger, e);
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
        if (wakener.compareAndSet(0, 1)) {
            hasTask = true;
            if (selecting.compareAndSet(0, 1)) {
                selecting.set(0);
            } else {
                selector.wakeup();
                super.wakeup();
            }
            wakener.set(0);
        }
    }

    static class SelectionKeySet extends AbstractSet<SelectionKey> {

        SelectionKey[] keys;
        int            size;

        SelectionKeySet(int cap) {
            keys = new SelectionKey[cap];
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
            keys = Arrays.copyOf(keys, size << 1);
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

        @Override
        public String toString() {
            return "SelectionKeySet[" + size() + "]";
        }
    }

    private static boolean checkEnableSelectionKeySet() {
        Selector selector = null;
        try {
            selector = openSelector(new SelectionKeySet(0));
            return selector.selectedKeys().getClass() == SelectionKeySet.class;
        } catch (Throwable e) {
            return false;
        } finally {
            CloseUtil.close(selector);
        }
    }

    private static Logger newLogger() {
        return LoggerFactory.getLogger(NioEventLoop.class);
    }

    private static IOException NOT_FINISH_CONNECT() {
        return ThrowableUtil.unknownStackTrace(new IOException("not finish connect"),
                SocketChannel.class, "finishConnect(...)");
    }

    @SuppressWarnings("rawtypes")
    private static Selector openSelector(final SelectionKeySet keySet) throws IOException {
        final SelectorProvider provider = SelectorProvider.provider();
        final Selector selector = provider.openSelector();
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
        if (res instanceof Throwable) {
            return selector;
        }
        final Class selectorImplClass = (Class) res;
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
        return selector;
    }

    private static IOException OVER_CH_SIZE_LIMIT() {
        return ThrowableUtil.unknownStackTrace(new IOException("over channel size limit"),
                NioEventLoop.class, "registChannel(...)");
    }

}
