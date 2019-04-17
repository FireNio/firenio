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
package sample.http11.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.firenio.codec.http11.Cookie;
import com.firenio.codec.http11.CookieUtil;
import com.firenio.codec.http11.HttpContentType;
import com.firenio.codec.http11.HttpFrame;
import com.firenio.codec.http11.HttpHeader;
import com.firenio.common.Util;
import com.firenio.component.Channel;

import sample.http11.HttpFrameAcceptor;

@Service("/test-cookie")
public class TestCookieHeaderServlet extends HttpFrameAcceptor {

    @Override
    protected void doAccept(Channel ch, HttpFrame frame) throws Exception {
        String name  = frame.getRequestParam("name");
        String value = frame.getRequestParam("value");
        if (Util.isNullOrBlank(name)) {
            name = "test8";
        }
        if (Util.isNullOrBlank(value)) {
            value = Util.randomUUID();
        }
        String              cookieLine = frame.getRequestHeader(HttpHeader.Cookie);
        Map<String, String> cookieMap  = new HashMap<>();
        if (cookieLine != null) {
            CookieUtil.parseCookies(cookieMap, cookieLine);
        }
        String res = "yes server already accept your message :) " + frame.getRequestParams();
        res += "<BR/>";
        res += cookieMap.toString();
        Cookie c = new Cookie(name, value);
        c.setComment("comment");
        c.setMaxAge(999999);
        frame.setContentType(HttpContentType.text_html_utf8);
        frame.setResponseHeader(HttpHeader.Set_Cookie, c.toString().getBytes());
        frame.setContent(res.getBytes(ch.getCharset()));
        ch.writeAndFlush(frame);
    }

}
