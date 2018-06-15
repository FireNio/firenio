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
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.component.FutureAcceptor;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.ApplicationIoEventHandle;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 *
 */
public class SystemRedeployServlet implements FutureAcceptor {

    @Override
    public void accept(NioSocketChannel channel, Future future) throws IOException {
        ApplicationIoEventHandle applicationIoEventHandle = (ApplicationIoEventHandle) channel
                .getContext().getIoEventHandle();
        AtomicInteger redeployTime = applicationIoEventHandle.getRedeployTime();
        long startTime = System.currentTimeMillis();
        Charset charset = channel.getEncoding();
        if (applicationIoEventHandle.redeploy()) {
            int time = redeployTime.incrementAndGet();
            future.write("redeploy successful_", charset);
            future.write(String.valueOf(time), charset);
            future.write(",spent time:", charset);
            future.write(String.valueOf(System.currentTimeMillis() - startTime), charset);
        } else {
            future.write("redeploy failed,spent time:", charset);
            future.write(String.valueOf(System.currentTimeMillis() - startTime), charset);
        }
        channel.flush(future);
    }

}
