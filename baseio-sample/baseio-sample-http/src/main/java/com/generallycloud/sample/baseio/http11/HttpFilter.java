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
package com.generallycloud.sample.baseio.http11;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.Parameters;
import com.generallycloud.baseio.container.http11.HttpFutureAcceptorService;
import com.generallycloud.baseio.container.http11.HttpSession;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.NamedFuture;
import com.generallycloud.baseio.protocol.ParametersFuture;

/**
 * @author wangkai
 *
 */
@Service("http-filter")
public class HttpFilter extends HttpFutureAcceptorService {

    private Logger      logger              = LoggerFactory.getLogger(getClass());
    private Set<String> noneLoggerSuffixSet = new HashSet<>();
    private Set<String> noneLoggerUrlSet    = new HashSet<>();

    @Override
    protected void doAccept(HttpSession session, HttpFuture future) throws Exception {
        log(session, future);
    }

    private boolean endContains(String futureName) {
        int idx = StringUtil.lastIndexOf(futureName, '.', 5);
        if (idx == -1) {
            return false;
        }
        String suffix = futureName.substring(idx);
        return noneLoggerSuffixSet.contains(suffix);
    }

    @PostConstruct
    protected void initialize() throws Exception {
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

    private void log(HttpSession session, NamedFuture future) throws Exception {
        NamedFuture nf = (NamedFuture) future;
        String futureName = nf.getFutureName();
        if (noneLoggerUrlSet.contains(futureName) || endContains(futureName)) {
            return;
        }
        String remoteAddr = session.getIoSession().getRemoteAddr();
        String readText = nf.getReadText();
        if (!StringUtil.isNullOrBlank(readText)) {
            logger.info("request ip:{}, service name:{}, content: {}",
                    new String[] { remoteAddr, futureName, readText });
            return;
        }
        if (nf instanceof ParametersFuture) {
            Parameters parameters = ((ParametersFuture) nf).getParameters();
            if (parameters.size() > 0) {
                logger.info("request ip:{}, service name:{}, content: {}",
                        new String[] { remoteAddr, futureName, parameters.toString() });
                return;
            }
        }
        logger.info("request ip:{}, service name:{}", remoteAddr, futureName);
    }

}
