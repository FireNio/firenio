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
package com.generallycloud.baseio.container.jms;

public interface Transaction {

    /**
     * 连接启动事务后，需要进行显示的commit</BR>
     * 如果出现以下情况则服务器进行回滚操作</BR>
     * <ul>
     * <li>连接失败</li>
     * <li>连接被客户端主动关闭</li>
     * <li>连接调用rollback方法</li>
     * </ul>
     * @throws MQException
     */
    public abstract boolean beginTransaction() throws MQException;

    public abstract boolean commit() throws MQException;

    public abstract boolean rollback() throws MQException;
}
