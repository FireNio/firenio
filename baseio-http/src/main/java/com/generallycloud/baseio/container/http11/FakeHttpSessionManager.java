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
package com.generallycloud.baseio.container.http11;

import java.util.Map;

import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.concurrent.AbstractEventLoop;

/**
 * @author wangkai
 *
 */
public class FakeHttpSessionManager extends AbstractEventLoop implements HttpSessionManager {

    private final String HTTP_SESSION_KEY = "_HTTP_SESSION_KEY";

    @Override
    public void putSession(String sessionId, HttpSession session) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSession(String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getHttpSession(HttpContext context, SocketSession ioSession,
            HttpFuture future) {
        HttpSession httpSession = (HttpSession) ioSession.getAttribute(HTTP_SESSION_KEY);
        if (httpSession == null) {
            httpSession = new DefaultHttpSession(context, ioSession, null);
            ioSession.setAttribute(HTTP_SESSION_KEY, httpSession);
        }
        return httpSession;
    }

    @Override
    public Map<String, HttpSession> getManagedSessions() {
        return null;
    }

    @Override
    public int getManagedSessionSize() {
        return -1;
    }

    @Override
    protected void doLoop() {

    }

}
