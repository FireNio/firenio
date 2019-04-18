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
package com.firenio.collection;

import com.firenio.buffer.ByteBuf;
import com.firenio.common.Unsafe;

/**
 * @author wangkai
 */
public class ByteTree {

    private Node root = new ByteNode();

    public void add(String s) {
        add(s.getBytes(), s);
    }

    private void add(byte[] bytes, Object value) {
        byte curr_b = bytes[0];
        Node next_n = root = root.append(curr_b);
        Node temp_n = next_n.next(curr_b);
        Node prev_n = next_n;
        byte prev_b = curr_b;
        for (int i = 1; i < bytes.length; i++) {
            curr_b = bytes[i];
            next_n = temp_n.append(curr_b);
            prev_n.set(prev_b, next_n);
            prev_n = next_n;
            prev_b = curr_b;
            temp_n = next_n.next(curr_b);
        }
        temp_n.value = value;
    }

    public String getString(byte[] bytes, int offset, int length) {
        return (String) get(bytes, offset, length);
    }

    public Object get(byte[] bytes, int offset, int length) {
        Node node  = root;
        int  count = offset + length;
        for (int i = offset; i < count; i++) {
            node = node.next(bytes[i]);
            if (node == null) {
                return null;
            }
        }
        return node.value;
    }

    public String getString(ByteBuf buf, int absPos, int length) {
        return (String) get(buf, absPos, length);
    }

    public Object get(ByteBuf buf, int absPos, int length) {
        if (buf.hasArray()) {
            return get(buf.array(), absPos, length);
        } else {
            return get(buf.address() + absPos, length);
        }
    }

    public String getString(long address, int length) {
        return (String) get(address, length);
    }

    public Object get(long address, int length) {
        Node node  = root;
        long count = address + length;
        for (long i = address; i < count; i++) {
            node = node.next(Unsafe.getByte(i));
            if (node == null) {
                return null;
            }
        }
        return node.value;
    }

    static abstract class Node {

        Object value;

        abstract Node next(byte b);

        abstract Node append(byte b);

        abstract Node set(byte b, Node node);

    }

    static final class ArrayNode extends Node {

        private Node[] nodes;
        private int    shift = -1;

        ArrayNode(byte old_b, Node old_n, byte new_b, Node new_n, Object value) {
            int old_i = old_b & 0xff;
            int new_i = new_b & 0xff;
            this.value = value;
            this.shift = Math.min(old_i, new_i);
            increase(Math.max(old_i, new_i) - shift + 1);
            nodes[old_i - shift] = old_n;
            nodes[new_i - shift] = new_n;
        }

        @Override
        Node next(byte b) {
            int i = (b & 0xff) - shift;
            if (i >= nodes.length || i < 0) {
                return null;
            }
            return nodes[i];
        }

        @Override
        Node append(byte b) {
            set((b & 0xff), new ByteNode(), true);
            return this;
        }

        @Override
        Node set(byte b, Node node) {
            set(b & 0xff, node);
            return this;
        }

        Node set(int i, Node node) {
            return set(i, node, false);
        }

        Node set(int i, Node node, boolean ifNull) {
            if (shift == -1) {
                shift = i;
            }
            if (i - shift < 0) {
                increase(i - shift);
            } else {
                increase(i + 1 - shift);
            }
            if (ifNull) {
                if (nodes[i - shift] == null) {
                    nodes[i - shift] = node;
                }
            } else {
                nodes[i - shift] = node;
            }
            return this;
        }

        private void increase(int size) {
            if (size == 0) {
                size = -1;
            }
            if (nodes == null) {
                nodes = new Node[size];
            } else if (size < 0) {
                Node[] temp = new Node[nodes.length - size];
                System.arraycopy(nodes, 0, temp, -size, nodes.length);
                nodes = temp;
                shift += size;
            } else if (nodes.length < size) {
                Node[] temp = new Node[size];
                System.arraycopy(nodes, 0, temp, 0, nodes.length);
                nodes = temp;
            }
        }
    }

    static final class ByteNode extends Node {

        private byte b;
        private Node next;

        @Override
        Node next(byte b) {
            return b == this.b ? next : null;
        }

        @Override
        Node append(byte b) {
            if (this.b == b) {
                return this;
            } else {
                if (next == null) {
                    this.b = b;
                    this.next = new ByteNode();
                    return this;
                } else {
                    return new ArrayNode(this.b, this.next, b, new ByteNode(), value);
                }
            }
        }

        @Override
        Node set(byte b, Node n) {
            if (this.b == b) {
                this.next = n;
                return this;
            } else {
                if (next == null) {
                    this.b = b;
                    this.next = n;
                    return this;
                } else {
                    return new ArrayNode(this.b, this.next, b, n, value);
                }
            }
        }

        @Override
        public String toString() {
            if (b == 0) {
                return "single " + value;
            } else {
                return "single " + (char) b;
            }
        }

    }
}
