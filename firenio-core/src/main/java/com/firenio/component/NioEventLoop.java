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

import com.carrotsearch.hppc.IntArrayList;
import com.firenio.Options;
import com.firenio.buffer.ByteBuf;
import com.firenio.buffer.ByteBufAllocator;
import com.firenio.collection.ArrayListStack;
import com.firenio.collection.DelayedQueue;
import com.firenio.collection.IntObjectMap;
import com.firenio.collection.LinkedBQStack;
import com.firenio.collection.Stack;
import com.firenio.common.Util;
import com.firenio.concurrent.AtomicArray;
import com.firenio.concurrent.EventLoop;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangkai
 */
public abstract class NioEventLoop extends EventLoop {

    static final int           CHANNELS_COMPACT_SIZE = 1024 * 256;
    static final boolean       CHANNEL_READ_FIRST    = Options.isChannelReadFirst();
    static final Logger        logger                = NEW_LOGGER();
    static final IOException   NOT_FINISH_CONNECT    = NOT_FINISH_CONNECT();
    static final IOException   OVER_CH_SIZE_LIMIT    = OVER_CH_SIZE_LIMIT();
    static final AtomicInteger ATTRIBUTE_KEYS        = new AtomicInteger();
    // NOTICE USE_HAS_TASK can make a memory barrier for io thread,
    // please do not change this value to false unless you are really need.
    // I am not sure if the select()/select(n) has memory barrier,
    // it can be set USE_HAS_TASK to false if there is a memory barrier.
    static final boolean       USE_HAS_TASK          = true;

    final ByteBufAllocator        alloc;
    final ByteBuf                 buf;
    final IntArrayList            close_channels = new IntArrayList();
    final int                     ch_size_limit;
    final DelayedQueue            delayed_queue  = new DelayedQueue();
    final BlockingQueue<Runnable> events         = new LinkedBlockingQueue<>();
    final AtomicArray             attributes     = new AtomicArray();
    final NioEventLoopGroup       group;
    final int                     index;
    final AtomicInteger           selecting      = new AtomicInteger();
    final boolean                 sharable;
    final long                    buf_address;
    final boolean                 acceptor;

    IntObjectMap<Channel> channels = new IntObjectMap<>(4096);
    volatile boolean has_task = false;

    NioEventLoop(NioEventLoopGroup group, int index, String threadName) {
        super(threadName);
        this.index = index;
        this.group = group;
        this.sharable = group.isSharable();
        this.acceptor = group.isAcceptor();
        this.alloc = group.getByteBufAllocator(index);
        this.ch_size_limit = group.getChannelSizeLimit();
        int channelReadBuffer = group.getChannelReadBuffer();
        if (channelReadBuffer > 0) {
            this.buf = ByteBuf.buffer(channelReadBuffer);
            this.buf_address = buf.address();
        } else {
            this.buf = null;
            this.buf_address = -1;
        }
    }

