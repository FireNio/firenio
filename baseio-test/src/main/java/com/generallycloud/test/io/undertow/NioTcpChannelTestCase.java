/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.generallycloud.test.io.undertow;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.logging.Logger;
import org.xnio.ChannelListener;
import org.xnio.FutureResult;
import org.xnio.IoFuture;
import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;
import org.xnio.channels.BoundChannel;
import org.xnio.channels.Channels;
import org.xnio.channels.ConnectedStreamChannel;

/**
 * Test for TCP connected stream channels.
 * 
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author <a href="mailto:frainone@redhat.com">Flavia Rainone</a>
 */
@SuppressWarnings("deprecation")
public class NioTcpChannelTestCase {

    protected static final Logger log             = Logger.getLogger("TEST");

    private final List<Throwable> problems        = new CopyOnWriteArrayList<>();

    protected static final int    SERVER_PORT     = 12345;

    private OptionMap             serverOptionMap = OptionMap.create(Options.REUSE_ADDRESSES,
            Boolean.TRUE);                                                                   // any random map

    private OptionMap             clientOptionMap = OptionMap.EMPTY;

    private int                   threads         = 1;

    public static void main(String[] args) throws Exception {
        log.info("Test: acceptor");
        final CountDownLatch ioLatch = new CountDownLatch(4);
        final CountDownLatch closeLatch = new CountDownLatch(2);
        final AtomicBoolean clientOpened = new AtomicBoolean();
        final AtomicBoolean clientReadOnceOK = new AtomicBoolean();
        final AtomicBoolean clientReadDoneOK = new AtomicBoolean();
        final AtomicBoolean clientReadTooMuch = new AtomicBoolean();
        final AtomicBoolean clientWriteOK = new AtomicBoolean();
        final AtomicBoolean serverOpened = new AtomicBoolean();
        final AtomicBoolean serverReadOnceOK = new AtomicBoolean();
        final AtomicBoolean serverReadDoneOK = new AtomicBoolean();
        final AtomicBoolean serverReadTooMuch = new AtomicBoolean();
        final AtomicBoolean serverWriteOK = new AtomicBoolean();
        final byte[] bytes = "Ummagumma!".getBytes("UTF-8");
        final Xnio xnio = Xnio.getInstance("nio");
        final XnioWorker worker = xnio.createWorker(
                OptionMap.create(Options.WORKER_WRITE_THREADS, 2, Options.WORKER_READ_THREADS, 2));
        try {
            final FutureResult<InetSocketAddress> futureAddressResult = new FutureResult<>();
            final IoFuture<InetSocketAddress> futureAddress = futureAddressResult.getIoFuture();
            worker.acceptStream(
                    new InetSocketAddress(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 0),
                    new ChannelListener<ConnectedStreamChannel>() {
                        private final ByteBuffer inboundBuf  = ByteBuffer.allocate(512);
                        private int      readCnt     = 0;
                        private final ByteBuffer outboundBuf = ByteBuffer.wrap(bytes);

                        @Override
                        public void handleEvent(final ConnectedStreamChannel channel) {
                            channel.getCloseSetter()
                                    .set(new ChannelListener<ConnectedStreamChannel>() {
                                        @Override
                                        public void handleEvent(
                                                final ConnectedStreamChannel channel) {
                                            closeLatch.countDown();
                                        }
                                    });
                            channel.getReadSetter()
                                    .set(new ChannelListener<ConnectedStreamChannel>() {
                                        @Override
                                        public void handleEvent(
                                                final ConnectedStreamChannel channel) {
                                            try {
                                                final int res = channel.read(inboundBuf);
                                                if (res == -1) {
                                                    serverReadDoneOK.set(true);
                                                    ioLatch.countDown();
                                                    channel.shutdownReads();
                                                } else if (res > 0) {
                                                    final int ttl = readCnt += res;
                                                    if (ttl == bytes.length) {
                                                        serverReadOnceOK.set(true);
                                                    } else if (ttl > bytes.length) {
                                                        serverReadTooMuch.set(true);
                                                        IoUtils.safeClose(channel);
                                                        return;
                                                    }
                                                }
                                            } catch (IOException e) {
                                                log.errorf(e, "Server read failed");
                                                IoUtils.safeClose(channel);
                                            }
                                        }
                                    });
                            channel.getWriteSetter()
                                    .set(new ChannelListener<ConnectedStreamChannel>() {
                                        @Override
                                        public void handleEvent(
                                                final ConnectedStreamChannel channel) {
                                            try {
                                                channel.write(outboundBuf);
                                                if (!outboundBuf.hasRemaining()) {
                                                    serverWriteOK.set(true);
                                                    Channels.shutdownWritesBlocking(channel);
                                                    ioLatch.countDown();
                                                }
                                            } catch (IOException e) {
                                                log.errorf(e, "Server write failed");
                                                IoUtils.safeClose(channel);
                                            }
                                        }
                                    });
                            channel.resumeReads();
                            channel.resumeWrites();
                            serverOpened.set(true);
                        }
                    }, new ChannelListener<BoundChannel>() {
                        @Override
                        public void handleEvent(final BoundChannel channel) {
                            futureAddressResult
                                    .setResult(channel.getLocalAddress(InetSocketAddress.class));
                        }
                    }, OptionMap.create(Options.REUSE_ADDRESSES, Boolean.TRUE));
            final InetSocketAddress localAddress = futureAddress.get();
            worker.connectStream(localAddress, new ChannelListener<ConnectedStreamChannel>() {
                private final ByteBuffer inboundBuf  = ByteBuffer.allocate(512);
                private int              readCnt     = 0;
                private final ByteBuffer outboundBuf = ByteBuffer.wrap(bytes);

                @Override
                public void handleEvent(final ConnectedStreamChannel channel) {
                    channel.getCloseSetter().set(new ChannelListener<ConnectedStreamChannel>() {
                        @Override
                        public void handleEvent(final ConnectedStreamChannel channel) {
                            closeLatch.countDown();
                        }
                    });
                    channel.getReadSetter().set(new ChannelListener<ConnectedStreamChannel>() {
                        @Override
                        public void handleEvent(final ConnectedStreamChannel channel) {
                            try {
                                final int res = channel.read(inboundBuf);
                                if (res == -1) {
                                    channel.shutdownReads();
                                    clientReadDoneOK.set(true);
                                    ioLatch.countDown();
                                } else if (res > 0) {
                                    final int ttl = readCnt += res;
                                    if (ttl == bytes.length) {
                                        clientReadOnceOK.set(true);
                                    } else if (ttl > bytes.length) {
                                        clientReadTooMuch.set(true);
                                        IoUtils.safeClose(channel);
                                        return;
                                    }
                                }
                            } catch (IOException e) {
                                log.errorf(e, "Client read failed");
                                IoUtils.safeClose(channel);
                            }
                        }
                    });
                    channel.getWriteSetter().set(new ChannelListener<ConnectedStreamChannel>() {
                        @Override
                        public void handleEvent(final ConnectedStreamChannel channel) {
                            try {
                                channel.write(outboundBuf);
                                if (!outboundBuf.hasRemaining()) {
                                    clientWriteOK.set(true);
                                    Channels.shutdownWritesBlocking(channel);
                                    ioLatch.countDown();
                                }
                            } catch (IOException e) {
                                log.errorf(e, "Client write failed");
                                IoUtils.safeClose(channel);
                            }
                        }
                    });
                    channel.resumeReads();
                    channel.resumeWrites();
                    clientOpened.set(true);
                }
            }, null, OptionMap.EMPTY);
            //            assertTrue("Read timed out", ioLatch.await(500L, TimeUnit.MILLISECONDS));
            //            assertTrue("Close timed out", closeLatch.await(500L, TimeUnit.MILLISECONDS));
            //            assertFalse("Client read too much", clientReadTooMuch.get());
            //            assertTrue("Client read OK", clientReadOnceOK.get());
            //            assertTrue("Client read done", clientReadDoneOK.get());
            //            assertTrue("Client write OK", clientWriteOK.get());
            //            assertFalse("Server read too much", serverReadTooMuch.get());
            //            assertTrue("Server read OK", serverReadOnceOK.get());
            //            assertTrue("Server read done", serverReadDoneOK.get());
            //            assertTrue("Server write OK", serverWriteOK.get());
        } finally {
            worker.shutdown();
        }
    }

}
