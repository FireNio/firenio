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
import com.generallycloud.baseio.component.ChannelContext;

//FIXME 校验参数
public class ServerConfiguration {

    private int     SERVER_PORT;
    private String  SERVER_HOST                      = "localhost";
    private int     SERVER_CORE_SIZE                 = Runtime.getRuntime().availableProcessors();
    private Charset SERVER_ENCODING                  = Encoding.UTF8;
    private long    SERVER_SESSION_IDLE_TIME         = 30 * 1000;
    private int     SERVER_WORK_EVENT_QUEUE_SIZE     = 1024 * 256;
    //内存池单元大小
    private int     SERVER_MEMORY_POOL_UNIT;
    //内存池是否使用启用堆外内存
    private boolean SERVER_ENABLE_MEMORY_POOL_DIRECT;
    private boolean SERVER_ENABLE_HEARTBEAT_LOG      = true;
    private boolean SERVER_ENABLE_SSL;
    //是否启用work event loop，如果启用，则future在work event loop中处理
    private boolean SERVER_ENABLE_WORK_EVENT_LOOP;
    private boolean SERVER_ENABLE_MEMORY_POOL        = true;
    //内存池内存单元数量（单核）
    private int     SERVER_MEMORY_POOL_CAPACITY;
    private int     SERVER_CHANNEL_READ_BUFFER       = 1024 * 512;
    //内存池内存单元数量百分比（单核），该配置影响"SERVER_MEMORY_POOL_CAPACITY"属性
    private double  SERVER_MEMORY_POOL_CAPACITY_RATE = 1d;

    private String  SERVER_CERT_CRT                  = "localhost";
    private String  SERVER_CERT_KEY                  = "localhost";

    public ServerConfiguration() {}

    public ServerConfiguration(int SERVER_PORT) {
        this.SERVER_PORT = SERVER_PORT;
    }

    public ServerConfiguration(String SERVER_HOST, int SERVER_PORT) {
        this.SERVER_PORT = SERVER_PORT;
        this.SERVER_HOST = SERVER_HOST;
    }

    public int getSERVER_PORT() {
        return SERVER_PORT;
    }

    public void setSERVER_PORT(int SERVER_PORT) {
        if (SERVER_PORT == 0) {
            return;
        }
        this.SERVER_PORT = SERVER_PORT;
    }

    public boolean isSERVER_ENABLE_SSL() {
        return SERVER_ENABLE_SSL;
    }

    public void setSERVER_ENABLE_SSL(boolean SERVER_ENABLE_SSL) {
        this.SERVER_ENABLE_SSL = SERVER_ENABLE_SSL;
    }

    public int getSERVER_CORE_SIZE() {
        return SERVER_CORE_SIZE;
    }

    public void setSERVER_CORE_SIZE(int SERVER_CORE_SIZE) {
        if (SERVER_CORE_SIZE == 0) {
            return;
        }
        this.SERVER_CORE_SIZE = SERVER_CORE_SIZE;
    }

    public Charset getSERVER_ENCODING() {
        return SERVER_ENCODING;
    }

    public void setSERVER_ENCODING(Charset SERVER_ENCODING) {
        this.SERVER_ENCODING = SERVER_ENCODING;
    }

    public String getSERVER_HOST() {
        return SERVER_HOST;
    }

    public void setSERVER_HOST(String SERVER_HOST) {
        this.SERVER_HOST = SERVER_HOST;
    }

    public long getSERVER_SESSION_IDLE_TIME() {
        return SERVER_SESSION_IDLE_TIME;
    }

    public void setSERVER_SESSION_IDLE_TIME(long SERVER_SESSION_IDLE_TIME) {

        if (SERVER_SESSION_IDLE_TIME == 0) {
            return;
        }

        this.SERVER_SESSION_IDLE_TIME = SERVER_SESSION_IDLE_TIME;
    }

    public int getSERVER_MEMORY_POOL_UNIT() {
        return SERVER_MEMORY_POOL_UNIT;
    }

    public void setSERVER_MEMORY_POOL_UNIT(int SERVER_MEMORY_POOL_UNIT) {
        if (SERVER_MEMORY_POOL_UNIT == 0) {
            return;
        }
        this.SERVER_MEMORY_POOL_UNIT = SERVER_MEMORY_POOL_UNIT;
    }

    public int getSERVER_MEMORY_POOL_CAPACITY() {
        return (int) (SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_CAPACITY_RATE);
    }

