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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 仅适用于：</BR>
 * MULTIPLE => PUT </BR>
 * MULTIPLE => REMOVE </BR>
 * SINGLE => FOREACH </BR>
 * SINGLE => SIZE
 * @param <T>
 */
public class ReentrantList<T> {

    private List<T>       snapshot;
    private List<Event>   modifList = new ArrayList<>();
    private ReentrantLock loack     = new ReentrantLock();
    private boolean       modifid   = false;

    public ReentrantList(List<T> snapshot) {
        this.snapshot = snapshot;
    }

    public List<T> takeSnapshot() {
        if (modifid) {
            ReentrantLock lock = this.loack;

            lock.lock();

            List<T> snapshot = this.snapshot;

            List<Event> modifList = this.modifList;

            for (Event e : modifList) {

                if (e.isAdd) {
                    snapshot.add(e.value);
                } else {
                    snapshot.remove(e.value);
                }
            }

            modifList.clear();

            this.modifid = false;

            lock.unlock();
        }

        return snapshot;
    }

    public boolean add(T t) {
        return modif(t, true);
    }

    private boolean modif(T t, boolean isAdd) {
        ReentrantLock lock = this.loack;
        lock.lock();
        Event e = new Event();
        e.isAdd = isAdd;
        e.value = t;
        this.modifList.add(e);
        this.modifid = true;
        lock.unlock();
        return true;
    }

    public void remove(T t) {
        modif(t, false);
    }

    public void clear() {
        ReentrantLock lock = this.loack;
        lock.lock();
        this.modifList.clear();
        this.modifid = false;
        this.snapshot.clear();
        lock.unlock();
    }

    public ReentrantLock getReentrantLock() {
        return loack;
    }

    public int size() {
        return takeSnapshot().size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    class Event {
        T       value;
        boolean isAdd;
    }

}
