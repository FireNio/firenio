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
package com.generallycloud.baseio.protocol;

import com.generallycloud.baseio.component.DatagramChannelContext;
import com.generallycloud.baseio.component.JsonParameters;
import com.generallycloud.baseio.component.Parameters;

public class DatagramRequest {

    private String     serviceName;

    private Parameters parameters;

    public DatagramRequest(String content) {
        this.parameters = new JsonParameters(content);
        this.serviceName = parameters.getParameter("serviceName");
    }

    public String getFutureName() {
        return serviceName;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public static DatagramRequest create(DatagramPacket packet, DatagramChannelContext context) {
        if (packet.getTimestamp() == 0) {
            String param = new String(packet.getData(), context.getEncoding());

            return new DatagramRequest(param);
        }
        return null;
    }

}
