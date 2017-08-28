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

import com.generallycloud.test.test.ITest;
import com.generallycloud.test.test.ITestHandle;

public class TestLinkAndArrayList {

    public static void main(String[] args) {

        int size = 2999 * 10000;

        testArrayList(size);
        //		testLinkedList(size);

    }

    static void testLinkedList(int size) {

        Node n = new Node();
        final Node r = n;
        for (int i = 0; i < size; i++) {
            Node t = new Node();
            n.setNext(t);
            n = t;
        }

        ITestHandle.doTest(new ITest() {

            @Override
            public void test(int i1) throws Exception {
                Node t = r;
                for (;;) {
                    t.hello();
                    t = t.getNext();
                    if (t == null) {
                        break;
                    }
                }

            }
        }, 1, "TestLinkedList");

    }

    static void testArrayList(int size) {

        final Node[] ns = new Node[size];

        for (int i = 0; i < ns.length; i++) {
            ns[i] = new Node();
        }

        ITestHandle.doTest(new ITest() {

            @Override
            public void test(int i1) throws Exception {
                ns[i1].hello();
            }
        }, size, "TestArrayList");

    }

}

class Node {

    private Node next;

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void hello() {

    }
}
