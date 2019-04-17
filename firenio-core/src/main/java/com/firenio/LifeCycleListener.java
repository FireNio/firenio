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
package com.firenio;

public abstract class LifeCycleListener implements Comparable<LifeCycleListener> {

    @Override
    public int compareTo(LifeCycleListener o) {
        return sortIndex() - o.sortIndex();
    }

    public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {}

    public void lifeCycleStarted(LifeCycle lifeCycle) {}

    public void lifeCycleStarting(LifeCycle lifeCycle) {}

    public void lifeCycleStopped(LifeCycle lifeCycle) {}

    public void lifeCycleStopping(LifeCycle lifeCycle) {}

    public int sortIndex() {
        return 0;
    }

}
