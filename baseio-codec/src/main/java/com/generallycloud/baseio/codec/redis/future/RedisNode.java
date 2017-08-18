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
package com.generallycloud.baseio.codec.redis.future;

public class RedisNode {

    private char        type;

    private Object      value;

    private RedisNode   parent;

    private RedisNode[] children;

    private RedisNode   next;

    public RedisNode() {}

    public RedisNode(RedisNode parent) {
        this.parent = parent;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public RedisNode getParent() {
        return parent;
    }

    public void setParent(RedisNode parent) {
        this.parent = parent;
    }

    public RedisNode[] getChildren() {
        return children;
    }

    // public void setChildren(RedisNode[] children) {
    // this.children = children;
    // }

    public RedisNode getNext() {
        return next;
    }

    public void setNext(RedisNode next) {
        this.next = next;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public RedisNode deepNext() {

        if (next == null) {

            if (parent == null) {
                return null;
            }

            return parent.deepNext();
        }

        return next;
    }

    public void createChildren(int size) {

        this.children = new RedisNode[size];

        RedisNode last = null;

        for (int i = 0; i < size; i++) {

            RedisNode n = new RedisNode(this);

            if (last == null) {
                last = n;
            } else {
                last.setNext(n);
                last = n;
            }

            children[i] = n;
        }
    }

    @Override
    public String toString() {

        if (value == null) {

            StringBuilder b = new StringBuilder();

            for (int i = 0; i < children.length; i++) {
                b.append(children[i].toString());
                b.append(";");
            }

            return b.toString();
        }

        return String.valueOf(value);
    }

}
