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
package com.firenio.component;

import java.net.SocketOption;
import java.net.StandardSocketOptions;

/**
 * @author wangkai
 */
public final class SocketOptions {

    public static final  int      PARAM_BOOLEAN;
    public static final  int      IPPROTO_TCP;
    public static final  int      SOL_SOCKET;
    public static final  int      SO_BROADCAST;
    public static final  int      SO_ERROR;
    public static final  int      SO_KEEPALIVE;
    public static final  int      SO_SNDBUF;
    public static final  int      SO_RCVBUF;
    public static final  int      SO_REUSEADDR;
    public static final  int      SO_LINGER;
    public static final  int      TCP_NODELAY;
    public static final  int      TCP_QUICKACK;
    private static final Object[] TCP_SOCKET_OPTIONS = new Object[16];
    private static final Object[] SO_SOCKET_OPTIONS  = new Object[16];

    static {
        IPPROTO_TCP = 6 << 16;
        SOL_SOCKET = 1 << 16;
        PARAM_BOOLEAN = 1 << 8;
        SO_ERROR = SOL_SOCKET | 4;
        SO_BROADCAST = SOL_SOCKET | PARAM_BOOLEAN | 6;
        SO_KEEPALIVE = SOL_SOCKET | PARAM_BOOLEAN | 9;
        SO_SNDBUF = SOL_SOCKET | 7;
        SO_RCVBUF = SOL_SOCKET | 8;
        SO_REUSEADDR = SOL_SOCKET | PARAM_BOOLEAN | 2;
        SO_LINGER = SOL_SOCKET | 13;
        TCP_NODELAY = IPPROTO_TCP | PARAM_BOOLEAN | 1;
        TCP_QUICKACK = IPPROTO_TCP | PARAM_BOOLEAN | 12;
        TCP_SOCKET_OPTIONS[TCP_NODELAY & 0xff] = StandardSocketOptions.TCP_NODELAY;
        SO_SOCKET_OPTIONS[SO_BROADCAST & 0xff] = StandardSocketOptions.SO_BROADCAST;
        SO_SOCKET_OPTIONS[SO_KEEPALIVE & 0xff] = StandardSocketOptions.SO_KEEPALIVE;
        SO_SOCKET_OPTIONS[SO_SNDBUF & 0xff] = StandardSocketOptions.SO_SNDBUF;
        SO_SOCKET_OPTIONS[SO_RCVBUF & 0xff] = StandardSocketOptions.SO_RCVBUF;
        SO_SOCKET_OPTIONS[SO_REUSEADDR & 0xff] = StandardSocketOptions.SO_REUSEADDR;
        SO_SOCKET_OPTIONS[SO_LINGER & 0xff] = StandardSocketOptions.SO_LINGER;
    }

    @SuppressWarnings("unchecked")
    public static SocketOption<Object> getSocketOption(int name) {
        if ((name & IPPROTO_TCP) != 0) {
            return (SocketOption<Object>) TCP_SOCKET_OPTIONS[name & 0xff];
        } else if ((name & SOL_SOCKET) != 0) {
            return (SocketOption<Object>) SO_SOCKET_OPTIONS[name & 0xff];
        } else {
            return null;
        }
    }

    public static boolean isParamBoolean(int name) {
        return (name & PARAM_BOOLEAN) != 0;
    }

}
