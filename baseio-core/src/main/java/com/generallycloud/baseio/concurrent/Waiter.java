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
package com.generallycloud.baseio.concurrent;

public class Waiter {

    private boolean isDnoe;
    private boolean timeouted;
    private Object  response;

    public boolean await() {
        return await(0);
    }

    /**
     * return true is timeout
     * 
     * @param timeout
     * @return timeouted 
     */
    public boolean await(long timeout) {
        synchronized (this) {
            if (isDnoe) {
                return false;
            }
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {}
            timeouted = !isDnoe;
        }
        return timeouted;
    }

    public void response(Object res) {
        synchronized (this) {
            if (isDnoe) {
                return;
            }
            this.isDnoe = true;
            this.response = res;
            this.notify();
        }
    }

    public boolean isDnoe() {
        return isDnoe;
    }

    public Object getResponse() {
        return response;
    }

    public boolean isTimeouted() {
        return timeouted;
    }

    //not include timeout
    public boolean isFailed() {
        return (response instanceof Throwable);
    }

}
