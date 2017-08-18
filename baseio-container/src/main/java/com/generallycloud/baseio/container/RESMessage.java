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
package com.generallycloud.baseio.container;

import com.alibaba.fastjson.JSON;

public class RESMessage {

    public static RESMessage EMPTY_404      = new RESMessage(404, "EMPTY SERVICE-NAME");

    public static RESMessage SUCCESS        = new RESMessage(0, "SUCCESS");

    public static RESMessage SYSTEM_ERROR   = new RESMessage(-1, "SYSTEM ERROR");

    public static RESMessage UNAUTH         = new RESMessage(403, "REQUEST FORBIDDEN");

    public static RESMessage TIMEOUT        = new RESMessage(503, "TIMEOUT");

    public static RESMessage USER_EXIST     = new RESMessage(1001, "USER EXIST");
    public static RESMessage CONTACT_EXIST  = new RESMessage(1002, "CONTACT EXIST");
    public static RESMessage USER_NOT_EXIST = new RESMessage(1003, "USER NOT EXIST");

    private int              code;
    private Object           data;
    private String           description;

    protected RESMessage(int code) {
        this.code = code;
    }

    public RESMessage(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public RESMessage(int code, Object data, String description) {
        this.code = code;
        this.data = data;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    private String string;

    @Override
    public String toString() {
        if (string == null) {

            if (data == null) {
                if (description == null) {
                    string = new StringBuilder("{\"code\":").append(code).append("}").toString();
                } else {

                    string = new StringBuilder("{\"code\":").append(code)
                            .append(",\"description\":\"").append(description).append("\"}")
                            .toString();
                }
            } else {
                string = JSON.toJSONString(this);
            }
        }
        return string;
    }

    public static void main(String[] args) {

        System.out.println(SUCCESS);

        System.out.println(new RESMessage(100, null));
    }
}
