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
package com.generallycloud.baseio.balance;

import java.io.IOException;

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.balance.facade.BalanceFacadeAcceptorSEListener;
import com.generallycloud.baseio.balance.facade.BalanceFacadeSocketSessionFactory;
import com.generallycloud.baseio.balance.facade.SessionIdFacadeAcceptorHandler;
import com.generallycloud.baseio.balance.reverse.BalanceReverseAcceptorHandler;
import com.generallycloud.baseio.balance.reverse.BalanceReverseAcceptorSEListener;
import com.generallycloud.baseio.balance.reverse.ReverseLogger;
import com.generallycloud.baseio.balance.reverse.BalanceReverseSocketSessionFactory;
import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.balance.router.SimpleNextRouter;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.SilentExceptionCaughtHandle;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class BalanceServerBootStrap {

    private Logger                 logger         = LoggerFactory.getLogger(getClass());
    private BalanceContext         balanceContext = new BalanceContext();
    private ReverseLogger   balanceReverseLogger;
    private IoEventHandleAdaptor   facadeAcceptorHandler;
    private SocketChannelContext   facadeChannelContext;
    private ExceptionCaughtHandle  facadeExceptionCaughtHandle;
    private FacadeInterceptor      facadeInterceptor;
    private NoneLoadFutureAcceptor noneLoadReadFutureAcceptor;
    private IoEventHandleAdaptor   reverseAcceptorHandler;
    private SocketChannelContext   reverseChannelContext;
    private ExceptionCaughtHandle  reverseExceptionCaughtHandle;

    public BalanceContext getBalanceContext() {
        return balanceContext;
    }

    public ReverseLogger getBalanceReverseLogger() {
        return balanceReverseLogger;
    }

    public IoEventHandleAdaptor getFacadeAcceptorHandler() {
        return facadeAcceptorHandler;
    }

    public SocketChannelContext getFacadeChannelContext() {
        return facadeChannelContext;
    }

    public ExceptionCaughtHandle getFacadeExceptionCaughtHandle() {
        return facadeExceptionCaughtHandle;
    }

    public FacadeInterceptor getFacadeInterceptor() {
        return facadeInterceptor;
    }

    public NoneLoadFutureAcceptor getNoneLoadReadFutureAcceptor() {
        return noneLoadReadFutureAcceptor;
    }

    public IoEventHandleAdaptor getReverseAcceptorHandler() {
        return reverseAcceptorHandler;
    }

    public SocketChannelContext getReverseChannelContext() {
        return reverseChannelContext;
    }

    public ExceptionCaughtHandle getReverseExceptionCaughtHandle() {
        return reverseExceptionCaughtHandle;
    }

    public void setBalanceReverseLogger(ReverseLogger balanceReverseLogger) {
        this.balanceReverseLogger = balanceReverseLogger;
    }

    public void setBalanceRouter(BalanceRouter balanceRouter) {
        this.balanceContext.setBalanceRouter(balanceRouter);
    }

    public void setChannelLostReadFutureFactory(
            ChannelLostFutureFactory channelLostReadFutureFactory) {
        balanceContext.setChannelLostReadFutureFactory(channelLostReadFutureFactory);
    }

    public void setFacadeAcceptorHandler(IoEventHandleAdaptor facadeAcceptorHandler) {
        this.facadeAcceptorHandler = facadeAcceptorHandler;
    }

    public void setFacadeChannelContext(SocketChannelContext facadeChannelContext) {
        this.facadeChannelContext = facadeChannelContext;
    }

    public void setFacadeExceptionCaughtHandle(ExceptionCaughtHandle facadeExceptionCaughtHandle) {
        this.facadeExceptionCaughtHandle = facadeExceptionCaughtHandle;
    }

    public void setFacadeInterceptor(FacadeInterceptor facadeInterceptor) {
        this.facadeInterceptor = facadeInterceptor;
    }

    public void setNoneLoadReadFutureAcceptor(NoneLoadFutureAcceptor noneLoadReadFutureAcceptor) {
        this.noneLoadReadFutureAcceptor = noneLoadReadFutureAcceptor;
    }

    public void setReverseAcceptorHandler(IoEventHandleAdaptor reverseAcceptorHandler) {
        this.reverseAcceptorHandler = reverseAcceptorHandler;
    }

    public void setReverseChannelContext(SocketChannelContext reverseChannelContext) {
        this.reverseChannelContext = reverseChannelContext;
    }

    public void setReverseExceptionCaughtHandle(
            ExceptionCaughtHandle reverseExceptionCaughtHandle) {
        this.reverseExceptionCaughtHandle = reverseExceptionCaughtHandle;
    }

    public void startup() throws IOException {
        if (facadeChannelContext == null) {
            throw new IllegalArgumentException("facadeChannelContext is null");
        }
        if (reverseChannelContext == null) {
            throw new IllegalArgumentException("reverseChannelContext is null");
        }
        if (balanceContext.getBalanceRouter() == null) {
            balanceContext.setBalanceRouter(new SimpleNextRouter());
        }
        if (facadeInterceptor == null) {
            facadeInterceptor = new FacadeInterceptorImpl(5, 50000);
        }
        if (balanceReverseLogger == null) {
            balanceReverseLogger = new ReverseLogger();
        }
        if (noneLoadReadFutureAcceptor == null) {
            noneLoadReadFutureAcceptor = new DefaultNoneLoadFutureAcceptor();
        }
        if (facadeExceptionCaughtHandle == null) {
            facadeExceptionCaughtHandle = new SilentExceptionCaughtHandle();
        }
        if (reverseExceptionCaughtHandle == null) {
            reverseExceptionCaughtHandle = new SilentExceptionCaughtHandle();
        }
        if (facadeAcceptorHandler == null) {
            facadeAcceptorHandler = new SessionIdFacadeAcceptorHandler();
        }
        if (reverseAcceptorHandler == null) {
            reverseAcceptorHandler = new BalanceReverseAcceptorHandler();
        }
        balanceContext.setNoneLoadReadFutureAcceptor(noneLoadReadFutureAcceptor);
        balanceContext.setReverseExceptionCaughtHandle(reverseExceptionCaughtHandle);
        balanceContext.setFacadeExceptionCaughtHandle(facadeExceptionCaughtHandle);
        balanceContext.setReverseLogger(balanceReverseLogger);
        balanceContext.setFacadeInterceptor(facadeInterceptor);
        balanceContext.setFacadeAcceptor(new SocketChannelAcceptor(facadeChannelContext));
        balanceContext.setReverseAcceptor(new SocketChannelAcceptor(reverseChannelContext));
        facadeChannelContext.setAttribute(BalanceContext.BALANCE_CONTEXT_KEY, balanceContext);
        facadeChannelContext.setIoEventHandleAdaptor(facadeAcceptorHandler);
        facadeChannelContext.addSessionEventListener(new BalanceFacadeAcceptorSEListener(balanceContext));
        facadeChannelContext.setSocketSessionFactory(new BalanceFacadeSocketSessionFactory());
        reverseChannelContext.setAttribute(BalanceContext.BALANCE_CONTEXT_KEY, balanceContext);
        reverseChannelContext.setIoEventHandleAdaptor(reverseAcceptorHandler);
        reverseChannelContext.addSessionEventListener(new BalanceReverseAcceptorSEListener(balanceContext));
        reverseChannelContext.setSocketSessionFactory(new BalanceReverseSocketSessionFactory());
        balanceContext.getFacadeAcceptor().bind();
        LoggerUtil.prettyLog(logger, "Facade Acceptor startup completed ...");
        balanceContext.getReverseAcceptor().bind();
        LoggerUtil.prettyLog(logger, "Reverse Acceptor startup completed ...");
    }

    public void stop() {
        CloseUtil.unbind(balanceContext.getFacadeAcceptor());
        CloseUtil.unbind(balanceContext.getReverseAcceptor());
    }
    
}
