/*
 * Copyright 2015 The FireNio Project
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
package test.others.jdkproxy;

import java.lang.reflect.Proxy;

import com.firenio.common.Util;

public class ProviderFactory {

    static class FontProviderFromDisk implements FontProvider {

        @Override
        public String getFont(String name) {
            return "DISK:" + name;
        }
    }

    public static FontProvider getFontProvider() {
        return (FontProvider) Proxy.newProxyInstance(FontProvider.class.getClassLoader(),
                Util.getInterfaces(FontProvider.class),
                new CachedProviderHandler(new FontProviderFromDisk()));
    }

}
