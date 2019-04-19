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
package com.firenio.log;

public interface Logger {

    void debug(String msg);

    void debug(String msg, Object param);

    void debug(String msg, Object... params);

    void debug(String msg, Object param, Object param1);

    void debug(String msg, Throwable t);

    void error(String msg);

    void error(String msg, Object param);

    void error(String msg, Object... params);

    void error(String msg, Object param, Object param1);

    void error(String msg, Throwable t);

    void error(Throwable e);

    String getName();

    void info(String msg);

    void info(String msg, Object param);

    void info(String msg, Object... params);

    void info(String msg, Object param, Object param1);

    boolean isDebugEnabled();

    boolean isErrorEnabled();

    boolean isInfoEnabled();

    boolean isWarnEnabled();

    void warn(String msg);

    void warn(String msg, Object param);

    void warn(String msg, Object... params);

    void warn(String msg, Object param, Object param1);

    void warn(String msg, Throwable t);

}
