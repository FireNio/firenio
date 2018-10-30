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
package com.generallycloud.baseio.codec.protobase;

import java.util.Map;

import com.generallycloud.baseio.collection.Parameters;
import com.generallycloud.baseio.protocol.NamedFrame;
import com.generallycloud.baseio.protocol.ParametersFrame;

/**
 * @author wangkai
 *
 */
public class ParamedProtobaseFrame extends ProtobaseFrame implements NamedFrame, ParametersFrame {

    private static final String FRAME_NAME_KEY = "FRAME_NAME_KEY";

    private Parameters          parameters;

    public ParamedProtobaseFrame() {}

    public ParamedProtobaseFrame(int frameId, String frameName) {
        super(frameId);
        this.setFrameName(frameName);
    }

    public ParamedProtobaseFrame(String frameName) {
        this.setFrameName(frameName);
    }

    ParamedProtobaseFrame complete() {
        parameters = new JsonParameters(getReadText());
        return this;
    }

    @Override
    public String getFrameName() {
        return getParameters().getParameter(FRAME_NAME_KEY);
    }

    @Override
    public Parameters getParameters() {
        return parameters;
    }

    public void put(String key, Object value) {
        if (parameters == null) {
            parameters = new JsonParameters();
        }
        parameters.put(key, value);
    }

    public void putAll(Map<String, Object> params) {
        if (parameters == null) {
            parameters = new JsonParameters();
        }
        parameters.putAll(params);
    }
    
    public void setFrameName(String frameName) {
        if (parameters == null) {
            parameters = new JsonParameters();
        }
        parameters.put(FRAME_NAME_KEY, frameName);
    }

}
