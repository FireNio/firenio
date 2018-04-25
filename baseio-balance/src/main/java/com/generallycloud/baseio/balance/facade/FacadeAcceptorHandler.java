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

import com.generallycloud.baseio.balance.BalanceContext;
import com.generallycloud.baseio.balance.BalanceFuture;
import com.generallycloud.baseio.balance.FacadeInterceptor;
import com.generallycloud.baseio.balance.NoneLoadFutureAcceptor;
import com.generallycloud.baseio.balance.reverse.ReverseLogger;
import com.generallycloud.baseio.balance.reverse.ReverseSocketSession;
import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

public abstract class FacadeAcceptorHandler extends IoEventHandleAdaptor {

    private Logger                 logger = LoggerFactory.getLogger(getClass());
    private BalanceRouter          balanceRouter;
    private FacadeInterceptor      facadeInterceptor;
    private ReverseLogger          reverseLogger;
    private ExceptionCaughtHandle  exceptionCaughtHandle;
    private NoneLoadFutureAcceptor noneLoadReadFutureAcceptor;

    @Override
    protected void initialize(SocketChannelContext context) throws Exception {
        BalanceContext balanceContext = (BalanceContext) context
                .getAttribute(BalanceContext.BALANCE_CONTEXT_KEY);
        this.balanceRouter = balanceContext.getBalanceRouter();
        this.facadeInterceptor = balanceContext.getFacadeInterceptor();
        this.reverseLogger = balanceContext.getReverseLogger();
        this.exceptionCaughtHandle = balanceContext.getFacadeExceptionCaughtHandle();
        this.noneLoadReadFutureAcceptor = balanceContext.getNoneLoadReadFutureAcceptor();
        super.initialize(context);
    }

    @Override
    public void accept(SocketSession session, Future future) throws Exception {
        FacadeSocketSession fs = (FacadeSocketSession) session;
        BalanceFuture f = (BalanceFuture) future;
        if (facadeInterceptor.intercept(fs, f)) {
            logger.info("msg intercepted [ {} ], msg: {}", fs.getRemoteSocketAddress(), f);
            return;
        }
        ReverseSocketSession rs = balanceRouter.getRouterSession(fs, f);
        if (rs == null || rs.isClosed()) {
            noneLoadReadFutureAcceptor.accept(fs, f, reverseLogger);
            return;
        }
        doAccept(fs, rs, f);
    }

    protected abstract void doAccept(FacadeSocketSession fs, ReverseSocketSession rs,
            BalanceFuture future);

    protected void logDispatchMsg(FacadeSocketSession fs, ReverseSocketSession rs,
            BalanceFuture f) {
        logger.info("dispatch msg: F[{}],T[{}],msg:{}", fs.getRemoteSocketAddress(),
                rs.getRemoteSocketAddress(), f);
    }

    @Override
    public void exceptionCaught(SocketSession session, Future future, Exception ex) {
        exceptionCaughtHandle.exceptionCaught(session, future, ex);
    }

}
