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

public class MQSessionAttachment {

    private MQContext          context;
    private TransactionSection transactionSection;
    private Consumer           consumer;

    public MQSessionAttachment(MQContext context) {
        this.context = context;
    }

    public TransactionSection getTransactionSection() {
        return transactionSection;
    }

    public void setTransactionSection(TransactionSection transactionSection) {
        this.transactionSection = transactionSection;
    }

    public MQContext getContext() {
        return context;
    }

    protected Consumer getConsumer() {
        return consumer;
    }

    protected void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

}
