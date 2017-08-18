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
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.baseio.balance.facade.BalanceFacadeAcceptor;
import com.generallycloud.baseio.balance.facade.BalanceFacadeAcceptorHandler;
import com.generallycloud.baseio.balance.facade.BalanceFacadeSocketSessionFactory;
import com.generallycloud.baseio.balance.facade.SessionIdBalanceFacadeAcceptorHandler;
import com.generallycloud.baseio.balance.reverse.BalanceReverseLogger;
import com.generallycloud.baseio.balance.reverse.BalanceReverseSocketSessionFactory;
import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.balance.router.SimpleNextRouter;
import com.generallycloud.baseio.component.BeatFutureFactory;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SilentExceptionCaughtHandle;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSessionEventListener;
import com.generallycloud.baseio.component.SocketSessionIdleEventListener;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.protocol.ProtocolFactory;

public class BalanceServerBootStrap {

    private ProtocolFactory                      balanceProtocolFactory;
    private ProtocolFactory                      balanceReverseProtocolFactory;
    private ServerConfiguration                  balanceServerConfiguration;
    private ServerConfiguration                  balanceReverseServerConfiguration;
    private List<SocketSessionEventListener>     balanceSessionEventListeners;
    private List<SocketSessionIdleEventListener> balanceSessionIdleEventListeners;
    private List<SocketSessionEventListener>     balanceReverseSessionEventListeners;
    private List<SocketSessionIdleEventListener> balanceReverseSessionIdleEventListeners;
    private BeatFutureFactory                    balanceBeatFutureFactory;
    private BeatFutureFactory                    balanceReverseBeatFutureFactory;
    private ChannelLostFutureFactory             channelLostReadFutureFactory;
    private NoneLoadFutureAcceptor               noneLoadReadFutureAcceptor;
    private ExceptionCaughtHandle                facadeExceptionCaughtHandle;
    private ExceptionCaughtHandle                reverseExceptionCaughtHandle;
    private BalanceRouter                        balanceRouter;
    private SslContext                           sslContext;
    private FacadeInterceptor                    facadeInterceptor;
    private BalanceFacadeAcceptor                balanceFacadeAcceptor;
    private BalanceReverseLogger                 balanceReverseLogger;
    private BalanceFacadeAcceptorHandler         balanceFacadeAcceptorHandler;

