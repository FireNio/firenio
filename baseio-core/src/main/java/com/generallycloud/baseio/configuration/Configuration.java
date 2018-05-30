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
package com.generallycloud.baseio.configuration;

import java.nio.charset.Charset;

import com.generallycloud.baseio.common.Encoding;

//FIXME 校验参数
public class Configuration {

    private int     port;
    private String  host               = "127.0.0.1";
    private Charset charset            = Encoding.UTF8;
    private int     workEventQueueSize = 1024 * 256;
    private boolean enableHeartbeatLog = true;
    private boolean enableSsl;
    //是否启用work event loop，如果启用，则future在work event loop中处理
    private boolean enableWorkEventLoop;
    private String  certCrt;
    private String  certKey;
    private String  sslKeystore;
    //单条连接write(srcs)的数量
    private int     writeBuffers       = 8;
    private int     bufRecycleSize     = 1024 * 4;

    public Configuration() {}

    public Configuration(int port) {
        this.port = port;
    }

    public Configuration(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public int getWorkEventQueueSize() {
        return workEventQueueSize;
    }

    public void setWorkEventQueueSize(int workEventQueueSize) {
        this.workEventQueueSize = workEventQueueSize;
    }

    public boolean isEnableHeartbeatLog() {
        return enableHeartbeatLog;
    }

    public void setEnableHeartbeatLog(boolean enableHeartbeatLog) {
        this.enableHeartbeatLog = enableHeartbeatLog;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    public boolean isEnableWorkEventLoop() {
        return enableWorkEventLoop;
    }

    public void setEnableWorkEventLoop(boolean enableWorkEventLoop) {
        this.enableWorkEventLoop = enableWorkEventLoop;
    }

    public String getCertCrt() {
        return certCrt;
    }

    public void setCertCrt(String certCrt) {
        this.certCrt = certCrt;
    }

    public String getCertKey() {
        return certKey;
    }

    public void setCertKey(String certKey) {
        this.certKey = certKey;
    }

    public String getSslKeystore() {
        return sslKeystore;
    }

    public void setSslKeystore(String sslKeystore) {
        this.sslKeystore = sslKeystore;
    }

    public int getWriteBuffers() {
        return writeBuffers;
    }

    public int getBufRecycleSize() {
        return bufRecycleSize;
    }

    public void setBufRecycleSize(int bufRecycleSize) {
        this.bufRecycleSize = bufRecycleSize;
    }

    public void setWriteBuffers(int writeBuffers) {
        this.writeBuffers = writeBuffers;
    }

}
