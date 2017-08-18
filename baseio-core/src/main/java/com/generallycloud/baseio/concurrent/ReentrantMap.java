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
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 仅适用于：</BR>
 * MULTIPLE => PUT </BR>
 * MULTIPLE => REMOVE </BR>
 * SINGLE => FOREACH </BR>
 * SINGLE => SIZE
 * @param <K>
 * @param <V>
 */
public class ReentrantMap<K, V> {

    private Map<K, V>     snapshot;
    private List<Event>   modifList = new ArrayList<>();
    private ReentrantLock loack     = new ReentrantLock();
    private boolean       modifid   = false;

    public ReentrantMap(Map<K, V> snapshot) {
        this.snapshot = snapshot;
    }

    public Map<K, V> takeSnapshot() {
        if (modifid) {
            ReentrantLock lock = this.loack;

            lock.lock();

            Map<K, V> snapshot = this.snapshot;

            List<Event> modifList = this.modifList;

            for (Event e : modifList) {

                if (e.isAdd) {
                    snapshot.put(e.key, e.value);
                } else {
                    snapshot.remove(e.key);
                }
            }

            modifList.clear();

            this.modifid = false;

            lock.unlock();
        }

        return snapshot;
    }

    private boolean modif(K key, V value, boolean isAdd) {
        ReentrantLock lock = this.loack;
        lock.lock();
        Event event = new Event();
        event.key = key;
        event.value = value;
        event.isAdd = isAdd;
        this.modifList.add(event);
        this.modifid = true;
        lock.unlock();
        return true;
    }

    public boolean put(K key, V value) {
        return modif(key, value, true);
    }

    public void remove(K key) {
        modif(key, null, false);
    }

    public ReentrantLock getReentrantLock() {
        return loack;
    }

    public int size() {
        return takeSnapshot().size();
    }

    public void clear() {
        ReentrantLock lock = this.loack;
        lock.lock();
        this.modifList.clear();
        this.modifid = false;
        this.snapshot.clear();
        lock.unlock();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    class Event {
        K       key;
        V       value;
        boolean isAdd;
    }

}
