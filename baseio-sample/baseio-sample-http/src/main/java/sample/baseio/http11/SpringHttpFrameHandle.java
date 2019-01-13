/*
 * Copyright 2015 The Baseio Project
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
package sample.baseio.http11;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.firenio.baseio.codec.http11.HttpFrame;
import com.firenio.baseio.component.ChannelContext;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.Channel;

import sample.baseio.http11.service.ContextUtil;

public class SpringHttpFrameHandle extends HttpFrameHandle {

    private ClassPathXmlApplicationContext applicationContext;
    private boolean                        checkFilter = true;
    private HttpFrameFilter                filter;

    @Override
    public void accept(Channel ch, Frame f) throws Exception {
        if (checkFilter) {
            checkFilter = false;
            filter = (HttpFrameFilter) ContextUtil.getBean("http-filter");
        }
        if (filter != null && filter.accept(ch, f)) {
            return;
        }
        String frameName = HttpUtil.getFrameName(ch, f);
        HttpFrameAcceptor acceptor = getFrameAcceptor(frameName);
        if (f instanceof HttpFrame) {
            setDefaultResponseHeaders((HttpFrame) f);
        }
        if (acceptor == null) {
            acceptHtml(ch, f);
            return;
        }
        acceptor.accept(ch, f);
    }

    public void destroy(ChannelContext context) {
        applicationContext.destroy();
    }

    private HttpFrameAcceptor getFrameAcceptor(String name) {
        return (HttpFrameAcceptor) ContextUtil.getBean(name);
    }

    @Override
    public void initialize(ChannelContext context, String rootPath, String mode) throws Exception {
        super.initialize(context, rootPath, mode);
        System.setProperty("org.apache.commons.logging.log", Sl4jLogger.class.getName());
        Thread.currentThread().setContextClassLoader(null); //for spring
        applicationContext = new ClassPathXmlApplicationContext("classpath:spring-core.xml");
        applicationContext.start();
    }

}
