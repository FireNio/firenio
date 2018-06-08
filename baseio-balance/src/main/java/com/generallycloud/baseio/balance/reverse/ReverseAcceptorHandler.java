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
package com.generallycloud.baseio.balance.reverse;

import java.util.List;

import com.generallycloud.baseio.balance.BalanceContext;
import com.generallycloud.baseio.balance.BalanceFuture;
import com.generallycloud.baseio.balance.FacadeAcceptor;
import com.generallycloud.baseio.balance.facade.FacadeSocketSession;
import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

public class ReverseAcceptorHandler extends IoEventHandleAdaptor {

    private Logger                logger = LoggerFactory.getLogger(getClass());
    private BalanceRouter         balanceRouter;
    private List<FacadeAcceptor>  facadeAcceptors;
    private ExceptionCaughtHandle exceptionCaughtHandle;
    private ReverseLogger         reverseLogger;

    @Override
    protected void initialize(ChannelContext context) throws Exception {
        BalanceContext balanceContext = (BalanceContext) context
                .getAttribute(BalanceContext.BALANCE_CONTEXT_KEY);
        this.balanceRouter = balanceContext.getBalanceRouter();
        this.reverseLogger = balanceContext.getReverseLogger();
        this.facadeAcceptors = balanceContext.getFacadeAcceptors();
        this.exceptionCaughtHandle = balanceContext.getReverseExceptionCaughtHandle();
        super.initialize(context);
    }

    @Override
    public void accept(SocketSession session, Future future) throws Exception {
        ReverseSocketSession rs = (ReverseSocketSession) session;
        BalanceFuture f = (BalanceFuture) future;
        if (f.isBroadcast()) {
            for (FacadeAcceptor facadeAcceptor : facadeAcceptors) {
                ChannelAcceptor acceptor = facadeAcceptor.getAcceptor();
                if(acceptor.getContext().getProtocolCodec().getProtocolId()
                        .equals(session.getProtocolCodec().getProtocolId())){
                    acceptor.broadcast(f.translate(session));
                }else{
                    BalanceFuture nf = facadeAcceptor.getFutureTranslator().translateOut(rs, f);
                    acceptor.broadcast(nf);
                }
            }
            reverseLogger.logBroadcast(session, future, logger);
            return;
        }
        FacadeSocketSession response = balanceRouter.getClientSession(f.getSessionKey());
        if (response == null || response.isClosed()) {
            reverseLogger.logPushLost(session, future, logger);
            return;
        }
        response.writeAndFlush(rs, f);
        reverseLogger.logPush(session, response, future, logger);
    }

    @Override
    public void exceptionCaught(SocketSession session, Future future, Exception ex) {
        exceptionCaughtHandle.exceptionCaught(session, future, ex);
    }

}
