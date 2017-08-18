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
import java.util.concurrent.atomic.AtomicBoolean;

import com.generallycloud.baseio.container.jms.Message;
import com.generallycloud.baseio.container.jms.Transaction;

public class TransactionSection implements Transaction {

    private List<Message> messages = new ArrayList<>();
    private MQContext     context;
    private AtomicBoolean finish   = new AtomicBoolean(false);

    public TransactionSection(MQContext context) {
        this.context = context;
    }

    @Override
    public boolean beginTransaction() {
        return false;
    }

    @Override
    public boolean commit() {
        if (finish.compareAndSet(false, true)) {
            this.messages.clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean rollback() {
        if (finish.compareAndSet(false, true)) {
            MQContext context = this.context;
            for (Message message : messages) {
                context.offerMessage(message);
            }
            return true;
        }
        return false;
    }

    public void offerMessage(Message message) {
        if (finish.get()) {
            return;
        }
        this.messages.add(message);
    }

}
