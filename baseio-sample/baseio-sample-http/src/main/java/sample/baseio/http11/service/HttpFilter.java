/*
 * Copyright 2015 The Baseio Project
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
package sample.baseio.http11.service;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.NioSocketChannel;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;
import com.firenio.baseio.protocol.Frame;
import com.firenio.baseio.protocol.NamedFrame;
import com.firenio.baseio.protocol.TextFrame;

import sample.baseio.http11.HttpFrameFilter;

/**
 * @author wangkai
 *
 */
@Service("http-filter")
public class HttpFilter implements HttpFrameFilter {

    private Logger      logger              = LoggerFactory.getLogger(getClass());
    private Set<String> noneLoggerSuffixSet = new HashSet<>();
    private Set<String> noneLoggerUrlSet    = new HashSet<>();

    @Override
    public boolean accept(NioSocketChannel ch, NamedFrame frame) throws Exception {
        return log(ch, frame);
    }

    private boolean endContains(String frameName) {
        int idx = Util.lastIndexOf(frameName, '.', 5);
        if (idx == -1) {
            return false;
        }
        String suffix = frameName.substring(idx);
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

    private boolean log(NioSocketChannel ch, Frame frame) throws Exception {
        NamedFrame m = (NamedFrame) frame;
        String frameName = m.getFrameName();
        if (!noneLoggerUrlSet.contains(frameName) && !endContains(frameName)) {
            String remoteAddr = ch.getRemoteAddr();
            if (frame instanceof TextFrame) {
                String readText = ((TextFrame) frame).getReadText();
                if (!Util.isNullOrBlank(readText)) {
                    logger.info("request ip:{}, service name:{}, content: {}", remoteAddr,
                            frameName, readText);
                    return false;
                }
            } else {
                logger.info("request ip:{}, service name:{}", remoteAddr, frameName);
            }
        }
        return false;
    }

}
