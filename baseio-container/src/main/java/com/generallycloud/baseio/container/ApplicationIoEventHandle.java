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
package com.generallycloud.baseio.container;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.service.FutureAcceptorFilterWrapper;
import com.generallycloud.baseio.container.service.FutureAcceptorService;
import com.generallycloud.baseio.protocol.Future;

public class ApplicationIoEventHandle extends IoEventHandleAdaptor {

    private ApplicationContext          applicationContext;

    private FutureAcceptorService       appRedeployService;

    private volatile boolean            deploying = true;

    private ExceptionCaughtHandle       exceptionCaughtHandle;

    private ExceptionCaughtHandle       ioExceptionCaughtHandle;

    private FutureAcceptorFilterWrapper rootFilter;

    public ApplicationIoEventHandle(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void accept(SocketSession session, Future future) throws Exception {

        if (deploying) {
            appRedeployService.accept(session, future);
            return;
        }

        try {
            rootFilter.accept(session, future);
        } catch (Exception e) {
            exceptionCaughtHandle.exceptionCaught(session, future, e);
        }

    }

    @Override
    protected void destroy(SocketChannelContext context) throws Exception {
        LifeCycleUtil.stop(applicationContext);
        this.deploying = true;
        super.destroy(context);
    }

    @Override
    public void exceptionCaught(SocketSession session, Future future, Exception ex) {
        ioExceptionCaughtHandle.exceptionCaught(session, future, ex);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    public boolean redeploy(){
        return applicationContext.redeploy();
    }

    @Override
    protected void initialize(SocketChannelContext context) throws Exception {

        ApplicationContext applicationContext = this.applicationContext;

        LifeCycleUtil.start(applicationContext);

        this.appRedeployService = applicationContext.getAppRedeployService();

        this.exceptionCaughtHandle = applicationContext.getExceptionCaughtHandle();

        this.ioExceptionCaughtHandle = applicationContext.getIoExceptionCaughtHandle();

        this.rootFilter = applicationContext.getRootFutureAcceptorFilter();

        this.deploying = false;

        super.initialize(context);
    }

}