    public void startup() throws IOException {

        if (balanceRouter == null) {
            balanceRouter = new SimpleNextRouter();
        }

        if (facadeInterceptor == null) {
            facadeInterceptor = new FacadeInterceptorImpl(5, 50000);
        }

        if (balanceReverseLogger == null) {
            balanceReverseLogger = new BalanceReverseLogger();
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

        BalanceContext balanceContext = new BalanceContext();

        balanceContext.setChannelLostReadFutureFactory(channelLostReadFutureFactory);

        balanceContext.setNoneLoadReadFutureAcceptor(noneLoadReadFutureAcceptor);

        balanceContext.setReverseExceptionCaughtHandle(reverseExceptionCaughtHandle);

        balanceContext.setFacadeExceptionCaughtHandle(facadeExceptionCaughtHandle);

        balanceContext.setBalanceReverseLogger(balanceReverseLogger);

        balanceContext.setFacadeInterceptor(facadeInterceptor);

        balanceContext.setBalanceRouter(balanceRouter);

        if (balanceFacadeAcceptorHandler == null) {
            balanceFacadeAcceptorHandler = new SessionIdBalanceFacadeAcceptorHandler(
                    balanceContext);
        }

        balanceContext.setBalanceFacadeAcceptorHandler(balanceFacadeAcceptorHandler);

        balanceContext.initialize();

        balanceFacadeAcceptor = balanceContext.getBalanceFacadeAcceptor();

        SocketChannelContext balanceChannelContext = getBalanceChannelContext(balanceContext,
                balanceServerConfiguration, balanceProtocolFactory);

        balanceChannelContext.setSocketSessionFactory(new BalanceFacadeSocketSessionFactory());

        SocketChannelContext balanceReverseChannelContext = getBalanceReverseChannelContext(
                balanceContext, balanceReverseServerConfiguration, balanceReverseProtocolFactory);

        balanceReverseChannelContext
                .setSocketSessionFactory(new BalanceReverseSocketSessionFactory());

        balanceFacadeAcceptor.start(balanceContext, balanceChannelContext,
                balanceReverseChannelContext);
    }

    public void stop() {

        if (balanceFacadeAcceptor == null) {
            return;
        }

        balanceFacadeAcceptor.stop();
    }

    private SocketChannelContext getBalanceChannelContext(BalanceContext balanceContext,
            ServerConfiguration configuration, ProtocolFactory protocolFactory) {

        SocketChannelContext context = new NioSocketChannelContext(configuration);

        //		SocketChannelContext context = new AioSocketChannelContext(configuration);

        context.setIoEventHandleAdaptor(balanceContext.getBalanceFacadeAcceptorHandler());

        context.addSessionEventListener(balanceContext.getBalanceFacadeAcceptorSEListener());

        context.setProtocolFactory(protocolFactory);

        context.setBeatFutureFactory(balanceBeatFutureFactory);

        if (balanceSessionEventListeners != null) {
            addSessionEventListener2Context(context, balanceSessionEventListeners);
        }

        if (balanceSessionIdleEventListeners != null) {
            addSessionIdleEventListener2Context(context, balanceSessionIdleEventListeners);
        }

        if (sslContext != null) {
            context.setSslContext(sslContext);
        }

        return context;
    }

    private SocketChannelContext getBalanceReverseChannelContext(BalanceContext balanceContext,
            ServerConfiguration configuration, ProtocolFactory protocolFactory) {

        SocketChannelContext context = new NioSocketChannelContext(configuration);

        //		SocketChannelContext context = new AioSocketChannelContext(configuration);

        context.setIoEventHandleAdaptor(balanceContext.getBalanceReverseAcceptorHandler());

        context.addSessionEventListener(balanceContext.getBalanceReverseAcceptorSEListener());

        context.setProtocolFactory(protocolFactory);

        context.setBeatFutureFactory(balanceReverseBeatFutureFactory);

        if (balanceReverseSessionEventListeners != null) {
            addSessionEventListener2Context(context, balanceReverseSessionEventListeners);
        }

        if (balanceReverseSessionIdleEventListeners != null) {
            addSessionIdleEventListener2Context(context, balanceReverseSessionIdleEventListeners);
        }

        return context;
    }

    public ProtocolFactory getBalanceProtocolFactory() {
        return balanceProtocolFactory;
    }

    public void setBalanceProtocolFactory(ProtocolFactory balanceProtocolFactory) {
        this.balanceProtocolFactory = balanceProtocolFactory;
    }

    public ProtocolFactory getBalanceReverseProtocolFactory() {
        return balanceReverseProtocolFactory;
    }

    public void setBalanceReverseProtocolFactory(ProtocolFactory balanceReverseProtocolFactory) {
        this.balanceReverseProtocolFactory = balanceReverseProtocolFactory;
    }

    public ServerConfiguration getBalanceServerConfiguration() {
        return balanceServerConfiguration;
    }

    public void setBalanceServerConfiguration(ServerConfiguration balanceServerConfiguration) {
        this.balanceServerConfiguration = balanceServerConfiguration;
    }

    public ServerConfiguration getBalanceReverseServerConfiguration() {
        return balanceReverseServerConfiguration;
    }

    public void setBalanceReverseServerConfiguration(
            ServerConfiguration balanceReverseServerConfiguration) {
        this.balanceReverseServerConfiguration = balanceReverseServerConfiguration;
    }

    public void addBalanceSessionIdleEventListener(SocketSessionIdleEventListener listener) {
        if (balanceSessionIdleEventListeners == null) {
            balanceSessionIdleEventListeners = new ArrayList<>();
        }
        balanceSessionIdleEventListeners.add(listener);
    }

    public void addBalanceSessionEventListener(SocketSessionEventListener listener) {
        if (balanceSessionEventListeners == null) {
            balanceSessionEventListeners = new ArrayList<>();
        }
        balanceSessionEventListeners.add(listener);
    }

    public void addBalanceReverseSessionIdleEventListener(SocketSessionIdleEventListener listener) {
        if (balanceReverseSessionIdleEventListeners == null) {
            balanceReverseSessionIdleEventListeners = new ArrayList<>();
        }
        balanceReverseSessionIdleEventListeners.add(listener);
    }

    public void addBalanceReverseSessionEventListener(SocketSessionEventListener listener) {
        if (balanceReverseSessionEventListeners == null) {
            balanceReverseSessionEventListeners = new ArrayList<>();
        }
        balanceReverseSessionEventListeners.add(listener);
    }

    private void addSessionEventListener2Context(SocketChannelContext context,
            List<SocketSessionEventListener> listeners) {
        for (SocketSessionEventListener l : listeners) {
            context.addSessionEventListener(l);
        }
    }

    private void addSessionIdleEventListener2Context(SocketChannelContext context,
            List<SocketSessionIdleEventListener> listeners) {
        for (SocketSessionIdleEventListener l : listeners) {
            context.addSessionIdleEventListener(l);
        }
    }

    public BeatFutureFactory getBalanceBeatFutureFactory() {
        return balanceBeatFutureFactory;
    }

    public BeatFutureFactory getBalanceReverseBeatFutureFactory() {
        return balanceReverseBeatFutureFactory;
    }

    public void setBalanceBeatFutureFactory(BeatFutureFactory balanceBeatFutureFactory) {
        this.balanceBeatFutureFactory = balanceBeatFutureFactory;
    }

    public void setBalanceReverseBeatFutureFactory(
            BeatFutureFactory balanceReverseBeatFutureFactory) {
        this.balanceReverseBeatFutureFactory = balanceReverseBeatFutureFactory;
    }

    public void setBalanceRouter(BalanceRouter balanceRouter) {
        this.balanceRouter = balanceRouter;
    }

    public BalanceRouter getBalanceRouter() {
        return balanceRouter;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    public ChannelLostFutureFactory getChannelLostReadFutureFactory() {
        return channelLostReadFutureFactory;
    }

    public void setChannelLostReadFutureFactory(
            ChannelLostFutureFactory channelLostReadFutureFactory) {
        this.channelLostReadFutureFactory = channelLostReadFutureFactory;
    }

    public NoneLoadFutureAcceptor getNoneLoadReadFutureAcceptor() {
        return noneLoadReadFutureAcceptor;
    }

    public void setNoneLoadReadFutureAcceptor(NoneLoadFutureAcceptor noneLoadReadFutureAcceptor) {
        this.noneLoadReadFutureAcceptor = noneLoadReadFutureAcceptor;
    }

    public FacadeInterceptor getFacadeInterceptor() {
        return facadeInterceptor;
    }

    public void setFacadeInterceptor(FacadeInterceptor facadeInterceptor) {
        this.facadeInterceptor = facadeInterceptor;
    }

    public BalanceReverseLogger getBalanceReverseLogger() {
        return balanceReverseLogger;
    }

    public void setBalanceReverseLogger(BalanceReverseLogger balanceReverseLogger) {
        this.balanceReverseLogger = balanceReverseLogger;
    }

    public BalanceFacadeAcceptorHandler getBalanceFacadeAcceptorHandler() {
        return balanceFacadeAcceptorHandler;
    }

    public void setBalanceFacadeAcceptorHandler(
            BalanceFacadeAcceptorHandler balanceFacadeAcceptorHandler) {
        this.balanceFacadeAcceptorHandler = balanceFacadeAcceptorHandler;
    }

    public ExceptionCaughtHandle getFacadeExceptionCaughtHandle() {
        return facadeExceptionCaughtHandle;
    }

    public void setFacadeExceptionCaughtHandle(ExceptionCaughtHandle facadeExceptionCaughtHandle) {
        this.facadeExceptionCaughtHandle = facadeExceptionCaughtHandle;
    }

    public ExceptionCaughtHandle getReverseExceptionCaughtHandle() {
        return reverseExceptionCaughtHandle;
    }

    public void setReverseExceptionCaughtHandle(
            ExceptionCaughtHandle reverseExceptionCaughtHandle) {
        this.reverseExceptionCaughtHandle = reverseExceptionCaughtHandle;
    }

}
