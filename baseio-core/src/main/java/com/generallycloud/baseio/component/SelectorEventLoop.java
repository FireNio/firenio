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
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLHandshakeException;

import com.generallycloud.baseio.LifeCycleUtil;
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
import com.generallycloud.baseio.concurrent.LineEventLoop;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
//FIXME 使用ThreadLocal
public class SelectorEventLoop extends AbstractEventLoop implements ChannelThreadContext {

    private static final Logger                 logger            = LoggerFactory.getLogger(SelectorEventLoop.class);
    private Map<Object, Object>                  attributes         = new HashMap<>();
    private ByteBuf                              buf                = null;
    private ByteBufAllocator                     byteBufAllocator   = null;
    private ChannelByteBufReader                 byteBufReader      = null;
    private NioSocketChannelContext              context            = null;
    private SelectorEventLoopGroup               eventLoopGroup     = null;
    private ExecutorEventLoop                    executorEventLoop  = null;
    private volatile boolean                   hasTask            = false;
    private int                                  index;
    private AtomicBoolean                        selecting          = new AtomicBoolean();
    private SelectionKeySet                      selectionKeySet    = null;
    private SocketSelector                       selector           = null;
    private BufferedArrayList<SelectorLoopEvent> events             = new BufferedArrayList<>();
    private SocketSessionManager                 sessionManager     = null;    
    private SslHandler                           sslHandler         = null;
    private ByteBuffer []                        writeBuffers       = null;
    private boolean                             isEnableSsl;
    private IoEventHandleAdaptor                 ioEventHandle;

    SelectorEventLoop(SelectorEventLoopGroup group, int index) {
        this.index = index;
        this.eventLoopGroup = group;
        this.context = group.getChannelContext();
        this.ioEventHandle = context.getIoEventHandleAdaptor();
        this.executorEventLoop = context.getExecutorEventLoopGroup().getNext();
        this.byteBufReader = context.newChannelByteBufReader();
        this.sessionManager = new NioSocketSessionManager(context);
        this.byteBufAllocator = context.getByteBufAllocatorManager().getNextBufAllocator();
        if (context.isEnableSsl()) {
            this.isEnableSsl = context.isEnableSsl();
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
                selector.buildChannel(this, k);
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
            buf.nioBuffer();
            int length = ch.read(buf);
            if (length < 1) {
                if (length == -1) {
                    CloseUtil.close(ch);
                }
                return;
            }
            ch.active();
            byteBufReader.accept(ch, buf.flip());
        } catch (Throwable e) {
            if (e instanceof SSLHandshakeException) {
                getSelector().finishConnect(ch.getSession(), e);
            }
            closeSocketChannel(ch, e);
        }
    }

    @Override
    public void clearAttributes() {
        this.attributes.clear();
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

        // 这里再次判断一下，防止判断isRunning为true后的线程切换停顿
        // 如果切换停顿，这里判断可以确保event要么被close了，要么被执行了

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
    protected void doLoop() throws IOException {
        SocketSelector selector = getSelector();
        int selected;
        //		long last_select = System.currentTimeMillis();
        if (selecting.compareAndSet(false, true)) {
            // Im not sure selectorLoopEvent.size if visible immediately by other thread ?
            // can we use selectorLoopEvents.getBufferSize() > 0 ?
            if (hasTask) {
                selected = selector.selectNow();
            }else{
                // FIXME try
                selected = selector.select(16);
            }
            hasTask = false;
            selecting.set(false);
        } else {
            selected = selector.selectNow();
            hasTask = false;
        }
        if (selected > 0) {
            if (selectionKeySet != null) {
                SelectionKeySet keySet = selectionKeySet;
                for (int i = 0; i < keySet.size; i++) {
                    SelectionKey k = keySet.keys[i];
                    keySet.keys[i] = null;
                    accept(k);
                }
            } else {
                Set<SelectionKey> sks = selector.selectedKeys();
                for (SelectionKey k : sks) {
                    accept(k);
                }
                sks.clear();
            }
        } else {
            //			selectEmpty(last_select);
        }
        for (SelectorLoopEvent event : events.getBuffer()) {
            handleEvent(event);
        }
        sessionManager.loop();
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
        this.rebuildSelector();
    }

    @Override
    protected void doStop() {
        ThreadUtil.sleep(8);
        closeEvents(events);
        closeEvents(events);
        LifeCycleUtil.stop(sessionManager);
        CloseUtil.close(selector);
        ReleaseUtil.release(buf,buf.getReleaseVersion());
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
    
    protected SocketSelector getSelector() {
        return selector;
    }

    @Override
    public SocketSessionManager getSocketSessionManager() {
        return sessionManager;
    }

    @Override
    public SslHandler getSslHandler() {
        return sslHandler;
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

    @SuppressWarnings("rawtypes")
    private SocketSelector openSelector(SelectableChannel channel) throws IOException {
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
            return new SocketSelector(this, channel, selector);
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
            return new SocketSelector(this, channel, selector);
        }
        selectionKeySet = keySet;
        return new SelectionKeySocketSelector(this, channel, selector, keySet);
    }

    private void rebuildSelector() throws IOException {
        SocketSelector oldSelector = this.selector;
        SocketSelector newSelector = rebuildSelector0();
        if (oldSelector != null) {
            Selector oldSel = oldSelector.getSelector();
            Selector newSel = newSelector.getSelector();
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

    private SocketSelector rebuildSelector0() throws IOException {
        NioSocketChannelContext context = getChannelContext();
        SelectableChannel channel = context.getSelectableChannel();
        SocketSelector selector = openSelector(channel);
        if (context.getChannelService() instanceof ChannelAcceptor) {
            //FIXME 使用多eventLoop accept是否导致卡顿 是否要区分accept和read
            channel.register(selector.getSelector(), SelectionKey.OP_ACCEPT);
        } else {
            channel.register(selector.getSelector(), SelectionKey.OP_CONNECT);
        }
        return selector;
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
        hasTask = true;
        if (selecting.compareAndSet(false, true)) {
            selecting.set(false);
            return;
        }
        getSelector().wakeup();
        super.wakeup();
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
