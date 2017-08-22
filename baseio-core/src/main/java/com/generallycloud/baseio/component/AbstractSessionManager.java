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
package com.generallycloud.baseio.component;

public abstract class AbstractSessionManager implements SessionManager {

    //FIXME chuan can
    protected int sessionSizeLimit  = 100 * 10000;

    private long  current_idle_time = 0;
    private long  last_idle_time    = 0;
    private long  session_idle_time = 0;
    private long  next_idle_time    = System.currentTimeMillis();

    public AbstractSessionManager(long session_idle_time) {
        this.session_idle_time = session_idle_time;
    }

    @Override
    public void loop() {

        long current_time = System.currentTimeMillis();

        if (next_idle_time > current_time) {
            return;
        }

        this.last_idle_time = this.current_idle_time;

        this.current_idle_time = current_time;

        this.next_idle_time = current_idle_time + session_idle_time;

        sessionIdle(last_idle_time, current_time);
    }

    protected abstract void sessionIdle(long lastIdleTime, long currentTime);

    public int getSessionSizeLimit() {
        return sessionSizeLimit;
    }

}
