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

import com.generallycloud.baseio.collection.JsonParameters;
import com.generallycloud.baseio.collection.Parameters;
import com.generallycloud.baseio.protocol.NamedFuture;
import com.generallycloud.baseio.protocol.ParametersFuture;

/**
 * @author wangkai
 *
 */
public class ParamedProtobaseFuture extends ProtobaseFuture
        implements NamedFuture, ParametersFuture {

    private static final String FUTURE_NAME_KEY = "FUTURE_NAME_KEY";

    public ParamedProtobaseFuture() {
        this.parameters = new JsonParameters();
    }

    ParamedProtobaseFuture(int textLengthLimit, int binaryLengthLimit) {
        super(textLengthLimit, binaryLengthLimit);
    }

    public ParamedProtobaseFuture(String futureName) {
        this.parameters = new JsonParameters();
        this.setFutureName(futureName);
    }

    public ParamedProtobaseFuture(int futureId, String futureName) {
        super(futureId);
        this.parameters = new JsonParameters();
        this.setFutureName(futureName);
    }

    @Override
    public String getFutureName() {
        return getParameters().getParameter(FUTURE_NAME_KEY);
    }

    public void setFutureName(String futureName) {
        this.parameters.put(FUTURE_NAME_KEY, futureName);
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
