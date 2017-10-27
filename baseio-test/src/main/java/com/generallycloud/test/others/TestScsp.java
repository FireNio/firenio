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
package com.generallycloud.test.others;

import java.lang.reflect.Field;

import com.generallycloud.baseio.common.UnsafeUtil;
import com.generallycloud.baseio.concurrent.Linkable;
import com.generallycloud.baseio.concurrent.ScspLinkedQueue;

/**
 * @author wangkai
 *
 */
public class TestScsp {

    public static void main(String[] args) throws Exception {

        ScspLinkedQueue<String> queue = new ScspLinkedQueue<>(new StringLink("h"));

        Field head = queue.getClass().getDeclaredField("head");
        Field tail = queue.getClass().getDeclaredField("tail");

        long h = UnsafeUtil.objectFieldOffset(head);

        long t = UnsafeUtil.objectFieldOffset(tail);
        
        System.out.println(h);
        System.out.println(t);
        
        System.out.println(t - h);

    }

    static class StringLink implements Linkable {

        private String     value;

        private StringLink next;

        private boolean    validate;

        public StringLink(String value) {
            this.value = value;
        }

        @Override
        public Linkable getNext() {
            return next;
        }

        @Override
        public void setNext(Linkable next) {
            this.next = (StringLink) next;
        }

        @Override
        public boolean isValidate() {
            return validate;
        }

        @Override
        public void setValidate(boolean validate) {
            this.validate = validate;
        }

        public String getValue() {
            return value;
        }

    }

}
