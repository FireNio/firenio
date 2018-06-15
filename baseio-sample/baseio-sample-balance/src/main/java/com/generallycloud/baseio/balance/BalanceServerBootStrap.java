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

import com.generallycloud.baseio.balance.facade.FacadeAcceptorSEListener;
import com.generallycloud.baseio.balance.facade.FacadeSocketChannelFactory;
import com.generallycloud.baseio.balance.facade.ChannelIdFacadeAcceptorHandler;
import com.generallycloud.baseio.balance.reverse.ReverseAcceptorHandler;
import com.generallycloud.baseio.balance.reverse.ReverseAcceptorSEListener;
import com.generallycloud.baseio.balance.reverse.ReverseLogger;
import com.generallycloud.baseio.balance.reverse.ReverseSocketChannelFactory;
import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.balance.router.SimpleNextRouter;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.SilentExceptionCaughtHandle;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class BalanceServerBootStrap {

    private BalanceContext         balanceContext = new BalanceContext();
    private IoEventHandleAdaptor   facadeAcceptorHandler;
    private ChannelContext         facadeChannelContext;
    private ExceptionCaughtHandle  facadeExceptionCaughtHandle;
    private FacadeInterceptor      facadeInterceptor;
    private Logger                 logger         = LoggerFactory.getLogger(getClass());
    private NoneLoadFutureAcceptor noneLoadReadFutureAcceptor;
    private IoEventHandleAdaptor   reverseAcceptorHandler;
    private ChannelContext         reverseChannelContext;
    private ExceptionCaughtHandle  reverseExceptionCaughtHandle;
    private ReverseLogger          reverseLogger;

    public BalanceContext getBalanceContext() {
        return balanceContext;
    }

    public IoEventHandleAdaptor getFacadeAcceptorHandler() {
        return facadeAcceptorHandler;
    }

    public ChannelContext getFacadeChannelContext() {
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

    public ChannelContext getReverseChannelContext() {
        return reverseChannelContext;
    }

    public ExceptionCaughtHandle getReverseExceptionCaughtHandle() {
        return reverseExceptionCaughtHandle;
    }

    public ReverseLogger getReverseLogger() {
        return reverseLogger;
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

    public void setFacadeChannelContext(ChannelContext facadeChannelContext) {
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

    public void setReverseChannelContext(ChannelContext reverseChannelContext) {
        this.reverseChannelContext = reverseChannelContext;
    }

    public void setReverseExceptionCaughtHandle(
            ExceptionCaughtHandle reverseExceptionCaughtHandle) {
        this.reverseExceptionCaughtHandle = reverseExceptionCaughtHandle;
    }

    public void setReverseLogger(ReverseLogger reverseLogger) {
        this.reverseLogger = reverseLogger;
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
        if (reverseLogger == null) {
            reverseLogger = new ReverseLogger();
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
            facadeAcceptorHandler = new ChannelIdFacadeAcceptorHandler();
        }
        if (reverseAcceptorHandler == null) {
            reverseAcceptorHandler = new ReverseAcceptorHandler();
        }
        balanceContext.setNoneLoadReadFutureAcceptor(noneLoadReadFutureAcceptor);
        balanceContext.setReverseExceptionCaughtHandle(reverseExceptionCaughtHandle);
        balanceContext.setFacadeExceptionCaughtHandle(facadeExceptionCaughtHandle);
        balanceContext.setReverseLogger(reverseLogger);
        balanceContext.setFacadeInterceptor(facadeInterceptor);
        balanceContext.setFacadeAcceptor(new ChannelAcceptor(facadeChannelContext));
        balanceContext.setReverseAcceptor(new ChannelAcceptor(reverseChannelContext));
        facadeChannelContext.setAttribute(BalanceContext.BALANCE_CONTEXT_KEY, balanceContext);
        facadeChannelContext.setIoEventHandle(facadeAcceptorHandler);
        facadeChannelContext.addChannelEventListener(new FacadeAcceptorSEListener(balanceContext));
        facadeChannelContext.setSocketChannelFactory(new FacadeSocketChannelFactory());
        reverseChannelContext.setAttribute(BalanceContext.BALANCE_CONTEXT_KEY, balanceContext);
        reverseChannelContext.setIoEventHandle(reverseAcceptorHandler);
        reverseChannelContext
                .addChannelEventListener(new ReverseAcceptorSEListener(balanceContext));
        reverseChannelContext.setSocketChannelFactory(new ReverseSocketChannelFactory());
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
