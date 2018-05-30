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
package com.generallycloud.sample.baseio.protobase;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class Sl4jLogger implements Log {

    // ------------------------------------------------------------- Attributes

    /** Log to this logger */
    private transient Logger logger = null;

    // ------------------------------------------------------------
    // Static Initializer.
    //
    // Note that this must come after the static variable declarations
    // otherwise initialiser expressions associated with those variables
    // will override any settings done here.
    //
    // Verify that log4j is available, and that it is version 1.2.
    // If an ExceptionInInitializerError is generated, then LogFactoryImpl
    // will treat that as meaning that the appropriate underlying logging
    // library is just not present - if discovery is in progress then
    // discovery will continue.
    // ------------------------------------------------------------

    // ------------------------------------------------------------ Constructor

    public Sl4jLogger() {}

    /**
     * Base constructor.
     */
    public Sl4jLogger(String name) {
        this.logger = getLogger(name);
    }

    /** 
     * For use with a log4j factory.
     */
    public Sl4jLogger(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException(
                    "Warning - null logger in constructor; possible log4j misconfiguration.");
        }
        this.logger = logger;
    }

    // --------------------------------------------------------- 
    // Implementation
    //
    // Note that in the methods below the Priority class is used to define
    // levels even though the Level class is supported in 1.2. This is done
    // so that at compile time the call definitely resolves to a call to
    // a method that takes a Priority rather than one that takes a Level.
    // 
    // The Category class (and hence its subclass Logger) in version 1.2 only
    // has methods that take Priority objects. The Category class (and hence
    // Logger class) in version 1.3 has methods that take both Priority and
    // Level objects. This means that if we use Level here, and compile
    // against log4j 1.3 then calls would be bound to the versions of
    // methods taking Level objects and then would fail to run against
    // version 1.2 of log4j.
    // --------------------------------------------------------- 

    /**
     * Logs a message with <code>org.apache.log4j.Priority.TRACE</code>.
     * When using a log4j version that does not support the <code>TRACE</code>
     * level, the message will be logged at the <code>DEBUG</code> level.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    @Override
    public void trace(Object message) {
        getLogger().trace(String.valueOf(message));
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.TRACE</code>.
     * When using a log4j version that does not support the <code>TRACE</code>
     * level, the message will be logged at the <code>DEBUG</code> level.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    @Override
    public void trace(Object message, Throwable t) {
        getLogger().trace(String.valueOf(message), t);
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.DEBUG</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    @Override
    public void debug(Object message) {
        getLogger().debug(String.valueOf(message));
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.DEBUG</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    @Override
    public void debug(Object message, Throwable t) {
        getLogger().debug(String.valueOf(message), t);
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.INFO</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#info(Object)
     */
    @Override
    public void info(Object message) {
        getLogger().info(String.valueOf(message));
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.INFO</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    @Override
    public void info(Object message, Throwable t) {
        getLogger().info(String.valueOf(message), t);
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.WARN</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    @Override
    public void warn(Object message) {
        getLogger().warn(String.valueOf(message));
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.WARN</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    @Override
    public void warn(Object message, Throwable t) {
        getLogger().warn(String.valueOf(message), t);
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.ERROR</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#error(Object)
     */
    @Override
    public void error(Object message) {
        getLogger().error(String.valueOf(message));
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.ERROR</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    @Override
    public void error(Object message, Throwable t) {
        getLogger().error(String.valueOf(message), t);
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.FATAL</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    @Override
    public void fatal(Object message) {
        getLogger().error(String.valueOf(message));
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.FATAL</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    @Override
    public void fatal(Object message, Throwable t) {
        getLogger().error(String.valueOf(message), t);
    }

    private Logger getLogger() {
        return logger;
    }

    /**
     * Return the native Logger instance we are using.
     */
    public Logger getLogger(String name) {
        if (logger == null) {
            logger = LoggerFactory.getLogger(name);
        }
        return (this.logger);
    }

    public Logger getLogger(Class<?> clazz) {
        if (logger == null) {
            logger = LoggerFactory.getLogger(clazz);
        }
        return (this.logger);
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>DEBUG</code> priority.
     */
    @Override
    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
    * Check whether the Log4j Logger used is enabled for <code>ERROR</code> priority.
    */
    @Override
    public boolean isErrorEnabled() {
        return getLogger().isErrorEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>FATAL</code> priority.
     */
    @Override
    public boolean isFatalEnabled() {
        return getLogger().isErrorEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>INFO</code> priority.
     */
    @Override
    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>TRACE</code> priority.
     * When using a log4j version that does not support the TRACE level, this call
     * will report whether <code>DEBUG</code> is enabled or not.
     */
    @Override
    public boolean isTraceEnabled() {
        return getLogger().isTraceEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>WARN</code> priority.
     */
    @Override
    public boolean isWarnEnabled() {
        return getLogger().isWarnEnabled();
    }
}
