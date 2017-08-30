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
package com.generallycloud.baseio.container.implementation;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationIoEventHandle;
import com.generallycloud.baseio.container.service.FutureAcceptorService;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 *
 */
public class SystemRedeployServlet extends FutureAcceptorService {

    public SystemRedeployServlet() {
        this.setServiceName("/system-redeploy");
    }

    @Override
    public void accept(SocketSession session, Future future) throws IOException {

        ApplicationIoEventHandle applicationIoEventHandle =  
                (ApplicationIoEventHandle) session.getContext().getIoEventHandleAdaptor();

        AtomicInteger redeployTime = applicationIoEventHandle
                .getApplicationContext().getRedeployTime();
        
        int time = redeployTime.incrementAndGet();

        if (applicationIoEventHandle.redeploy()) {
            future.write("redeploy successful_" + time);
        } else {
            future.write("redeploy failed_" + time);
        }

        session.flush(future);

    }

}
