/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import com.firenio.collection.IntMap;
import com.firenio.common.Util;
import static com.firenio.Develop.debugException;

/**
 * @author: wangkai
 **/
public class JavaEventLoop extends NioEventLoop {

    private static final boolean         ENABLE_SEL_KEY_SET = check_enable_selection_key_set();
    private final        SelectionKeySet selection_key_set;
    private final        Selector        selector;
    private final        ByteBuffer[]    write_buffers;

    JavaEventLoop(NioEventLoopGroup group, int index, String threadName) throws IOException {
        super(group, index, threadName);
        if (ENABLE_SEL_KEY_SET) {
            this.selection_key_set = new SelectionKeySet(1024);
        } else {
            this.selection_key_set = null;
        }
        this.selector = open_selector(selection_key_set);
        this.write_buffers = new ByteBuffer[group.getWriteBuffers()];
    }

    private static boolean check_enable_selection_key_set() {
        Selector selector = null;
        try {
            selector = open_selector(new SelectionKeySet(0));
            return selector.selectedKeys().getClass() == SelectionKeySet.class;
        } catch (Throwable e) {
            return false;
        } finally {
            Util.close(selector);
        }
    }

    @SuppressWarnings("rawtypes")
    private static Selector open_selector(final SelectionKeySet keySet) throws IOException {
        final SelectorProvider provider = SelectorProvider.provider();
        final Selector         selector = provider.openSelector();
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
                    Field     selectedKeysField       = selectorImplClass.getDeclaredField("selectedKeys");
                    Field     publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");
                    Throwable cause                   = Util.trySetAccessible(selectedKeysField);
                    if (cause != null) {
                        return cause;
                    }
                    cause = Util.trySetAccessible(publicSelectedKeysField);
                    if (cause != null) {
                        return cause;
                    }
                    selectedKeysField.set(selector, keySet);
                    publicSelectedKeysField.set(selector, keySet);
                    return null;
                } catch (Throwable e) {
                    return e;
                }
            }
        });
        if (res instanceof Throwable) {
            return selector;
        }
        return selector;
    }

    ByteBuffer[] getWriteBuffers() {
        return write_buffers;
    }

    @Override
    void accept(int size) {
        if (ENABLE_SEL_KEY_SET) {
            final SelectionKeySet keySet = selection_key_set;
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

    private void accept(final Channel ch, final int readyOps) {
        if (CHANNEL_READ_FIRST) {
            if ((readyOps & SelectionKey.OP_READ) != 0) {
                try {
                    ch.read();
                } catch (Throwable e) {
                    read_exception_caught(ch, e);
                }
            } else if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                if (ch.write() == -1) {
                    ch.close();
                    return;
                }
            }
        } else {
            if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                if (ch.write() == -1) {
                    ch.close();
                    return;
                }
            }
            if ((readyOps & SelectionKey.OP_READ) != 0) {
                try {
                    ch.read();
                } catch (Throwable e) {
                    read_exception_caught(ch, e);
                }
            }
        }
    }

    private void accept(final ChannelAcceptor acceptor) {
        ChannelAcceptor.JavaAcceptorUnsafe au      = (ChannelAcceptor.JavaAcceptorUnsafe) acceptor.getUnsafe();
        ServerSocketChannel                channel = au.getSelectableChannel();
        try {
            //有时候还未register selector，但是却能selector到sk
            //如果getLocalAddress为空则不处理该sk
            if (channel.getLocalAddress() == null) {
                return;
            }
            final SocketChannel ch = channel.accept();
            if (ch == null) {
                return;
            }
            final NioEventLoopGroup group    = acceptor.getProcessorGroup();
            final NioEventLoop      targetEL = group.getNext();
            ch.configureBlocking(false);
            targetEL.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        register_channel(ch, targetEL, acceptor, true);
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
        } catch (Throwable e) {
            debugException(logger, e);
        }
    }

    private void accept(ChannelConnector connector, SelectionKey key) {
        final SocketChannel channel = getSocketChannel(connector);
        try {
            if (channel.finishConnect()) {
                int ops = key.interestOps();
                ops &= ~SelectionKey.OP_CONNECT;
                key.interestOps(ops);
                register_channel(channel, this, connector, false);
            } else {
                connector.channelEstablish(null, NOT_FINISH_CONNECT);
            }
        } catch (Throwable e) {
            connector.channelEstablish(null, e);
        }
    }

    private void accept(Object attach, SelectionKey key) {
        if (attach instanceof ChannelAcceptor) {
            accept((ChannelAcceptor) attach);
        } else {
            accept((ChannelConnector) attach, key);
        }
    }

    private void accept(final SelectionKey key) {
        if (!key.isValid()) {
            key.cancel();
            return;
        }
        final Object attach = key.attachment();
        if (attach instanceof Channel) {
            accept((Channel) attach, key.readyOps());
        } else {
            accept(attach, key);
        }
    }

    @Override
    public void shutdown0() {
        Util.close(selector);
    }

    protected Selector getSelector() {
        return selector;
    }

    private SocketChannel getSocketChannel(ChannelConnector connector) {
        ChannelConnector.JavaConnectorUnsafe cu = (ChannelConnector.JavaConnectorUnsafe) connector.getUnsafe();
        return cu.getSelectableChannel();
    }

    private void register_channel(SocketChannel jch, NioEventLoop el, ChannelContext ctx, boolean acceptor) throws IOException {
        IntMap<Channel> channels = el.channels;
        if (channels.size() >= el.ch_size_limit) {
            logger.error(OVER_CH_SIZE_LIMIT.getMessage(), OVER_CH_SIZE_LIMIT);
            ctx.channelEstablish(null, OVER_CH_SIZE_LIMIT);
            return;
        }
        JavaEventLoop elUnsafe  = (JavaEventLoop) el;
        int           channelId = el.nextChannelId();
        SelectionKey     sel_key   = jch.register(elUnsafe.selector, SelectionKey.OP_READ);
        Util.close(channels.get(channelId));
        Util.close((Channel) sel_key.attachment());
        String ra;
        int    lp;
        int    rp;
        if (acceptor) {
            InetSocketAddress address = (InetSocketAddress) jch.getRemoteAddress();
            lp = ctx.getPort();
            ra = address.getAddress().getHostAddress();
            rp = address.getPort();
        } else {
            InetSocketAddress remote = (InetSocketAddress) jch.getRemoteAddress();
            InetSocketAddress local  = (InetSocketAddress) jch.getLocalAddress();
            lp = local.getPort();
            ra = remote.getAddress().getHostAddress();
            rp = remote.getPort();
        }
        sel_key.attach(new Channel.JavaChannel(el, ctx, sel_key, ra, lp, rp, channelId));
        Channel ch = (Channel) sel_key.attachment();
        register_ch(ctx, channelId, channels, ch);
    }

    @Override
    int select(long timeout) {
        try {
            return selector.select(timeout);
        } catch (Throwable e) {
            debugException(logger, e);
            return 0;
        }
    }

    @Override
    int select_now() {
        try {
            return selector.selectNow();
        } catch (Throwable e) {
            debugException(logger, e);
            return 0;
        }
    }

    @Override
    void wakeup0() {
        selector.wakeup();
    }

    static final class SelectionKeySet extends AbstractSet<SelectionKey> {

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

}
