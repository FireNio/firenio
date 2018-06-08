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
import com.generallycloud.baseio.balance.facade.FacadeSocketSessionFactory;
import com.generallycloud.baseio.balance.facade.SessionIdFacadeAcceptorHandler;
import com.generallycloud.baseio.balance.reverse.ReverseAcceptorHandler;
import com.generallycloud.baseio.balance.reverse.ReverseAcceptorSEListener;
import com.generallycloud.baseio.balance.reverse.ReverseLogger;
import com.generallycloud.baseio.balance.reverse.ReverseSocketSessionFactory;
import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.balance.router.SimpleNextRouter;
import com.generallycloud.baseio.common.Assert;
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
    
    public void addFacadeAcceptor(FacadeAcceptor facadeAcceptor) {
        this.balanceContext.addFacadeAcceptor(facadeAcceptor);
    }

    public IoEventHandleAdaptor getFacadeAcceptorHandler() {
        return facadeAcceptorHandler;
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
        Assert.notNull(reverseChannelContext, "reverseChannelContext is null");
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
            facadeAcceptorHandler = new SessionIdFacadeAcceptorHandler();
        }
        if (reverseAcceptorHandler == null) {
            reverseAcceptorHandler = new ReverseAcceptorHandler();
        }
        balanceContext.setNoneLoadReadFutureAcceptor(noneLoadReadFutureAcceptor);
        balanceContext.setReverseExceptionCaughtHandle(reverseExceptionCaughtHandle);
        balanceContext.setReverseLogger(reverseLogger);
        balanceContext.setFacadeInterceptor(facadeInterceptor);
        balanceContext.setReverseAcceptor(new ChannelAcceptor(reverseChannelContext));
        
        reverseChannelContext.setAttribute(BalanceContext.BALANCE_CONTEXT_KEY, balanceContext);
        reverseChannelContext.setIoEventHandle(reverseAcceptorHandler);
        reverseChannelContext
                .addSessionEventListener(new ReverseAcceptorSEListener(balanceContext));
        reverseChannelContext.setSocketSessionFactory(new ReverseSocketSessionFactory());
        balanceContext.getReverseAcceptor().bind();
        LoggerUtil.prettyLog(logger, "Reverse Acceptor startup completed ...");
        for(FacadeAcceptor facadeAcceptor : balanceContext.getFacadeAcceptors()){
            ChannelAcceptor acceptor = facadeAcceptor.getAcceptor();
            ChannelContext context = acceptor.getContext();
            context.setAttribute(BalanceContext.BALANCE_CONTEXT_KEY, balanceContext);
            context.setIoEventHandle(facadeAcceptorHandler);
            context.addSessionEventListener(new FacadeAcceptorSEListener(balanceContext));
            context.setSocketSessionFactory(new FacadeSocketSessionFactory(facadeAcceptor));
            acceptor.bind();
            LoggerUtil.prettyLog(logger, "Facade Acceptor startup completed ...");
        }
    }

    public void stop() {
        for(FacadeAcceptor acceptor : balanceContext.getFacadeAcceptors()){
            acceptor.unbind();
        }
        CloseUtil.unbind(balanceContext.getReverseAcceptor());
    }

}
