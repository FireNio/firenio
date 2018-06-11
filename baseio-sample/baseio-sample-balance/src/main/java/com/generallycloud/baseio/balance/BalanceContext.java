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

import com.generallycloud.baseio.balance.reverse.ReverseLogger;
import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.SilentExceptionCaughtHandle;

//FIXME 增加熔断机制
public class BalanceContext {

    public static final String       BALANCE_CONTEXT_KEY          = "BALANCE_CONTEXT_KEY";

    private BalanceRouter            balanceRouter;
    private ChannelLostFutureFactory channelLostReadFutureFactory;
    private ChannelAcceptor          facadeAcceptor;
    private ExceptionCaughtHandle    facadeExceptionCaughtHandle  = new SilentExceptionCaughtHandle();
    private FacadeInterceptor        facadeInterceptor;
    private NoneLoadFutureAcceptor   noneLoadReadFutureAcceptor;
    private ChannelAcceptor          reverseAcceptor;
    private ExceptionCaughtHandle    reverseExceptionCaughtHandle = facadeExceptionCaughtHandle;
    private ReverseLogger            reverseLogger;

    public BalanceRouter getBalanceRouter() {
        return balanceRouter;
    }

    public ChannelLostFutureFactory getChannelLostReadFutureFactory() {
        return channelLostReadFutureFactory;
    }

    public ChannelAcceptor getFacadeAcceptor() {
        return facadeAcceptor;
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

    public ChannelAcceptor getReverseAcceptor() {
        return reverseAcceptor;
    }

    public ExceptionCaughtHandle getReverseExceptionCaughtHandle() {
        return reverseExceptionCaughtHandle;
    }

    public ReverseLogger getReverseLogger() {
        return reverseLogger;
    }

    public void setBalanceRouter(BalanceRouter balanceRouter) {
        this.balanceRouter = balanceRouter;
    }

    public void setChannelLostReadFutureFactory(
            ChannelLostFutureFactory channelLostReadFutureFactory) {
        this.channelLostReadFutureFactory = channelLostReadFutureFactory;
    }

    public void setFacadeAcceptor(ChannelAcceptor facadeAcceptor) {
        this.facadeAcceptor = facadeAcceptor;
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

    public void setReverseAcceptor(ChannelAcceptor reverseAcceptor) {
        this.reverseAcceptor = reverseAcceptor;
    }

    public void setReverseExceptionCaughtHandle(
            ExceptionCaughtHandle reverseExceptionCaughtHandle) {
        this.reverseExceptionCaughtHandle = reverseExceptionCaughtHandle;
    }

    public void setReverseLogger(ReverseLogger reverseLogger) {
        this.reverseLogger = reverseLogger;
    }

}
