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
public class ParamedProtobaseFrame extends ProtobaseFrame
        implements NamedFrame, ParametersFrame {

    private static final String FRAME_NAME_KEY = "FRAME_NAME_KEY";

    public ParamedProtobaseFrame() {
        this.parameters = new JsonParameters();
    }

    public ParamedProtobaseFrame(String frameName) {
        this.parameters = new JsonParameters();
        this.setFrameName(frameName);
    }

    public ParamedProtobaseFrame(int frameId, String frameName) {
        super(frameId);
        this.parameters = new JsonParameters();
        this.setFrameName(frameName);
    }

    @Override
    public String getFrameName() {
        return getParameters().getParameter(FRAME_NAME_KEY);
    }

    public void setFrameName(String frameName) {
        this.parameters.put(FRAME_NAME_KEY, frameName);
    }

    public void put(String key, Object value) {
        this.parameters.put(key, value);
    }

    public void putAll(Map<String, Object> params) {
        this.parameters.putAll(params);
    }

    private Parameters parameters;

    @Override
    public Parameters getParameters() {
        if (parameters == null) {
            parameters = new JsonParameters(getReadText());
        }
        return parameters;
    }

}
