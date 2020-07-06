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

import com.firenio.Develop;
import com.firenio.collection.IntMap;
import com.firenio.common.ByteUtil;
import com.firenio.common.Unsafe;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

/**
 * @author: wangkai
 **/
public class EpollEventLoop extends NioEventLoop {

    private static final Logger logger = NEW_LOGGER();

    final IntMap<ChannelContext> ctxs    = new IntMap<>(256);
    final int                    ep_size = 1024;
    final int                    epfd;
    final int                    event_fd;
    final long                   data;
    final long                   ep_events;
    final long                   iovec;

    public EpollEventLoop(NioEventLoopGroup group, int index, String threadName) {
        super(group, index, threadName);
        int iovec_len = group.getWriteBuffers();
        this.event_fd = Native.new_event_fd();
        this.epfd = Native.epoll_create(ep_size);
        this.ep_events = Native.new_epoll_event_array(ep_size);
        this.data = Unsafe.allocate(256);
        this.iovec = Unsafe.allocate(iovec_len * 16);
        int res = Native.epoll_add(epfd, event_fd, Native.EPOLL_IN_ET);
        if (res == -1) {
            throw new RuntimeException(Native.err_str());
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
        return LoggerFactory.getLogger(EpollEventLoop.class);
    }

    @Override
    void accept(int size) {
        final int  epfd      = this.epfd;
        final long data      = this.data;
        final int  event_fd  = this.event_fd;
        final long ep_events = this.ep_events;
        for (int i = 0; i < size; i++) {
            int p  = i * Native.SIZEOF_EPOLL_EVENT;
            int e  = Unsafe.getInt(ep_events + p);
            int fd = Unsafe.getInt(ep_events + p + 4);
            if (fd == event_fd) {
                Native.event_fd_read(fd);
                continue;
            }
            if (acceptor) {
                accept(data, epfd, fd);
            } else {
                accept(fd, e);
            }
        }
    }

    private void accept(long data, int epfd, int fd) {
        final ChannelAcceptor ctx       = (ChannelAcceptor) ctxs.get(fd);
        final int             listen_fd = ((ChannelAcceptor.EpollAcceptorUnsafe) ctx.getUnsafe()).listen_fd;
        final int             cfd       = Native.accept(epfd, listen_fd, data);
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

    private void accept(int fd, int e) {
        Channel ch = getChannel(fd);
        if (ch != null) {
            if (Develop.CHANNEL_DEBUG) {
                if (!ch.isOpen()) {
                    logger.error("channel closed but goto accept block");
                    return;
                }
            }
            if ((e & Native.close_event()) != 0) {
                ch.close();
                return;
            }
            if (CHANNEL_READ_FIRST) {
                if ((e & Native.EPOLL_IN) != 0) {
                    try {
                        ch.read();
                    } catch (Throwable ex) {
                        read_exception_caught(ch, ex);
                        return;
                    }
                }
                if ((e & Native.EPOLL_OUT) != 0) {
                    if (ch.write() == -1) {
                        ch.close();
                        return;
                    }
                }
            } else {
                if ((e & Native.EPOLL_OUT) != 0) {
                    if (ch.write() == -1) {
                        ch.close();
                        return;
                    }
                }
                if ((e & Native.EPOLL_IN) != 0) {
                    try {
                        ch.read();
                    } catch (Throwable ex) {
                        read_exception_caught(ch, ex);
                    }
                }
            }
        } else {
            accept_connect(fd, e);
        }
    }

    private void accept_connect(int fd, int e) {
        ChannelConnector ctx = (ChannelConnector) ctxs.remove(fd);
        if (ctx == null) {
            if (Develop.CHANNEL_DEBUG) {
                logger.error("server run in accept_connect........");
            }
            return;
        }
        if ((e & Native.close_event()) != 0 || !Native.finish_connect(fd)) {
            ctx.channelEstablish(null, NOT_FINISH_CONNECT);
            return;
        }
        String ra = ((ChannelConnector.EpollConnectorUnsafe) ctx.getUnsafe()).getRemoteAddr();
        register_channel(this, ctx, fd, ra, Native.get_port(fd), ctx.getPort(), false);
    }

    long getData() {
        return data;
    }

    long getIovec() {
        return iovec;
    }

    @Override
    public void shutdown0() {
        Unsafe.free(iovec);
        Unsafe.free(data);
        Unsafe.free(ep_events);
        Native.epoll_del(epfd, event_fd);
        Native.close(event_fd);
        Native.close(epfd);
    }

    private void register_channel(NioEventLoop el, ChannelContext ctx, int fd, String ra, int lp, int rp, boolean add) {
        IntMap<Channel> channels = el.channels;
        if (channels.size() >= ch_size_limit) {
            logger.error(OVER_CH_SIZE_LIMIT.getMessage(), OVER_CH_SIZE_LIMIT);
            ctx.channelEstablish(null, OVER_CH_SIZE_LIMIT);
            return;
        }
        int epfd = ((EpollEventLoop) el).epfd;
        int res;
        if (add) {
            res = Native.epoll_add(epfd, fd, Native.EPOLL_IN_OUT_ET);
        } else {
            res = Native.epoll_mod(epfd, fd, Native.EPOLL_IN_OUT_ET);
        }
        if (res == -1) {
            if (add) {
                Native.close(fd);
            } else {
                ctx.channelEstablish(null, new IOException(Native.err_str()));
            }
            return;
        }
        Channel old = channels.get(fd);
        if (old != null) {
            if (Develop.CHANNEL_DEBUG) {
                logger.error("old channel ....................,{},open:{}", old, old.isOpen());
            }
            old.close();
        }
        Integer id = el.nextChannelId();
        Channel ch = new Channel.EpollChannel(el, ctx, epfd, fd, ra, lp, rp, id);
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
    void wakeup0() {
        Native.event_fd_write(event_fd, 1L);
    }

}
