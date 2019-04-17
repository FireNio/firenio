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

import static com.firenio.Develop.printException;

import java.io.Closeable;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.firenio.Develop;
import com.firenio.Options;
import com.firenio.buffer.ByteBuf;
import com.firenio.buffer.ByteBufAllocator;
import com.firenio.collection.ArrayListStack;
import com.firenio.collection.Attributes;
import com.firenio.collection.DelayedQueue;
import com.firenio.collection.IntMap;
import com.firenio.collection.LinkedBQStack;
import com.firenio.collection.Stack;
import com.firenio.common.ByteUtil;
import com.firenio.common.Unsafe;
import com.firenio.common.Util;
import com.firenio.component.ChannelConnector.EpollConnectorUnsafe;
import com.firenio.component.ChannelConnector.JavaConnectorUnsafe;
import com.firenio.concurrent.EventLoop;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

/**
 * @author wangkai
 */
public final class NioEventLoop extends EventLoop implements Attributes {

    private static final boolean     CHANNEL_READ_FIRST = Options.isChannelReadFirst();
    private static final Logger      logger             = NEW_LOGGER();
    private static final IOException NOT_FINISH_CONNECT = NOT_FINISH_CONNECT();
    private static final IOException OVER_CH_SIZE_LIMIT = OVER_CH_SIZE_LIMIT();
    private static final boolean     USE_HAS_TASK       = true;

    private final    ByteBufAllocator        alloc;
    private final    Map<Object, Object>     attributes    = new HashMap<>();
    private final    ByteBuf                 buf;
    private final    IntMap<Channel>         channels      = new IntMap<>(4096);
    private final    int                     ch_size_limit;
    private final    DelayedQueue            delayed_queue = new DelayedQueue();
    private final    BlockingQueue<Runnable> events        = new LinkedBlockingQueue<>();
    private final    NioEventLoopGroup       group;
    private volatile boolean                 has_task      = false;
    private final    int                     index;
    private final    AtomicInteger           selecting     = new AtomicInteger();
    private final    boolean                 sharable;
    private final    NioEventLoopUnsafe      unsafe;
    private final    long                    buf_address;
    private final    boolean                 acceptor;

    NioEventLoop(NioEventLoopGroup group, int index, String threadName) throws IOException {
        super(threadName);
        this.index = index;
        this.group = group;
        this.sharable = group.isSharable();
        this.acceptor = group.isAcceptor();
        this.alloc = group.getNextByteBufAllocator(index);
        this.ch_size_limit = group.getChannelSizeLimit();
        this.buf = ByteBuf.direct(group.getChannelReadBuffer());
        this.buf_address = Unsafe.address(buf.getNioBuffer());
        if (Native.EPOLL_AVAIABLE) {
            this.unsafe = new EpollNioEventLoopUnsafe(this);
        } else {
            this.unsafe = new JavaNioEventLoopUnsafe(this);
        }
    }

    public ByteBufAllocator alloc() {
        return alloc;
    }

    @Override
    public Map<Object, Object> attributes() {
        return attributes;
    }

