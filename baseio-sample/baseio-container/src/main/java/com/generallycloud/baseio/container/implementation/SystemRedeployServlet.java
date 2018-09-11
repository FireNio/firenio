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

import com.generallycloud.baseio.component.FrameAcceptor;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.ApplicationIoEventHandle;
import com.generallycloud.baseio.protocol.Frame;

/**
 * @author wangkai
 *
 */
public class SystemRedeployServlet implements FrameAcceptor {

    @Override
    public void accept(NioSocketChannel ch, Frame frame) throws IOException {
        ApplicationIoEventHandle applicationIoEventHandle = 
                (ApplicationIoEventHandle) ch.getIoEventHandle();
        AtomicInteger redeployTime = applicationIoEventHandle.getRedeployTime();
        long startTime = System.currentTimeMillis();
        Charset charset = ch.getCharset();
        if (applicationIoEventHandle.redeploy()) {
            int time = redeployTime.incrementAndGet();
            frame.write("redeploy successful_", charset);
            frame.write(String.valueOf(time), charset);
            frame.write(",spent time:", charset);
            frame.write(String.valueOf(System.currentTimeMillis() - startTime), charset);
        } else {
            frame.write("redeploy failed,spent time:", charset);
            frame.write(String.valueOf(System.currentTimeMillis() - startTime), charset);
        }
        ch.flush(frame);
    }

}
