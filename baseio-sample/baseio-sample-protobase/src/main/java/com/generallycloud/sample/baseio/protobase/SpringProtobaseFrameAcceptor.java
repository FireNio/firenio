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
package com.generallycloud.sample.baseio.protobase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.generallycloud.baseio.codec.protobase.ParamedProtobaseFrame;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.FrameAcceptor;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.protobase.ProtobaseFrameAcceptor;
import com.generallycloud.baseio.protocol.Frame;

/**
 * @author wangkai
 *
 */
public class SpringProtobaseFrameAcceptor extends ProtobaseFrameAcceptor {

    private ClassPathXmlApplicationContext applicationContext;

    @Override
    public void accept(NioSocketChannel ch, Frame frame) throws Exception {
        ParamedProtobaseFrame f = (ParamedProtobaseFrame) frame;
        FrameAcceptor acceptor = (FrameAcceptor) ContextUtil.getBean(f.getFrameName());
        if (acceptor == null) {
            f.put("code", 404);
            ch.flush(frame);
            return;
        }
        acceptor.accept(ch, frame);
    }

    @Override
    protected void initialize(ChannelContext context, boolean redeploy) throws Exception {
        super.initialize(context, redeploy);
        Thread.currentThread().setContextClassLoader(null); // for spring
        System.setProperty("org.apache.commons.logging.log", Sl4jLogger.class.getName());
        applicationContext = new ClassPathXmlApplicationContext("classpath:spring-core.xml");
        applicationContext.start();
    }

    @Override
    protected void destroy(ChannelContext context, boolean redeploy) {
        applicationContext.destroy();
        super.destroy(context, redeploy);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