    private static void channel_idle(ChannelIdleListener l, Channel ch, long lastIdleTime, long currentTime) {
        try {
            l.channelIdled(ch, lastIdleTime, currentTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    //FIXME ..optimize sharable group
    private void channel_idle(long last_idle_time, long current_time) {
        IntMap<Channel> channels = this.channels;
        if (channels.isEmpty()) {
            return;
        }
        if (sharable) {
            channel_idle_share(channels, last_idle_time, current_time);
        } else {
            channel_idle(group.getContext(), channels, last_idle_time, current_time);
        }
    }

    private static void channel_idle(ChannelContext context, IntMap<Channel> channels, long last_idle_time, long current_time) {
        List<ChannelIdleListener> ls = context.getChannelIdleEventListeners();
        for (int i = 0; i < ls.size(); i++) {
            ChannelIdleListener l = ls.get(i);
            for (channels.scan(); channels.hasNext(); ) {
                Channel ch = channels.nextValue();
                channel_idle(l, ch, last_idle_time, current_time);
            }
        }
    }

    private static void channel_idle_share(IntMap<Channel> channels, long last_idle_time, long current_time) {
        for (channels.scan(); channels.hasNext(); ) {
            Channel                   ch      = channels.nextValue();
            ChannelContext            context = ch.getContext();
            List<ChannelIdleListener> ls      = context.getChannelIdleEventListeners();
            if (ls.size() == 1) {
                channel_idle(ls.get(0), ch, last_idle_time, current_time);
            } else {
                for (int i = 0; i < ls.size(); i++) {
                    channel_idle(ls.get(i), ch, last_idle_time, current_time);
                }
            }
        }
    }

    @Override
    public void clearAttributes() {
        this.attributes.clear();
    }

    private void close_channels() {
        for (channels.scan(); channels.hasNext(); ) {
            Util.close(channels.nextValue());
        }
    }

    protected long getBufAddress() {
        return buf_address;
    }

    @Override
    public Object getAttribute(Object key) {
        return this.attributes.get(key);
    }

    @Override
    public Set<Object> getAttributeNames() {
        return this.attributes.keySet();
    }

    public Channel getChannel(int channelId) {
        return channels.get(channelId);
    }

    @SuppressWarnings("unchecked")
    private Stack<Object> get_cache0(String key, int max) {
        Stack<Object> cache = (Stack<Object>) getAttribute(key);
        if (cache == null) {
            if (group.isConcurrentFrameStack()) {
                cache = new LinkedBQStack<>(max);
            } else {
                cache = new ArrayListStack<>(max);
            }
            setAttribute(key, cache);
        }
        return cache;
    }

    public Object getCache(String key, int max) {
        return get_cache0(key, max).pop();
    }

    @Override
    public NioEventLoopGroup getGroup() {
        return group;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public BlockingQueue<Runnable> getJobs() {
        return events;
    }

    protected ByteBuf getReadBuf() {
        return buf;
    }

    protected NioEventLoopUnsafe getUnsafe() {
        return unsafe;
    }

    @SuppressWarnings("unchecked")
    public void release(String key, Object obj) {
        Stack<Object> buffer = (Stack<Object>) getAttribute(key);
        if (buffer != null) {
            buffer.push(obj);
        }
    }

    @Override
    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    protected void removeChannel(int id) {
        channels.remove(id);
    }


    private void run_events(BlockingQueue<Runnable> events) {
        if (!events.isEmpty()) {
            for (; ; ) {
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

    private void shutdown() {
        run_events(this.events);
        if (!delayed_queue.isEmpty()) {
            for (; ; ) {
                DelayedQueue.DelayTask t = delayed_queue.poll();
                if (t == null) {
                    break;
                }
                if (t.isCanceled()) {
                    continue;
                }
                try {
                    t.run();
                } catch (Throwable e) {
                    printException(logger, e, 1);
                }
            }
        }
        close_channels();
        Util.close(unsafe);
        Util.release(buf);
    }

    private boolean has_task() {
        return USE_HAS_TASK ? has_task : !events.isEmpty();
    }

    private void clear_has_task() {
        if (USE_HAS_TASK) {
            has_task = false;
        }
    }

    private long run_delayed_events(DelayedQueue dq, long now, long nextIdle, long selectTime) {
        for (; ; ) {
            DelayedQueue.DelayTask t = dq.peek();
            if (t == null) {
                break;
            }
            if (t.isCanceled()) {
                dq.poll();
                continue;
            }
            long delay = t.getDelay();
            if (now >= delay) {
                dq.poll();
                try {
                    t.done();
                    t.run();
                } catch (Throwable e) {
                    printException(logger, e, 1);
                }
                continue;
            }
            if (delay < nextIdle) {
                return delay - now;
            } else {
                return selectTime;
            }
        }
        return selectTime;
    }

    @Override
    public void run() {
        // does it useful to set variables locally ?
        final long                    idle           = group.getIdleTime();
        final NioEventLoopUnsafe      unsafe         = this.unsafe;
        final AtomicInteger           selecting      = this.selecting;
        final BlockingQueue<Runnable> events         = this.events;
        final DelayedQueue            dq             = this.delayed_queue;
        long                          next_idle_time = 0;
        long                          last_idle_time = 0;
        long                          select_time    = idle;
        for (; ; ) {
            // when this event loop is going to shutdown,we do not handle the last events 
            // because the method "submit" will return false, and if the task is closable,
            // the task will be closed, then free the other things and let it go, the group
            // restart will create a new event loop instead.
            if (!isRunning()) {
                shutdown();
                return;
            }
            try {
                // the method selector.wakeup is a weight operator, so we use flag "has_task"
                // and race flag "selecting" to reduce execution times of wake up
                // I am not sure events.size if a better way to instead of has_task?
                // example method selector.select(...) may throw an io exception 
                // and if we need to try with the method to do something when exception caught?
                int selected;
                if (!has_task() && selecting.compareAndSet(0, 1)) {
                    if (has_task()) {
                        selected = unsafe.select_now();
                    } else {
                        selected = unsafe.select(select_time);
                    }
                    selecting.set(0);
                } else {
                    selected = unsafe.select_now();
                }
                clear_has_task();
                if (selected > 0) {
                    unsafe.accept(selected);
                }
                long now = System.currentTimeMillis();
                if (now >= next_idle_time) {
                    channel_idle(last_idle_time, now);
                    last_idle_time = now;
                    next_idle_time = now + idle;
                    select_time = idle;
                } else {
                    select_time = next_idle_time - now;
                }
                run_events(events);
                if (!dq.isEmpty()) {
                    select_time = run_delayed_events(dq, now, next_idle_time, select_time);
                }
            } catch (Throwable e) {
                printException(logger, e, 1);
            }
        }
    }

    public boolean schedule(final DelayedQueue.DelayTask task) {
        if (inEventLoop()) {
            return delayed_queue.offer(task);
        } else {
            return submit(new Runnable() {

                @Override
                public void run() {
                    delayed_queue.offer(task);
                }
            });
        }
    }

    @Override
    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }

    public boolean submit(Runnable event) {
        if (super.submit(event)) {
            wakeup();
            return true;
        } else {
            if (event instanceof Closeable) {
                Util.close((Closeable) event);
            }
            return false;
        }
    }

    // FIXME 会不会出现这种情况，数据已经接收到本地，但是还没有被EventLoop处理完
    // 执行stop的时候如果确保不会再有数据进来
    @Override
    public void wakeup() {
        if (!inEventLoop()) {
            if (USE_HAS_TASK) {
                has_task = true;
            }
            if (selecting.compareAndSet(0, 1)) {
                selecting.set(0);
            } else {
                unsafe.wakeup();
            }
        }
    }

    static final class EpollNioEventLoopUnsafe extends NioEventLoopUnsafe {

        final IntMap<ChannelContext> ctxs    = new IntMap<>(256);
        final int                    ep_size = 1024;
        final int                    epfd;
        final int                    eventfd;
        final NioEventLoop           el;
        final long                   data;
        final long                   ep_events;
        final long                   iovec;

        public EpollNioEventLoopUnsafe(NioEventLoop eventLoop) {
            int iovec_len = eventLoop.group.getWriteBuffers();
            this.el = eventLoop;
            this.eventfd = Native.new_event_fd();
            this.epfd = Native.epoll_create(ep_size);
            this.ep_events = Native.new_epoll_event_array(ep_size);
            this.data = Unsafe.allocate(256);
            this.iovec = Unsafe.allocate(iovec_len * 16);
            int res = Native.epoll_add(epfd, eventfd, Native.EPOLLIN_ET);
            if (res == -1) {
                throw new RuntimeException(Native.errstr());
            }
        }

        @Override
        void accept(int size) {
            final int          epfd      = this.epfd;
            final long         data      = this.data;
            final int          eventfd   = this.eventfd;
            final NioEventLoop el        = this.el;
            final long         ep_events = this.ep_events;
            for (int i = 0; i < size; i++) {
                int p  = i * Native.SIZEOF_EPOLL_EVENT;
                int e  = Unsafe.getInt(ep_events + p);
                int fd = Unsafe.getInt(ep_events + p + 4);
                if (fd == eventfd) {
                    Native.event_fd_read(fd);
                    continue;
                }
                if (el.acceptor) {
                    accept(data, epfd, fd);
                } else {
                    accept(el, fd, e);
                }
            }
        }

        private void accept(long data, int epfd, int fd) {
            final ChannelAcceptor ctx      = (ChannelAcceptor) ctxs.get(fd);
            final int             listenfd = ((ChannelAcceptor.EpollAcceptorUnsafe) ctx.getUnsafe()).listenfd;
            final int             cfd      = Native.accept(epfd, listenfd, data);
            if (cfd == -1) {
                return;
            }
            final NioEventLoopGroup group    = ctx.getProcessorGroup();
            final NioEventLoop      targetEL = group.getNext();
            //10, 0, -7, -30, 0, 0, 0, 0, -2, -128, 0, 0, 0, 0, 0, 0, 80, 1, -107, 55, -55, 36, -124, -125, 2, 0, 0, 0,
            //10, 0, -4,  47, 0, 0, 0, 0,  0,       0, 0, 0, 0, 0, 0, 0,  0,  0,     -1, -1, -64, -88, -123,     1, 0, 0, 0, 0,
            int rp = (Unsafe.getByte(data + 2) & 0xff) << 8;
            rp |= (Unsafe.getByte(data + 3) & 0xff);
            String ra;
            if (Unsafe.getShort(data + 18) == -1 && Unsafe.getByte(data + 24) == 0) {
                //IPv4
                ra = decode_IPv4(data + 20);
            } else {
                //IPv6
                ra = decode_IPv6(data + 8);
            }
            final int    _lp = ctx.getPort();
            final int    _rp = rp;
            final String _ra = ra;
            targetEL.submit(new Runnable() {

                @Override
                public void run() {
                    register_channel(targetEL, ctx, cfd, _ra, _lp, _rp, true);
                }
            });
        }

        private void accept(NioEventLoop el, int fd, int e) {
            Channel ch = el.getChannel(fd);
            if (ch != null) {
                if (!ch.isOpen()) {
                    return;
                }
                if ((e & Native.close_event()) != 0) {
                    ch.close();
                    return;
                }
                if (CHANNEL_READ_FIRST) {
                    if ((e & Native.EPOLLIN) != 0) {
                        try {
                            ch.read();
                        } catch (Throwable ex) {
                            read_exception_caught(ch, ex);
                            return;
                        }
                    }
                    if ((e & Native.EPOLLOUT) != 0) {
                        int len = ch.write(this);
                        if (len == -1) {
                            ch.close();
                            return;
                        }
                    }
                } else {
                    if ((e & Native.EPOLLOUT) != 0) {
                        int len = ch.write(this);
                        if (len == -1) {
                            ch.close();
                            return;
                        }
                    }
                    if ((e & Native.EPOLLIN) != 0) {
                        try {
                            ch.read();
                        } catch (Throwable ex) {
                            read_exception_caught(ch, ex);
                        }
                    }
                }
            } else {
                accept_connect(el, fd, e);
            }
        }

        private void accept_connect(NioEventLoop el, int fd, int e) {
            ChannelConnector ctx = (ChannelConnector) ctxs.remove(fd);
            if ((e & Native.close_event()) != 0 || !Native.finish_connect(fd)) {
                ctx.channelEstablish(null, NOT_FINISH_CONNECT);
                return;
            }
            String ra = ((EpollConnectorUnsafe) ctx.getUnsafe()).getRemoteAddr();
            register_channel(el, ctx, fd, ra, Native.get_port(fd), ctx.getPort(), false);
        }

        long getData() {
            return data;
        }

        long getIovec() {
            return iovec;
        }

        @Override
        public void close() {
            Unsafe.free(iovec);
            Unsafe.free(data);
            Unsafe.free(ep_events);
            Native.epoll_del(epfd, eventfd);
            Native.close(eventfd);
            Native.close(epfd);
        }

        private void register_channel(NioEventLoop el, ChannelContext ctx, int fd, String ra, int lp, int rp, boolean add) {
            IntMap<Channel> channels = el.channels;
            if (channels.size() >= el.ch_size_limit) {
                printException(logger, OVER_CH_SIZE_LIMIT, 2);
                ctx.channelEstablish(null, OVER_CH_SIZE_LIMIT);
                return;
            }
            int epfd = ((EpollNioEventLoopUnsafe) el.unsafe).epfd;
            int res;
            if (add) {
                res = Native.epoll_add(epfd, fd, Native.EPOLLIN_OUT_ET);
            } else {
                res = Native.epoll_mod(epfd, fd, Native.EPOLLIN_OUT_ET);
            }
            if (res == -1) {
                if (add) {
                    Native.close(fd);
                } else {
                    ctx.channelEstablish(null, new IOException(Native.errstr()));
                }
                return;
            }
            Channel old = channels.get(fd);
            if (old != null) {
                if (Develop.NATIVE_DEBUG) {
                    logger.error("old channel ....................,open:{}", old.isOpen());
                }
                old.close();
            }
            Channel ch = new Channel(el, ctx, new Channel.EpollChannelUnsafe(epfd, fd, ra, lp, rp));
            register_ch(ctx, fd, channels, ch);
        }

        @Override
        int select(long timeout) {
            return Native.epoll_wait(epfd, ep_events, ep_size, timeout);
        }

        @Override
        int select_now() {
            return Native.epoll_wait(epfd, ep_events, ep_size, 0);
        }

        @Override
        void wakeup() {
            Native.event_fd_write(eventfd, 1L);
        }

    }

    private static void register_ch(ChannelContext ctx, int fd, IntMap<Channel> channels, Channel ch) {
        channels.put(fd, ch);
        ctx.getChannelManager().putChannel(ch);
        if (ch.isEnableSsl()) {
            // fire open event later
            if (ctx.getSslContext().isClient()) {
                ch.writeAndFlush(ByteBuf.empty());
            }
        } else {
            // fire open event immediately when plain ch
            ch.fire_opened();
            ctx.channelEstablish(ch, null);
        }
    }

    static final class JavaNioEventLoopUnsafe extends NioEventLoopUnsafe {

        private static final boolean         ENABLE_SELKEY_SET = check_enable_selection_key_set();
        private final        NioEventLoop    event_loop;
        private final        SelectionKeySet selection_key_set;
        private final        Selector        selector;
        private final        ByteBuffer[]    write_buffers;

        JavaNioEventLoopUnsafe(NioEventLoop eventLoop) throws IOException {
            if (ENABLE_SELKEY_SET) {
                this.selection_key_set = new SelectionKeySet(1024);
            } else {
                this.selection_key_set = null;
            }
            this.event_loop = eventLoop;
            this.selector = open_selector(selection_key_set);
            this.write_buffers = new ByteBuffer[eventLoop.group.getWriteBuffers()];
        }

        ByteBuffer[] getWriteBuffers() {
            return write_buffers;
        }

        @Override
        void accept(int size) {
            if (ENABLE_SELKEY_SET) {
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
                    int len = ch.write(this);
                    if (len == -1) {
                        ch.close();
                        return;
                    }
                }
            } else {
                if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                    int len = ch.write(this);
                    if (len == -1) {
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
                //有时候还未regist selector，但是却能selector到sk
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
                        } catch (IOException e) {
                            printException(logger, e, 1);
                        }
                    }
                });
            } catch (Throwable e) {
                printException(logger, e, 1);
            }
        }

        private void accept(ChannelConnector connector, SelectionKey key) {
            final SocketChannel channel = getSocketChannel(connector);
            try {
                if (channel.finishConnect()) {
                    int ops = key.interestOps();
                    ops &= ~SelectionKey.OP_CONNECT;
                    key.interestOps(ops);
                    register_channel(channel, event_loop, connector, false);
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
        public void close() {
            Util.close(selector);
        }

        protected Selector getSelector() {
            return selector;
        }

        private SocketChannel getSocketChannel(ChannelConnector connector) {
            JavaConnectorUnsafe cu = (JavaConnectorUnsafe) connector.getUnsafe();
            return cu.getSelectableChannel();
        }

        private void register_channel(SocketChannel jch, NioEventLoop el, ChannelContext ctx, boolean acceptor) throws IOException {
            IntMap<Channel> channels = el.channels;
            if (channels.size() >= el.ch_size_limit) {
                printException(logger, OVER_CH_SIZE_LIMIT, 2);
                ctx.channelEstablish(null, OVER_CH_SIZE_LIMIT);
                return;
            }
            JavaNioEventLoopUnsafe elUnsafe  = (JavaNioEventLoopUnsafe) el.unsafe;
            NioEventLoopGroup      g         = el.getGroup();
            int                    channelId = g.getChannelIds().getAndIncrement();
            SelectionKey           sk        = jch.register(elUnsafe.selector, SelectionKey.OP_READ);
            Util.close(channels.get(channelId));
            Util.close((Channel) sk.attachment());
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
            Channel.JavaChannelUnsafe unsafe = new Channel.JavaChannelUnsafe(sk, ra, lp, rp, channelId);
            sk.attach(new Channel(el, ctx, unsafe));
            Channel ch = (Channel) sk.attachment();
            register_ch(ctx, channelId, channels, ch);
        }

        @Override
        int select(long timeout) {
            try {
                return selector.select(timeout);
            } catch (IOException e) {
                printException(logger, e, 1);
                return 0;
            }
        }

        @Override
        int select_now() {
            try {
                return selector.selectNow();
            } catch (IOException e) {
                printException(logger, e, 1);
                return 0;
            }
        }

        @Override
        void wakeup() {
            selector.wakeup();
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

    }

    static abstract class NioEventLoopUnsafe implements Closeable {

        abstract void accept(int size);

        abstract int select(long timeout);

        abstract int select_now();

        abstract void wakeup();

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

    private static String decode_IPv4(long addr) {
        StringBuilder s = FastThreadLocal.get().getStringBuilder();
        s.append(ByteUtil.getNumString(Unsafe.getByte(addr + 0)));
        s.append('.');
        s.append(ByteUtil.getNumString(Unsafe.getByte(addr + 1)));
        s.append('.');
        s.append(ByteUtil.getNumString(Unsafe.getByte(addr + 2)));
        s.append('.');
        s.append(ByteUtil.getNumString(Unsafe.getByte(addr + 3)));
        return s.toString();
    }

    private static String decode_IPv6(long addr) {
        StringBuilder s = FastThreadLocal.get().getStringBuilder();
        for (int i = 0; i < 8; i++) {
            byte b1 = Unsafe.getByte(addr + (i << 1));
            byte b2 = Unsafe.getByte(addr + (i << 1) + 1);
            if (b1 == 0 && b2 == 0) {
                s.append('0');
                s.append(':');
            } else {
                s.append(ByteUtil.getHexString(b1));
                s.append(ByteUtil.getHexString(b2));
                s.append(':');
            }
        }
        s.setLength(s.length() - 1);
        return s.toString();
    }

    private static Logger NEW_LOGGER() {
        return LoggerFactory.getLogger(NioEventLoop.class);
    }

    private static IOException NOT_FINISH_CONNECT() {
        return Util.unknownStackTrace(new IOException("not finish connect"), SocketChannel.class, "finishConnect(...)");
    }

    private static IOException OVER_CH_SIZE_LIMIT() {
        return Util.unknownStackTrace(new IOException("over channel size limit"), NioEventLoop.class, "register_channel(...)");
    }

    private static void read_exception_caught(Channel ch, Throwable ex) {
        ch.close();
        Develop.printException(logger, ex, 2);
        if (!ch.isSslHandshakeFinished()) {
            ch.getContext().channelEstablish(ch, ex);
        }
    }

}
