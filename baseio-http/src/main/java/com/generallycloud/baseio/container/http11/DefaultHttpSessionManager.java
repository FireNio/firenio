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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.generallycloud.baseio.codec.http11.Cookie;
import com.generallycloud.baseio.codec.http11.HttpFrame;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.concurrent.AbstractEventLoop;

//FIXME 限制最大session数量
//FIXME 根据当前是否正在redeploy来保存和恢复session
public class DefaultHttpSessionManager extends AbstractEventLoop implements HttpSessionManager {

    private String                   COOKIE_NAME_SESSIONID = "BSESSIONID";
    private Object                   sleepLock             = new Object();
    private Map<String, HttpSession> sessions              = new ConcurrentHashMap<>();
    private Map<String, HttpSession> readOnlySessions      = Collections.unmodifiableMap(sessions);

    @Override
    public void putSession(String sessionId, HttpSession ch) {
        sessions.put(sessionId, ch);
    }

    @Override
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public HttpSession getHttpSession(HttpFrameAcceptor context, NioSocketChannel ioSession,
            HttpFrame frame) {
        String sessionId = frame.getCookie(COOKIE_NAME_SESSIONID);
        if (StringUtil.isNullOrBlank(sessionId)) {
            DefaultHttpSession ch = new DefaultHttpSession(context, ioSession);
            sessionId = ch.getSessionId();
            Cookie cookie = new Cookie(COOKIE_NAME_SESSIONID, sessionId);
            frame.addCookie(cookie);
            this.sessions.put(sessionId, ch);
            return ch;
        }
        HttpSession session = sessions.get(sessionId);
        if (session == null) {
            session = new DefaultHttpSession(context, ioSession, sessionId);
            this.sessions.put(sessionId, session);
            return session;
        }
        if (!session.isValidate()) {
            sessions.remove(sessionId);
            CloseUtil.close(session.getChannel());
            return getHttpSession(context, ioSession, frame);
        }
        session.active(ioSession);
        return session;
    }

    @Override
    public void doLoop() {
        Collection<HttpSession> es = sessions.values();
        for (HttpSession session : es) {
            if (!session.isValidate()) {
                sessions.remove(session.getSessionId());
                CloseUtil.close(session.getChannel());
            }
        }
        sleep(30 * 60 * 1000);
    }

    private void sleep(long time) {
        ThreadUtil.wait(sleepLock, time);
    }

    @Override
    protected void doStop() {
        for (HttpSession session : sessions.values()) {
            CloseUtil.close(session.getChannel());
        }
        super.doStop();
    }

    @Override
    public Map<String, HttpSession> getManagedSessions() {
        return readOnlySessions;
    }

    @Override
    public void wakeup() {
        synchronized (sleepLock) {
            sleepLock.notify();
        }
        super.wakeup();
    }

    @Override
    public int getManagedSessionSize() {
        return sessions.size();
    }

}
