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
package com.generallycloud.baseio;

public interface LifeCycle {

    int RUNNING  = 2;

    int STARTING = 1;

    int STOPPED  = 4;

    int FAILED   = 5;

    int STOPPING = 3;

    void addLifeCycleListener(LifeCycleListener listener);

    boolean isFailed();

    boolean isRunning();

    boolean isStarting();

    boolean isStopped();

    boolean isStopping();

    void removeLifeCycleListener(LifeCycleListener listener);

    void start() throws Exception;

    void stop();

}
