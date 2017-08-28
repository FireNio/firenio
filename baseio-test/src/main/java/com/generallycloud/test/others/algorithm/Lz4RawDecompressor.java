/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.test.others.algorithm;

import com.generallycloud.baseio.common.UnsafeUtil;

public final class Lz4RawDecompressor {
    private static final int   LAST_LITERAL_SIZE = 5;
    private static final int   MIN_MATCH         = 4;

    private static final int   SIZE_OF_SHORT     = 2;
    private static final int   SIZE_OF_INT       = 4;
    private static final int   SIZE_OF_LONG      = 8;
    private static final int[] DEC_32_TABLE      = { 4, 1, 2, 1, 4, 4, 4, 4 };
    private static final int[] DEC_64_TABLE      = { 0, 0, 0, -1, 0, 1, 2, 3 };
    private static final int   OFFSET_SIZE       = 2;
    private static final int   TOKEN_SIZE        = 1;

    private Lz4RawDecompressor() {}

    public static int decompress(byte[] input, int inputOffset, int inputLength, byte[] output,
            int outputOffset, int maxOutputLength) throws MalformedInputException {
        long inputAddress = UnsafeUtil.ARRAY_BASE_OFFSET + inputOffset;
        long inputLimit = inputAddress + inputLength;
        long outputAddress = UnsafeUtil.ARRAY_BASE_OFFSET + outputOffset;
        long outputLimit = outputAddress + maxOutputLength;

        return decompress0(input, inputAddress, inputLimit, output, outputAddress, outputLimit);
    }

