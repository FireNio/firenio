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
package com.generallycloud.baseio.protocol;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.Linkable;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public abstract class AbstractChannelFuture extends AbstractFuture implements ChannelFuture {

    private static final Logger logger = LoggerFactory.getLogger(AbstractChannelFuture.class);

    //FIXME isX 使用 byte & x ?
    protected ByteBuf  buf        = EmptyByteBuf.getInstance();
    protected boolean  isHeartbeat;
    protected boolean  isPING;
    protected boolean  isSilent;
    protected boolean  isValidate = true;
    protected boolean  needSSL;
    protected Linkable next;

    protected AbstractChannelFuture(SocketChannelContext context) {
        super(context);
        this.needSSL = context.isEnableSSL();
    }

    protected ByteBuf allocate(SocketChannel channel, int capacity) {
        return channel.getByteBufAllocator().allocate(capacity);
    }

    protected ByteBuf allocate(SocketChannel channel, int capacity, int maxLimit) {
        return channel.getByteBufAllocator().allocate(capacity, maxLimit);
    }

    @Override
    public ChannelFuture duplicate() {
        return new DuplicateChannelFuture(context, buf.duplicate(), this);
    }

    @Override
    public ChannelFuture flush() {
        flushed = true;
        return this;
    }

    @Override
    public ByteBuf getByteBuf() {
        return buf;
    }

    @Override
    public int getByteBufLimit() {
        return buf.limit();
    }

    @Override
    public Linkable getNext() {
        return next;
    }

    @Override
    public boolean isHeartbeat() {
        return isHeartbeat;
    }

    @Override
    public boolean isPING() {
        return isHeartbeat && isPING;
    }

    @Override
    public boolean isPONG() {
        return isHeartbeat && !isPING;
    }

    @Override
    public boolean isReleased() {
        return buf.isReleased();
    }

    @Override
    public boolean isSilent() {
        return isSilent;
    }

    @Override
    public boolean isValidate() {
        return isValidate;
    }

    @Override
    public boolean isWriteCompleted() {
        return !buf.hasRemaining();
    }

    @Override
    public void onException(SocketSession session, Exception ex) {
        ReleaseUtil.release(this);
        try {
            context.getIoEventHandleAdaptor().exceptionCaught(session, this, ex);
        } catch (Throwable e) {
            logger.debug(e.getMessage(), e);
        }
    }

    @Override
    public void onSuccess(SocketSession session) {
        ReleaseUtil.release(this);
        try {
            context.getIoEventHandleAdaptor().futureSent(session, this);
        } catch (Throwable e) {
            logger.debug(e);
        }
    }

    @Override
    public void release() {
        ReleaseUtil.release(buf);
    }

    @Override
    public void setByteBuf(ByteBuf buf) {
        buf.nioBuffer();
        this.buf = buf;
    }

    @Override
    public void setNext(Linkable next) {
        this.next = next;
    }

    @Override
    public ChannelFuture setPING() {
        this.isPING = true;
        this.isHeartbeat = true;
        return this;
    }

    @Override
    public ChannelFuture setPONG() {
        this.isPING = false;
        this.isHeartbeat = true;
        return this;
    }

    @Override
    public void setSilent(boolean isSilent) {
        this.isSilent = isSilent;
    }

    @Override
    public void setValidate(boolean validate) {
        this.isValidate = validate;
    }

    private void wrapSSL(SocketChannel channel) throws IOException {
        // FIXME 部分情况下可以不在业务线程做wrapssl
        ByteBuf old = this.buf;
        SslHandler handler = channel.getSslHandler();
        try {
            ByteBuf _buf = handler.wrap(channel, old);
            if (_buf == null) {
                throw new IOException("closed ssl");
            }
            this.buf = _buf;
            this.buf.nioBuffer();
        } finally {
            ReleaseUtil.release(old);
        }
    }

    @Override
    public void write(SocketChannel channel) throws IOException {
        if (needSSL) {
            needSSL = false;
            wrapSSL(channel);
        }
        channel.write(buf);
    }

}
