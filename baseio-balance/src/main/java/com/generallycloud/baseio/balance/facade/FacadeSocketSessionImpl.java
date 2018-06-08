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
package com.generallycloud.baseio.balance.facade;

import com.generallycloud.baseio.balance.BalanceFuture;
import com.generallycloud.baseio.balance.FacadeAcceptor;
import com.generallycloud.baseio.balance.reverse.ReverseSocketSession;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.SocketSessionImpl;

public class FacadeSocketSessionImpl extends SocketSessionImpl implements FacadeSocketSession {

    private int                  msg_size;

    private long                 next_check_time;

    private ReverseSocketSession reverseSocketSession;

    private FacadeAcceptor       acceptor;

    public FacadeSocketSessionImpl(FacadeAcceptor acceptor, NioSocketChannel channel) {
        super(channel);
        this.acceptor = acceptor;
    }

    @Override
    public ReverseSocketSession getReverseSocketSession() {
        return reverseSocketSession;
    }

    @Override
    public boolean overfulfil(int size) {
        long now = System.currentTimeMillis();
        if (now > next_check_time) {
            next_check_time = now + 1000;
            msg_size = 0;
        }
        return ++msg_size > size;
    }

    @Override
    public void writeAndFlush(ReverseSocketSession rs, BalanceFuture future) {
        if (getProtocolCodec().getProtocolId().equals(rs.getProtocolCodec().getProtocolId())) {
            flush(future.translate(rs));
        } else {
            BalanceFuture nf = acceptor.getFutureTranslator().translateOut(rs, future);
            flush(nf);
        }
    }
    
    @Override
    public FacadeAcceptor getAcceptor() {
        return acceptor;
    }

    @Override
    public void setReverseSocketSession(ReverseSocketSession reverseSocketSession) {
        this.reverseSocketSession = reverseSocketSession;
    }

    @Override
    public Object getSessionKey() {
        return getSessionId();
    }

}
