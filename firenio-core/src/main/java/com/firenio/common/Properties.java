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
package com.firenio.common;

public class Properties extends java.util.Properties {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {

        Properties p = new Properties();

        p.insertOneRow("aaa=bbb");

        System.out.println(p.get("aaa"));

    }

    public boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, false);
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String temp = getProperty(key);
        if (Util.isNullOrBlank(temp)) {
            return defaultValue;
        }
        return Boolean.valueOf(temp);
    }

    public double getDoubleProperty(String key) {
        return getDoubleProperty(key, 0);
    }

    public double getDoubleProperty(String key, double defaultValue) {
        String temp = getProperty(key);
        if (Util.isNullOrBlank(temp)) {
            return defaultValue;
        }
        return Double.valueOf(temp);
    }

    public int getIntegerProperty(String key) {
        return getIntegerProperty(key, 0);
    }

    public int getIntegerProperty(String key, int defaultValue) {
        String temp = getProperty(key);
        if (Util.isNullOrBlank(temp)) {
            return defaultValue;
        }
        return Integer.parseInt(temp);
    }

    public long getLongProperty(String key) {
        return getLongProperty(key, 0);
    }

    public long getLongProperty(String key, long defaultValue) {
        String temp = getProperty(key);
        if (Util.isNullOrBlank(temp)) {
            return defaultValue;
        }
        return Long.parseLong(temp);
    }

    public String getPropertyNoBlank(String key) throws PropertiesException {
        String value = getProperty(key);
        if (Util.isNullOrBlank(value)) {
            throw new PropertiesException("property " + key + " is empty");
        }
        return value;
    }

    private void insertOneRow(String line) {
        if (Util.isNullOrBlank(line)) {
            return;
        }
        int index = line.indexOf("=");
        if (index == -1) {
            return;
        }
        String key   = line.substring(0, index);
        String value = line.substring(index + 1);
        key = trim(key);
        value = trim(value);
        put(key, value);
    }

    public Properties loadString(String content) {
        if (Util.isNullOrBlank(content)) {
            return this;
        }
        String[] lines = content.split("\n");
        for (String line : lines) {
            insertOneRow(line);
        }
        return this;
    }

    private String trim(String value) {
        return value.trim().replace("\r", "").replace("\t", "");
    }

    class PropertiesException extends Exception {

        private static final long serialVersionUID = 1L;

        public PropertiesException() {}

        public PropertiesException(String message) {
            super(message);
        }

        public PropertiesException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
