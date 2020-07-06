/*
 * Copyright 2015 The FireNio Project
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
package sample.http11;

import com.firenio.codec.http11.HttpFrame;
import com.firenio.component.Channel;
import com.firenio.component.ChannelContext;
import com.firenio.component.Frame;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringHttpFrameHandle extends HttpFrameHandle {

    private ClassPathXmlApplicationContext applicationContext;
    private HttpFrameFilter                filter;

    @Override
    public void accept(Channel ch, Frame f) throws Exception {
        if (filter.accept(ch, f)) {
            return;
        }
        String            frameName = HttpUtil.getFrameName(ch, f);
        HttpFrameAcceptor acceptor  = getFrameAcceptor(frameName);
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
        try {
            return (HttpFrameAcceptor) applicationContext.getBean(name);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public void initialize(ChannelContext context, String rootPath, boolean prodMode) throws Exception {
        super.initialize(context, rootPath, prodMode);
        System.setProperty("org.apache.commons.logging.log", Sl4jLogger.class.getName());
        Thread.currentThread().setContextClassLoader(null); //for spring
        applicationContext = new ClassPathXmlApplicationContext("classpath:spring-core.xml");
        applicationContext.start();
        filter = (HttpFrameFilter) applicationContext.getBean("http-filter");
    }

}
