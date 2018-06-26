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
package com.generallycloud.baseio.container.configuration;

import com.generallycloud.baseio.common.Properties;

public class ApplicationConfiguration {

    private boolean    enableHttpSession;
    private boolean    enableRedeploy   = true;
    private boolean    enableStopServer = true;
    private String     futureAcceptor;
    private String     ioExceptionCaughtHandle;
    private String     onRedeployFutureAcceptor;
    private Properties properties;

    public String getFutureAcceptor() {
        return futureAcceptor;
    }

    public String getIoExceptionCaughtHandle() {
        return ioExceptionCaughtHandle;
    }

    public String getOnRedeployFutureAcceptor() {
        return onRedeployFutureAcceptor;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean isEnableHttpSession() {
        return enableHttpSession;
    }

    public boolean isEnableRedeploy() {
        return enableRedeploy;
    }

    public boolean isEnableStopServer() {
        return enableStopServer;
    }

    public void setEnableHttpSession(boolean enableHttpSession) {
        this.enableHttpSession = enableHttpSession;
    }

    public void setEnableRedeploy(boolean enableRedeploy) {
        this.enableRedeploy = enableRedeploy;
    }

    public void setEnableStopServer(boolean enableStopServer) {
        this.enableStopServer = enableStopServer;
    }

    public void setFutureAcceptor(String futureAcceptor) {
        this.futureAcceptor = futureAcceptor;
    }

    public void setIoExceptionCaughtHandle(String ioExceptionCaughtHandle) {
        this.ioExceptionCaughtHandle = ioExceptionCaughtHandle;
    }

    public void setOnRedeployFutureAcceptor(String onRedeployFutureAcceptor) {
        this.onRedeployFutureAcceptor = onRedeployFutureAcceptor;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
