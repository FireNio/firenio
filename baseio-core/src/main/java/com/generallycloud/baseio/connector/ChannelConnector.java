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
package com.generallycloud.baseio.connector;

import java.io.Closeable;

import com.generallycloud.baseio.component.ChannelService;
import com.generallycloud.baseio.component.Connectable;
import com.generallycloud.baseio.component.Session;

public interface ChannelConnector extends ChannelService, Connectable, Closeable {

    public abstract Session getSession();

    public abstract boolean isConnected();

    public abstract long getTimeout();

    public abstract void setTimeout(long timeout);

}