    private static void channel_idle(ChannelIdleListener l, Channel ch, long lastIdleTime) {
        try {
            l.channelIdled(ch, lastIdleTime);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void channel_idle(ChannelContext context, IntObjectMap<Channel> channels, long last_idle_time) {
        List<ChannelIdleListener> ls = context.getChannelIdleEventListeners();
        for (int i = 0; i < ls.size(); i++) {
            ChannelIdleListener l = ls.get(i);
            for (channels.scan(); channels.hasNext(); ) {
                Channel ch = channels.getValue();
                channel_idle(l, ch, last_idle_time);
            }
        }
    }

    private static void channel_idle_share(IntObjectMap<Channel> channels, long last_idle_time) {
        for (channels.scan(); channels.hasNext(); ) {
            Channel                   ch      = channels.getValue();
            ChannelContext            context = ch.getContext();
            List<ChannelIdleListener> ls      = context.getChannelIdleEventListeners();
            if (ls.size() == 1) {
                channel_idle(ls.get(0), ch, last_idle_time);
            } else {
                for (int i = 0; i < ls.size(); i++) {
                    channel_idle(ls.get(i), ch, last_idle_time);
                }
            }
        }
    }

    static void register_ch(ChannelContext ctx, int fd, IntObjectMap<Channel> channels, Channel ch) {
        channels.put(fd, ch);
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

    private static Logger NEW_LOGGER() {
        return LoggerFactory.getLogger(NioEventLoop.class);
    }

    static IOException NOT_FINISH_CONNECT() {
        return Util.unknownStackTrace(new IOException("not finish connect"), SocketChannel.class, "finishConnect(...)");
    }

    static IOException OVER_CH_SIZE_LIMIT() {
        return Util.unknownStackTrace(new IOException("over channel size writeIndex"), NioEventLoop.class, "register_channel(...)");
    }

    static void read_exception_caught(Channel ch, Throwable ex) {
        ch.close();
        if (!ch.isSslHandshakeFinished()) {
            ch.getContext().channelEstablish(ch, ex);
        }
        logger.error(ex);
    }

    public static int nextAttributeKey() {
        return ATTRIBUTE_KEYS.getAndIncrement();
    }

    public ByteBufAllocator alloc() {
        return alloc;
    }

    //FIXME ..optimize sharable group
    private void channel_idle(long last_idle_time) {
        IntObjectMap<Channel> channels = this.channels;
        if (channels.isEmpty()) {
            return;
        }
        if (sharable) {
            channel_idle_share(channels, last_idle_time);
        } else {
            channel_idle(group.getContext(), channels, last_idle_time);
        }
    }

    private void close_channels() {
        IntObjectMap<Channel> channels = this.channels;
        for (channels.scan(); channels.hasNext(); ) {
            Util.close(channels.getValue());
        }
    }

    protected long getBufAddress() {
        return buf_address;
    }

    public Channel getChannel(int channelId) {
        return channels.get(channelId);
    }

    private Stack<Object> get_cache0(int key, int max) {
        Stack<Object> cache = getAttribute(key);
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

    public Object getCache(int key, int max) {
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

    protected int nextChannelId() {
        return group.nextChannelId();
    }

    public void release(int key, Object obj) {
        Stack<Object> buffer = getAttribute(key);
        if (buffer != null) {
            buffer.push(obj);
        }
    }

    public <T> T getAttribute(int key) {
        return (T) attributes.get(key);
    }

    public void setAttribute(int key, Object value) {
        attributes.set(key, value);
    }

    protected void removeChannel(int id) {
        close_channels.add(id);
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
                    logger.error(e.getMessage(), e);
                }
            }
        }
        close_channels();
        shutdown0();
        Util.release(buf);
    }

    abstract void shutdown0();

    private boolean has_task() {
        return USE_HAS_TASK ? has_task : !events.isEmpty();
    }

    private void clear_has_task() {
        if (USE_HAS_TASK) {
            has_task = false;
        }
    }

    private long run_delayed_events(DelayedQueue dq, long nextIdle) {
        long now = Util.now();
        for (; ; ) {
            DelayedQueue.DelayTask t = dq.peek();
            if (t == null) {
                return nextIdle - now;
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
                    logger.error(e.getMessage(), e);
                }
                now = Util.now();
            } else {
                if (delay < nextIdle) {
                    return delay - now;
                } else {
                    return nextIdle - now;
                }
            }
        }
    }

    @Override
    public void run() {
        // does it useful to set variables locally ?
        final long                    idle           = group.getIdleTime();
        final AtomicInteger           selecting      = this.selecting;
        final BlockingQueue<Runnable> events         = this.events;
        final DelayedQueue            dq             = this.delayed_queue;
        long                          last_idle_time = Util.now();
        long                          next_idle_time = last_idle_time + idle;
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
                        selected = select_now();
                    } else {
                        selected = select(select_time);
                    }
                    selecting.set(0);
                } else {
                    selected = select_now();
                }
                clear_has_task();
                if (selected > 0) {
                    accept(selected);
                }
                long now = Util.now();
                if (now >= next_idle_time) {
                    channel_idle(last_idle_time);
                    last_idle_time = next_idle_time;
                    next_idle_time = next_idle_time + idle;
                }
                run_events(events);
                if (!dq.isEmpty()) {
                    select_time = run_delayed_events(dq, next_idle_time);
                } else {
                    select_time = next_idle_time - Util.now();
                }
                remove_closed_channels();
            } catch (Throwable e) {
                logger.error(e);
            }
        }
    }

    private void remove_closed_channels() {
        IntArrayList          c_list   = this.close_channels;
        IntObjectMap<Channel> channels = this.channels;
        if (!c_list.isEmpty()) {
            for (int i = 0; i < c_list.size(); i++) {
                Channel ch = channels.remove(c_list.get(i));
                if (ch.isOpen()) {
                    channels.put(ch.getFd(), ch);
                }
            }
            c_list.clear();
            int keys_length = channels.keys.length;
            if (keys_length >= CHANNELS_COMPACT_SIZE) {
                if (channels.size() < keys_length >>> 3) {
                    int                   map_size     = Math.max(CHANNELS_COMPACT_SIZE >>> 2, channels.size());
                    IntObjectMap<Channel> new_channels = new IntObjectMap<>(map_size);
                    new_channels.putAll(channels);
                    this.channels = new_channels;
                }
            }
        }
    }

    public boolean schedule(final DelayedQueue.DelayTask task) {
        if (inEventLoop()) {
            return delayed_queue.offer(task);
        } else {
            return submit(() -> delayed_queue.offer(task));
        }
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
                wakeup0();
            }
        }
    }

    abstract void accept(int size);

    abstract int select(long timeout);

    abstract int select_now();

    abstract void wakeup0();

}