    public void setSERVER_MEMORY_POOL_CAPACITY(int SERVER_MEMORY_POOL_CAPACITY) {
        if (SERVER_MEMORY_POOL_CAPACITY == 0) {
            return;
        }
        this.SERVER_MEMORY_POOL_CAPACITY = SERVER_MEMORY_POOL_CAPACITY;
    }

    public int getSERVER_CHANNEL_READ_BUFFER() {
        return SERVER_CHANNEL_READ_BUFFER;
    }

    public void setSERVER_CHANNEL_READ_BUFFER(int SERVER_CHANNEL_READ_BUFFER) {
        if (SERVER_CHANNEL_READ_BUFFER == 0) {
            return;
        }
        this.SERVER_CHANNEL_READ_BUFFER = SERVER_CHANNEL_READ_BUFFER;
    }

    public double getSERVER_MEMORY_POOL_CAPACITY_RATE() {
        return SERVER_MEMORY_POOL_CAPACITY_RATE;
    }

    public void setSERVER_MEMORY_POOL_CAPACITY_RATE(double SERVER_MEMORY_POOL_CAPACITY_RATE) {
        if (SERVER_MEMORY_POOL_CAPACITY_RATE == 0) {
            return;
        }
        this.SERVER_MEMORY_POOL_CAPACITY_RATE = SERVER_MEMORY_POOL_CAPACITY_RATE;
    }

    public boolean isSERVER_ENABLE_MEMORY_POOL_DIRECT() {
        return SERVER_ENABLE_MEMORY_POOL_DIRECT;
    }

    public void setSERVER_ENABLE_MEMORY_POOL_DIRECT(boolean SERVER_ENABLE_MEMORY_POOL_DIRECT) {
        this.SERVER_ENABLE_MEMORY_POOL_DIRECT = SERVER_ENABLE_MEMORY_POOL_DIRECT;
    }

    public void initializeDefault(ChannelContext context) {

        if (SERVER_MEMORY_POOL_UNIT == 0) {
            SERVER_MEMORY_POOL_UNIT = 512;
        }

        if (SERVER_MEMORY_POOL_CAPACITY == 0) {

            long total = Runtime.getRuntime().maxMemory();

            SERVER_MEMORY_POOL_CAPACITY = (int) (total
                    / (SERVER_MEMORY_POOL_UNIT * SERVER_CORE_SIZE * 16));
        }

    }

    public boolean isSERVER_ENABLE_WORK_EVENT_LOOP() {
        return SERVER_ENABLE_WORK_EVENT_LOOP;
    }

    public void setSERVER_ENABLE_WORK_EVENT_LOOP(boolean SERVER_ENABLE_WORK_EVENT_LOOP) {
        this.SERVER_ENABLE_WORK_EVENT_LOOP = SERVER_ENABLE_WORK_EVENT_LOOP;
    }

    public boolean isSERVER_ENABLE_MEMORY_POOL() {
        return SERVER_ENABLE_MEMORY_POOL;
    }

    public void setSERVER_ENABLE_MEMORY_POOL(boolean SERVER_ENABLE_MEMORY_POOL) {
        this.SERVER_ENABLE_MEMORY_POOL = SERVER_ENABLE_MEMORY_POOL;
    }

    public boolean isSERVER_ENABLE_HEARTBEAT_LOG() {
        return SERVER_ENABLE_HEARTBEAT_LOG;
    }

    public void setSERVER_ENABLE_HEARTBEAT_LOG(boolean SERVER_ENABLE_HEARTBEAT_LOG) {
        this.SERVER_ENABLE_HEARTBEAT_LOG = SERVER_ENABLE_HEARTBEAT_LOG;
    }

    public int getSERVER_WORK_EVENT_QUEUE_SIZE() {
        return SERVER_WORK_EVENT_QUEUE_SIZE;
    }

    public void setSERVER_WORK_EVENT_QUEUE_SIZE(int SERVER_WORK_EVENT_QUEUE_SIZE) {
        if (SERVER_WORK_EVENT_QUEUE_SIZE == 0) {
            return;
        }
        this.SERVER_WORK_EVENT_QUEUE_SIZE = SERVER_WORK_EVENT_QUEUE_SIZE;
    }

    public String getSERVER_CERT_CRT() {
        return SERVER_CERT_CRT;
    }

    public void setSERVER_CERT_CRT(String SERVER_CERT_CRT) {
        this.SERVER_CERT_CRT = SERVER_CERT_CRT;
    }

    public String getSERVER_CERT_KEY() {
        return SERVER_CERT_KEY;
    }

    public void setSERVER_CERT_KEY(String SERVER_CERT_KEY) {
        this.SERVER_CERT_KEY = SERVER_CERT_KEY;
    }

    
    
}
