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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.generallycloud.baseio.codec.http11.HttpFrame;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.common.UUIDGenerator;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.sample.baseio.http11.HttpFrameAcceptor;

@Service("/test-mm")
public class TestMmServlet extends HttpFrameAcceptor {

    private Map<String, String> mm         = new ConcurrentHashMap<>();
    private Map<String, String> mm_reverse = new ConcurrentHashMap<>();
    private String              url        = "https://www.generallycloud.com/test-mm";

    @Override
    protected void doAccept(NioSocketChannel ch, HttpFrame frame) throws Exception {
        if (mm.size() > 1024) {
            mm.clear();
        }
        String k = frame.getRequestParam("p");
        if (StringUtil.isNullOrBlank(k)) {
            frame.write("input your p :)", ch);
            ch.flush(frame);
            return;
        }
        String v = mm.remove(k);
        if (StringUtil.isNullOrBlank(v)) {
            if (k.length() == 32) {
                frame.write("input your p :)", ch);
                ch.flush(frame);
                return;
            }
            String rk = mm_reverse.get(k);
            if (StringUtil.isNullOrBlank(rk)) {
                rk = UUIDGenerator.random();
                mm.put(rk, k);
                mm_reverse.put(k, rk);
            }
            frame.write(url + "?p=" + rk, ch);
        } else {
            mm_reverse.remove(v);
            frame.write(v, ch);
        }
        ch.flush(frame);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
