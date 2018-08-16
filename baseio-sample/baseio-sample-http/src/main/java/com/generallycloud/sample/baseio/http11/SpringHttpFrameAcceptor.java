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
package com.generallycloud.sample.baseio.http11;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.FrameAcceptor;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.http11.HttpFrameAcceptor;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.NamedFrame;

public class SpringHttpFrameAcceptor extends HttpFrameAcceptor {

    private ClassPathXmlApplicationContext applicationContext;
    private boolean                        checkFilter = true;
    private FrameAcceptor                 filter;

    @Override
    public void accept(NioSocketChannel ch, Frame frame) throws Exception {
        NamedFrame f = (NamedFrame) frame;
        if (checkFilter) {
            checkFilter = false;
            filter = getFrameAcceptor("http-filter");
        }
        if (filter != null) {
            filter.accept(ch, frame);
            if (frame.flushed()) {
                return;
            }
        }
        FrameAcceptor acceptor = getFrameAcceptor(f.getFrameName());
        if (acceptor == null) {
            acceptHtml(ch, f);
            return;
        }
        acceptor.accept(ch, frame);
    }

    private FrameAcceptor getFrameAcceptor(String name) {
        return (FrameAcceptor) ContextUtil.getBean(name);
    }

    @Override
    protected void destroy(ChannelContext context, boolean redeploy) {
        applicationContext.destroy();
        super.destroy(context, redeploy);
    }

    @Override
    protected void initialize(ChannelContext context, boolean redeploy) throws Exception {
        super.initialize(context, redeploy);
        System.setProperty("org.apache.commons.logging.log", Sl4jLogger.class.getName());
        Thread.currentThread().setContextClassLoader(null); //for spring
        applicationContext = new ClassPathXmlApplicationContext("classpath:spring-core.xml");
        applicationContext.start();
    }

}
