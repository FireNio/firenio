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
package com.generallycloud.baseio.container.implementation;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.Parameters;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.service.FutureAcceptorFilter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.NamedFuture;
import com.generallycloud.baseio.protocol.ParametersFuture;

public class LoggerFilter extends FutureAcceptorFilter {

    private Logger      logger     = LoggerFactory.getLogger(getClass());

    private Set<String> noneLoggerUrlSet = new HashSet<>();
    
    private Set<String> noneLoggerSuffixSet = new HashSet<>();

    @Override
    protected void accept(SocketSession session, NamedFuture future) throws Exception {

        String futureName = future.getFutureName();

        if (noneLoggerUrlSet.contains(futureName) || endContains(futureName)) {
            return;
        }

        String remoteAddr = session.getRemoteAddr();

        String readText = future.getReadText();

        if (!StringUtil.isNullOrBlank(readText)) {
            logger.info("request ip:{}, service name:{}, content: {}",
                    new String[] { remoteAddr, futureName, readText });
            return;
        }

        if (future instanceof ParametersFuture) {

            Parameters parameters = ((ParametersFuture) future).getParameters();

            if (parameters.size() > 0) {
                logger.info("request ip:{}, service name:{}, content: {}",
                        new String[] { remoteAddr, futureName, parameters.toString() });
                return;
            }
        }
        logger.info("request ip:{}, service name:{}", remoteAddr, futureName);
    }
    
    private boolean endContains(String futureName){
        int idx = StringUtil.lastIndexOf(futureName, '.', 5);
        if (idx == -1) {
            return false;
        }
        String suffix = futureName.substring(idx);
        return noneLoggerSuffixSet.contains(suffix);
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {

        super.initialize(context, config);

        JSONArray array = config.getJSONArray("none-logger");

        if (array == null) {
            return;
        }

        for (int i = 0; i < array.size(); i++) {
            noneLoggerUrlSet.add(array.getString(i));
        }
        
        noneLoggerSuffixSet.add(".html");
        noneLoggerSuffixSet.add(".css");
        noneLoggerSuffixSet.add(".js");
        noneLoggerSuffixSet.add(".jpg");
        noneLoggerSuffixSet.add(".png");
        noneLoggerSuffixSet.add(".ico");
        noneLoggerSuffixSet.add(".jpeg");
        noneLoggerSuffixSet.add(".gif");
        noneLoggerSuffixSet.add(".scss");
    }

}
