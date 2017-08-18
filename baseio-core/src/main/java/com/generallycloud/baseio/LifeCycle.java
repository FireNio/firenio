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

    public static int STARTING = 1;

    public static int RUNNING  = 2;

    public static int STOPPING = 3;

    public static int STOPPED  = 4;

    public abstract void start() throws Exception;

    public abstract void stop();

    public abstract boolean isFailed();

    public abstract boolean isRunning();

    public abstract boolean isStarted();

    public abstract boolean isStarting();

    public abstract boolean isStopped();

    public abstract boolean isStopping();

    public abstract void removeLifeCycleListener(LifeCycleListener listener);

    public abstract void addLifeCycleListener(LifeCycleListener listener);

}
