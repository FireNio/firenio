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

import com.generallycloud.baseio.balance.facade.BalanceFacadeAcceptor;
import com.generallycloud.baseio.balance.facade.BalanceFacadeAcceptorHandler;
import com.generallycloud.baseio.balance.facade.BalanceFacadeAcceptorSEListener;
import com.generallycloud.baseio.balance.reverse.BalanceReverseAcceptor;
import com.generallycloud.baseio.balance.reverse.BalanceReverseAcceptorHandler;
import com.generallycloud.baseio.balance.reverse.BalanceReverseAcceptorSEListener;
import com.generallycloud.baseio.balance.reverse.BalanceReverseLogger;
import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.SilentExceptionCaughtHandle;

//FIXME 增加熔断机制
public class BalanceContext {

    public static final String               BALANCE_CHANNEL_LOST         = "BALANCE_CHANNEL_LOST";
    public static final String               BALANCE_RECEIVE_BROADCAST    = "BALANCE_RECEIVE_BROADCAST";

    private BalanceFacadeAcceptor            balanceFacadeAcceptor        = new BalanceFacadeAcceptor();
    private BalanceReverseAcceptor           balanceReverseAcceptor       = new BalanceReverseAcceptor();
    private BalanceFacadeAcceptorSEListener  balanceFacadeAcceptorSEListener;
    private BalanceReverseAcceptorSEListener balanceReverseAcceptorSEListener;
    private BalanceRouter                    balanceRouter;
    private BalanceReverseAcceptorHandler    balanceReverseAcceptorHandler;
    private BalanceFacadeAcceptorHandler     balanceFacadeAcceptorHandler;
    private ChannelLostFutureFactory         channelLostReadFutureFactory;
    private NoneLoadFutureAcceptor           noneLoadReadFutureAcceptor;
    private FacadeInterceptor                facadeInterceptor;
    private BalanceReverseLogger             balanceReverseLogger;
    private ExceptionCaughtHandle            facadeExceptionCaughtHandle  = new SilentExceptionCaughtHandle();
    private ExceptionCaughtHandle            reverseExceptionCaughtHandle = facadeExceptionCaughtHandle;

    public void initialize() {
        this.balanceFacadeAcceptorSEListener = new BalanceFacadeAcceptorSEListener(this);
        this.balanceReverseAcceptorSEListener = new BalanceReverseAcceptorSEListener(this);
        this.balanceReverseAcceptorHandler = new BalanceReverseAcceptorHandler(this);
    }

    public BalanceFacadeAcceptor getBalanceFacadeAcceptor() {
        return balanceFacadeAcceptor;
    }

    public BalanceFacadeAcceptorHandler getBalanceFacadeAcceptorHandler() {
        return balanceFacadeAcceptorHandler;
    }

    public BalanceFacadeAcceptorSEListener getBalanceFacadeAcceptorSEListener() {
        return balanceFacadeAcceptorSEListener;
    }

    public BalanceReverseAcceptor getBalanceReverseAcceptor() {
        return balanceReverseAcceptor;
    }

    public BalanceReverseAcceptorHandler getBalanceReverseAcceptorHandler() {
        return balanceReverseAcceptorHandler;
    }

    public BalanceReverseAcceptorSEListener getBalanceReverseAcceptorSEListener() {
        return balanceReverseAcceptorSEListener;
    }

    public BalanceRouter getBalanceRouter() {
        return balanceRouter;
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

    public void setBalanceRouter(BalanceRouter balanceRouter) {
        this.balanceRouter = balanceRouter;
    }

    public void setFacadeInterceptor(FacadeInterceptor facadeInterceptor) {
        if (facadeInterceptor == null) {
            throw new IllegalArgumentException("null facadeInterceptor");
        }
        this.facadeInterceptor = facadeInterceptor;
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

    public BalanceReverseLogger getBalanceReverseLogger() {
        return balanceReverseLogger;
    }

    public void setBalanceReverseLogger(BalanceReverseLogger balanceReverseLogger) {
        this.balanceReverseLogger = balanceReverseLogger;
    }

    public void setBalanceFacadeAcceptorHandler(
            BalanceFacadeAcceptorHandler balanceFacadeAcceptorHandler) {
        this.balanceFacadeAcceptorHandler = balanceFacadeAcceptorHandler;
    }

}
