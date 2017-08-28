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
package com.generallycloud.test.others.algorithm;

import com.generallycloud.test.test.IntRandom;

/**
 * @author wangkai
 *
 */
public class TwoThreeTree {

    private Node4 node4 = new Node4();

    class Node {
        public Node(int key) {
            this.keyL = key;
        }

        int     keyL;
        int     keyR;
        Node    left;
        Node    middle;
        Node    right;
        boolean isTwo = true;
        Node    parent;
    }

    class Node4 {
        int  key1;
        int  key2;
        int  key3;
        Node n1;
        Node n2;
        Node n3;
        Node n4;
    }

    private Node root;

    public void insert(int key) {
        if (root == null) {
            root = new Node(key);
            return;
        }
        insert(key, root);
    }

    private void insert(int key, Node r) {
        for (;;) {
            if (r.isTwo) {
                r.isTwo = false;
                if (key > r.keyL) {
                    if (r.right == null) {
                        r.isTwo = false;
                        r.keyR = key;
                    } else {
                        r = r.right;
                    }
                } else {
                    if (r.left == null) {
                        r.keyR = r.keyL;
                        r.keyL = key;
                    } else {
                        r = r.left;
                    }
                }
            } else {
                if (key > r.keyR) {
                    int k = r.keyR;
                    r.keyR = key;
                    insertParent(k, r.parent);
                } else if (key > r.keyL) {
                    insertParent(key, r.parent);
                } else {
                    int k = r.keyL;
                    r.keyL = key;
                    insertParent(k, r.parent);
                }
            }
        }
    }

    private void insertParent(int key, Node r) {
        if (r == null) {

        } else {
            if (r.isTwo) {
                r.isTwo = false;
                if (key > r.keyL) {
                    r.keyR = key;

                }

            }

        }

    }

    private void filterInsert(int key, Node r) {
        if (r == null) {
            r = root;
            r.isTwo = true;
            if (key > r.keyR) {
                Node n = new Node(r.keyR);
                n.left = r;
                n.right = new Node(key);

            }

            return;
        }
        insert(key, r);
    }

    public static void main(String[] args) {

        IntRandom random = new IntRandom(100);

    }

}
