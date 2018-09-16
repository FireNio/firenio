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
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.NamedFrame;
import com.generallycloud.sample.baseio.http11.service.ContextUtil;

public class SpringHttpFrameHandle extends HttpFrameHandle {

    private ClassPathXmlApplicationContext applicationContext;
    private boolean                        checkFilter = true;
    private HttpFrameFilter                filter;

    @Override
    public void accept(NioSocketChannel ch, Frame frame) throws Exception {
        NamedFrame f = (NamedFrame) frame;
        if (checkFilter) {
            checkFilter = false;
            filter = (HttpFrameFilter) ContextUtil.getBean("http-filter");
        }
        if (filter != null && filter.accept(ch, f)) {
            return;
        }
        HttpFrameAcceptor acceptor = getFrameAcceptor(f.getFrameName());
        if (acceptor == null) {
            acceptHtml(ch, f);
            return;
        }
        acceptor.accept(ch, f);
    }

    private HttpFrameAcceptor getFrameAcceptor(String name) {
        return (HttpFrameAcceptor) ContextUtil.getBean(name);
    }

    public void destroy(ChannelContext context) {
        applicationContext.destroy();
    }

    @Override
    public void initialize(String rootPath, String mode) throws Exception {
        super.initialize(rootPath, mode);
        System.setProperty("org.apache.commons.logging.log", Sl4jLogger.class.getName());
        Thread.currentThread().setContextClassLoader(null); //for spring
        applicationContext = new ClassPathXmlApplicationContext("classpath:spring-core.xml");
        applicationContext.start();
    }

}
