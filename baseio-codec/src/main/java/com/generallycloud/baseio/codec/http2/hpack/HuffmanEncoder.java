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
package com.generallycloud.baseio.codec.http2.hpack;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteProcessor;

final class HuffmanEncoder {

    private final int[]                  codes;
    private final byte[]                 lengths;
    private final EncodedLengthProcessor encodedLengthProcessor = new EncodedLengthProcessor();
    private final EncodeProcessor        encodeProcessor        = new EncodeProcessor();

    HuffmanEncoder() {
        this(HpackUtil.HUFFMAN_CODES, HpackUtil.HUFFMAN_CODE_LENGTHS);
    }

    /**
     * Creates a new Huffman encoder with the specified Huffman coding.
     *
     * @param codes the Huffman codes indexed by symbol
     * @param lengths the length of each Huffman code
     */
    private HuffmanEncoder(int[] codes, byte[] lengths) {
        this.codes = codes;
        this.lengths = lengths;
    }

    /**
     * Compresses the input string literal using the Huffman coding.
     *
     * @param out the output stream for the compressed data
     * @param data the string literal to be Huffman encoded
     */
    public void encode(ByteBuf out, String data) {
        encodeSlowPath(out, data);
    }

    private void encodeSlowPath(ByteBuf out, String data) {
        long current = 0;
        int n = 0;

        for (int i = 0; i < data.length(); i++) {
            int b = data.charAt(i) & 0xFF;
            int code = codes[b];
            int nbits = lengths[b];

            current <<= nbits;
            current |= code;
            n += nbits;

            while (n >= 8) {
                n -= 8;
                out.putByte((byte) (current >> n));
            }
        }

        if (n > 0) {
            current <<= 8 - n;
            current |= 0xFF >>> n; // this should be EOS symbol
            out.putByte((byte) current);
        }
    }

    /**
     * Returns the number of bytes required to Huffman encode the input string literal.
     *
     * @param data the string literal to be Huffman encoded
     * @return the number of bytes required to Huffman encode <code>data</code>
     */
    public int getEncodedLength(String data) {
        return getEncodedLengthSlowPath(data);
    }

    private int getEncodedLengthSlowPath(String data) {
        long len = 0;
        for (int i = 0; i < data.length(); i++) {
            len += lengths[data.charAt(i) & 0xFF];
        }
        return (int) ((len + 7) >> 3);
    }

    private final class EncodeProcessor implements ByteProcessor {
        ByteBuf      out;
        private long current;
        private int  n;

        @Override
        public boolean process(byte value) {
            int b = value & 0xFF;
            int nbits = lengths[b];

            current <<= nbits;
            current |= codes[b];
            n += nbits;

            while (n >= 8) {
                n -= 8;
                out.putByte((byte) (current >> n));
            }
            return true;
        }

        void end() {
            try {
                if (n > 0) {
                    current <<= 8 - n;
                    current |= 0xFF >>> n; // this should be EOS symbol
                    out.putByte((byte) current);
                }
            } finally {
                out = null;
                current = 0;
                n = 0;
            }
        }
    }

    private final class EncodedLengthProcessor implements ByteProcessor {
        private long len;

        @Override
        public boolean process(byte value) {
            len += lengths[value & 0xFF];
            return true;
        }

        void reset() {
            len = 0;
        }

        int length() {
            return (int) ((len + 7) >> 3);
        }
    }
}
