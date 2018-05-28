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

public abstract class AbstractSessionManager implements SocketSessionManager {

    //FIXME chuan can
    private int  sessionSizeLimit = 100 * 10000;
    private long last_idle_time   = 0;

    @Override
    public void sessionIdle(long currentTime) {
        sessionIdle0(last_idle_time, currentTime);
        last_idle_time = currentTime;
    }

    abstract void sessionIdle0(long lastIdleTime, long currentTime);

    public int getSessionSizeLimit() {
        return sessionSizeLimit;
    }

}