    private static int decompress0(final Object inputBase, final long inputAddress,
            final long inputLimit, final Object outputBase, final long outputAddress,
            final long outputLimit) {
        final long fastOutputLimit = outputLimit - SIZE_OF_LONG; // maximum offset in output buffer to which it's safe to write long-at-a-time

        long input = inputAddress;
        long output = outputAddress;

        if (inputAddress == inputLimit) {
            throw new MalformedInputException(0, "input is empty");
        }

        if (outputAddress == outputLimit) {
            if (inputLimit - inputAddress == 1
                    && UnsafeUtil.getByte(inputBase, inputAddress) == 0) {
                return 0;
            }
            return -1;
        }

        while (input < inputLimit) {
            final int token = UnsafeUtil.getByte(inputBase, input++) & 0xFF;

            // decode literal length
            int literalLength = token >>> 4; // top-most 4 bits of token
            if (literalLength == 0xF) {
                int value;
                do {
                    value = UnsafeUtil.getByte(inputBase, input++) & 0xFF;
                    literalLength += value;
                } while (value == 255 && input < inputLimit - 15);
            }

            // copy literal
            long literalOutputLimit = output + literalLength;
            if (literalOutputLimit > (fastOutputLimit - MIN_MATCH) || input
                    + literalLength > inputLimit - (OFFSET_SIZE + TOKEN_SIZE + LAST_LITERAL_SIZE)) {
                // copy the last literal and finish
                if (literalOutputLimit > outputLimit) {
                    throw new MalformedInputException(input - inputAddress,
                            "attempt to write last literal outside of destination buffer");
                }

                if (input + literalLength != inputLimit) {
                    throw new MalformedInputException(input - inputAddress,
                            "all input must be consumed");
                }

                // slow, precise copy
                UnsafeUtil.copyMemory(inputBase, input, outputBase, output, literalLength);
                input += literalLength;
                output += literalLength;
                break;
            }

            // fast copy. We may overcopy but there's enough room in input and output to not overrun them
            do {
                UnsafeUtil.putLong(outputBase, output, UnsafeUtil.getLong(inputBase, input));
                input += SIZE_OF_LONG;
                output += SIZE_OF_LONG;
            } while (output < literalOutputLimit);
            input -= (output - literalOutputLimit); // adjust index if we overcopied
            output = literalOutputLimit;

            // get offset
            // we know we can read two bytes because of the bounds check performed before copying the literal above
            int offset = UnsafeUtil.getShort(inputBase, input) & 0xFFFF;
            input += SIZE_OF_SHORT;

            long matchAddress = output - offset;
            if (matchAddress < outputAddress) {
                throw new MalformedInputException(input - inputAddress,
                        "offset outside destination buffer");
            }

            // compute match length
            int matchLength = token & 0xF; // bottom-most 4 bits of token
            if (matchLength == 0xF) {
                int value;
                do {
                    if (input > inputLimit - LAST_LITERAL_SIZE) {
                        throw new MalformedInputException(input - inputAddress);
                    }

                    value = UnsafeUtil.getByte(inputBase, input++) & 0xFF;
                    matchLength += value;
                } while (value == 255);
            }
            matchLength += MIN_MATCH; // implicit length from initial 4-byte match in encoder

            long matchOutputLimit = output + matchLength;

            // at this point we have at least 12 bytes of space in the output buffer
            // due to the fastLimit check before copying a literal, so no need to check again

            // copy repeated sequence
            if (offset < SIZE_OF_LONG) {
                // 8 bytes apart so that we can copy long-at-a-time below
                int increment32 = DEC_32_TABLE[offset];
                int decrement64 = DEC_64_TABLE[offset];

                UnsafeUtil.putByte(outputBase, output,
                        UnsafeUtil.getByte(outputBase, matchAddress));
                UnsafeUtil.putByte(outputBase, output + 1,
                        UnsafeUtil.getByte(outputBase, matchAddress + 1));
                UnsafeUtil.putByte(outputBase, output + 2,
                        UnsafeUtil.getByte(outputBase, matchAddress + 2));
                UnsafeUtil.putByte(outputBase, output + 3,
                        UnsafeUtil.getByte(outputBase, matchAddress + 3));
                output += SIZE_OF_INT;
                matchAddress += increment32;

                UnsafeUtil.putInt(outputBase, output, UnsafeUtil.getInt(outputBase, matchAddress));
                output += SIZE_OF_INT;
                matchAddress -= decrement64;
            } else {
                UnsafeUtil.putLong(outputBase, output,
                        UnsafeUtil.getLong(outputBase, matchAddress));
                matchAddress += SIZE_OF_LONG;
                output += SIZE_OF_LONG;
            }

            if (matchOutputLimit > fastOutputLimit - MIN_MATCH) {
                if (matchOutputLimit > outputLimit - LAST_LITERAL_SIZE) {
                    throw new MalformedInputException(input - inputAddress,
                            String.format("last %s bytes must be literals", LAST_LITERAL_SIZE));
                }

                while (output < fastOutputLimit) {
                    UnsafeUtil.putLong(outputBase, output,
                            UnsafeUtil.getLong(outputBase, matchAddress));
                    matchAddress += SIZE_OF_LONG;
                    output += SIZE_OF_LONG;
                }

                while (output < matchOutputLimit) {
                    UnsafeUtil.putByte(outputBase, output++,
                            UnsafeUtil.getByte(outputBase, matchAddress++));
                }
            } else {
                do {
                    UnsafeUtil.putLong(outputBase, output,
                            UnsafeUtil.getLong(outputBase, matchAddress));
                    matchAddress += SIZE_OF_LONG;
                    output += SIZE_OF_LONG;
                } while (output < matchOutputLimit);
            }

            output = matchOutputLimit; // correction in case we overcopied
        }

        return (int) (output - outputAddress);
    }

    private static class MalformedInputException extends RuntimeException {
        private final long offset;

        public MalformedInputException(long offset) {
            this(offset, "Malformed input");
        }

        public MalformedInputException(long offset, String reason) {
            super(reason + ": offset=" + offset);
            this.offset = offset;
        }

        public long getOffset() {
            return offset;
        }
    }
}
