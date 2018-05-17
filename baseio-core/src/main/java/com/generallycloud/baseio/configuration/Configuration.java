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
import com.generallycloud.baseio.component.SocketChannelContext;

//FIXME 校验参数
public class Configuration {

    private int     port;
    private String  host               = "127.0.0.1";
    private int     coreSize           = Runtime.getRuntime().availableProcessors();
    private Charset charset            = Encoding.UTF8;
    private long    sessionIdleTime    = 30 * 1000;
    private int     workEventQueueSize = 1024 * 256;
    //内存池单元大小
    private int     memoryPoolUnit     = 512;
    //内存池是否使用启用堆外内存
    private boolean enableMemoryPoolDirect;
    private boolean enableHeartbeatLog = true;
    private boolean enableSsl;
    //是否启用work event loop，如果启用，则future在work event loop中处理
    private boolean enableWorkEventLoop;
    private boolean enableMemoryPool   = true;
    //内存池内存单元数量（单核）
    private int     memoryPoolCapacity;
    private int     channelReadBuffer  = 1024 * 512;
    private String  certCrt;
    private String  certKey;
    private String  sslKeystore;
    private int     memoryPoolRate     = 32;
    //单条连接write(srcs)的数量
    private int     writeBuffers       = 8;

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

    public int getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(int coreSize) {
        this.coreSize = coreSize;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public long getSessionIdleTime() {
        return sessionIdleTime;
    }

    public void setSessionIdleTime(long sessionIdleTime) {
        this.sessionIdleTime = sessionIdleTime;
    }

    public int getWorkEventQueueSize() {
        return workEventQueueSize;
    }

    public void setWorkEventQueueSize(int workEventQueueSize) {
        this.workEventQueueSize = workEventQueueSize;
    }

    public int getMemoryPoolUnit() {
        return memoryPoolUnit;
    }

    public void setMemoryPoolUnit(int memoryPoolUnit) {
        this.memoryPoolUnit = memoryPoolUnit;
    }

    public boolean isEnableMemoryPoolDirect() {
        return enableMemoryPoolDirect;
    }

    public void setEnableMemoryPoolDirect(boolean enableMemoryPoolDirect) {
        this.enableMemoryPoolDirect = enableMemoryPoolDirect;
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

    public boolean isEnableMemoryPool() {
        return enableMemoryPool;
    }

    public void setEnableMemoryPool(boolean enableMemoryPool) {
        this.enableMemoryPool = enableMemoryPool;
    }

    public int getMemoryPoolCapacity() {
        return memoryPoolCapacity;
    }

    public void setMemoryPoolCapacity(int memoryPoolCapacity) {
        this.memoryPoolCapacity = memoryPoolCapacity;
    }

    public int getChannelReadBuffer() {
        return channelReadBuffer;
    }

    public void setChannelReadBuffer(int channelReadBuffer) {
        this.channelReadBuffer = channelReadBuffer;
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

    public int getMemoryPoolRate() {
        return memoryPoolRate;
    }

    public void setMemoryPoolRate(int memoryPoolRate) {
        this.memoryPoolRate = memoryPoolRate;
    }
    
    public int getWriteBuffers() {
        return writeBuffers;
    }

    public void setWriteBuffers(int writeBuffers) {
        this.writeBuffers = writeBuffers;
    }

    public void initializeDefault(SocketChannelContext context) {
        if (memoryPoolCapacity == 0) {
            long total = Runtime.getRuntime().maxMemory();
            memoryPoolCapacity = (int) (total / (memoryPoolUnit * coreSize * memoryPoolRate));
        }
    }

}
