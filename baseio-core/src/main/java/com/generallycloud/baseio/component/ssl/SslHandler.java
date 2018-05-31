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
package com.generallycloud.baseio.component.ssl;

import java.io.IOException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.DefaultChannelFuture;

public class SslHandler {
    
    public static final String SSL_HANDlER_EVENT_LOOP_KEY = "SSL_HANDlER_EVENT_LOOP_KEY";

    private ChannelFuture forgeFuture = new DefaultChannelFuture(EmptyByteBuf.get(), true);

    private ByteBuf       tempDst;

    private ByteBuf getTempDst(SSLEngine engine) {
        if (tempDst == null) {
            synchronized (this) {
                if (tempDst != null) {
                    return tempDst;
                }
                ByteBufAllocator allocator = UnpooledByteBufAllocator.getDirect();
                int packetBufferSize = engine.getSession().getPacketBufferSize();
                tempDst = allocator.allocate(packetBufferSize * 2);
            }
        }
        return tempDst;
    }

    private ByteBuf allocate(NioSocketChannel channel, int capacity) {
        return channel.allocator().allocate(capacity);
    }

    public ByteBuf wrap(NioSocketChannel channel, ByteBuf src) throws IOException {
        SSLEngine engine = channel.getSSLEngine();
        ByteBuf dst = getTempDst(engine);
        ByteBuf out = null;
        try {
            for (;;) {
                dst.clear();
                SSLEngineResult result = engine.wrap(src.nioBuffer(), dst.nioBuffer());
                Status status = result.getStatus();
                HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                synchByteBuf(result, src, dst);
                if (status == Status.CLOSED) {
                    return gc(channel, dst.flip());
                }
                if (handshakeStatus == HandshakeStatus.NOT_HANDSHAKING) {
                    if (src.hasRemaining()) {
                        if (out == null) {
                            int outLength = ((src.limit() / src.position()) + 1)
                                    * (dst.position() - src.position()) + src.limit();
                            out = allocate(channel, outLength);
                        }
                        out.read(dst.flip());
                        continue;
                    }
                    if (out != null) {
                        out.read(dst.flip());
                        return out.flip();
                    }
                    return gc(channel, dst.flip());
                } else {
                    if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                        if (out != null) {
                            out.read(dst.flip());
                            return out.flip();
                        }
                        return gc(channel, dst.flip());
                    } else if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                        if (out == null) {
                            out = allocate(channel, 256);
                        }
                        out.read(dst.flip());
                        continue;
                    } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                        channel.finishHandshake(null);
                        out.read(dst.flip());
                        return out.flip();
                    } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                        runDelegatedTasks(engine);
                        continue;
                    }
                }
            }
        } catch (Throwable e) {
            if (out != null) {
                out.release(out.getReleaseVersion());
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }

    //FIXME 部分buf不需要gc
    private ByteBuf gc(NioSocketChannel channel, ByteBuf buf) throws IOException {
        ByteBuf out = allocate(channel, buf.limit());
        try {
            out.read(buf);
        } catch (Exception e) {
            out.release(out.getReleaseVersion());
            throw e;
        }
        return out.flip();
    }

    public ByteBuf unwrap(NioSocketChannel channel, ByteBuf src) throws IOException {
        SSLEngine sslEngine = channel.getSSLEngine();
        ByteBuf dst = getTempDst(sslEngine);
        for (;;) {
            dst.clear();
            ByteBuf remainingBuf = channel.getRemainingBuf();
            if (remainingBuf != null
                    && sslEngine.getHandshakeStatus() == HandshakeStatus.NOT_HANDSHAKING) {
                dst.read(remainingBuf);
            }
            SSLEngineResult result = sslEngine.unwrap(src.nioBuffer(), dst.nioBuffer());
            HandshakeStatus handshakeStatus = result.getHandshakeStatus();
            if (handshakeStatus == HandshakeStatus.NOT_HANDSHAKING) {
                synchByteBuf(result, src, dst);
                return dst.flip();
            } else {
                synchByteBuf(result, src, dst);
                if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                    channel.flushChannelFuture(forgeFuture.duplicate());
                    return null;
                } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                    runDelegatedTasks(sslEngine);
                    continue;
                } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                    channel.finishHandshake(null);
                    return null;
                } else if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                    return null;
                }
            }
        }
    }

    private void synchByteBuf(SSLEngineResult result, ByteBuf src, ByteBuf dst) {
        //FIXME 同步。。。。。
        src.reverse();
        dst.reverse();
        //		int bytesConsumed = result.bytesConsumed();
        //		int bytesProduced = result.bytesProduced();
        //		
        //		if (bytesConsumed > 0) {
        //			src.skipBytes(bytesConsumed);
        //		}
        //
        //		if (bytesProduced > 0) {
        //			dst.skipBytes(bytesProduced);
        //		}
    }

    private void runDelegatedTasks(SSLEngine engine) {
        for (;;) {
            Runnable task = engine.getDelegatedTask();
            if (task == null) {
                break;
            }
            task.run();
        }
    }
}
