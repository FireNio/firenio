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
package com.generallycloud.sample.baseio.http11.service;

import org.springframework.stereotype.Service;

import com.generallycloud.baseio.codec.http11.Cookie;
import com.generallycloud.baseio.codec.http11.HttpFrame;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.common.UUIDGenerator;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.sample.baseio.http11.HttpFrameAcceptor;

@Service("/test-cookie")
public class TestCookieHeaderServlet extends HttpFrameAcceptor {

    @Override
    protected void doAccept(NioSocketChannel ch, HttpFrame frame) throws Exception {
        String name = frame.getRequestParam("name");
        String value = frame.getRequestParam("value");
        if (StringUtil.isNullOrBlank(name)) {
            name = "test8";
        }
        if (StringUtil.isNullOrBlank(value)) {
            value = UUIDGenerator.random();
        }
        String res = "yes server already accept your message :) " + frame.getRequestParams();
        Cookie c = new Cookie(name, value);
        c.setComment("comment");
        c.setMaxAge(999999);
        frame.addCookie(c);
        frame.setResponseHeader(name.getBytes(), value.getBytes());
        frame.write(res, ch);
        ch.flush(frame);
    }

}
