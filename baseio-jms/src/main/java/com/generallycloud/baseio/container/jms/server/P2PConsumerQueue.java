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
package com.generallycloud.baseio.container.jms.server;

import java.util.ArrayList;
import java.util.List;

import com.generallycloud.baseio.concurrent.ReentrantList;

public class P2PConsumerQueue implements ConsumerQueue {

    private ReentrantList<Consumer> consumers = new ReentrantList<>(
            new ArrayList<Consumer>());

    @Override
    public int size() {
        return consumers.size();
    }

    @Override
    public void offer(Consumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public void remove(Consumer consumer) {
        consumers.remove(consumer);
    }

    @Override
    public List<Consumer> getSnapshot() {
        return consumers.takeSnapshot();
    }

}
