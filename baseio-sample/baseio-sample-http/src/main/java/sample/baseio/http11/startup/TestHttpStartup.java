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
package sample.baseio.http11.startup;

import java.io.IOException;

import com.firenio.baseio.Options;
import com.firenio.baseio.container.ApplicationBootstrap;
import com.firenio.baseio.container.URLDynamicClassLoader;
import com.firenio.baseio.container.ApplicationBootstrap.ClassPathScaner;

/**
 * @author wangkai
 *
 */
public class TestHttpStartup {

    public static void main(String[] args) throws Exception {

        Options.setByteBufDebug(true);
        Options.setDebugErrorLevel(9);
        ApplicationBootstrap.startup("sample.baseio.http11.startup.TestHttpBootstrapEngine",
                ApplicationBootstrap.withDefault(new ClassPathScaner() {

                    @Override
                    public void scanClassPaths(URLDynamicClassLoader classLoader, String mode,
                            String rootLocalAddress) throws IOException {
                        if (!ApplicationBootstrap.isRuntimeDevMode(mode)) {
                            classLoader.scan(rootLocalAddress + "/app");
                        }
                    }
                }));

    }

}
