/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.protocol;

import java.nio.charset.Charset;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.common.ByteUtil;
import com.firenio.baseio.common.Encoding;

/**
 * 
 * <pre>
 * [0       ~              12]
 *  0	    = type
 *  1  ~  8   = timestamp
 *  9  ~  12  = sequenceNo
 * </pre>
 * 
 */
public class DatagramPacket {

    public static final int IP_HEADER;
    public static final int PACKET_HEADER;
    public static final int PACKET_MAX;
    public static final int TYPE_ACTION;
    public static final int TYPE_DATA;
    public static final int UDP_HEADER;

    static {
        IP_HEADER = 20;
        PACKET_HEADER = 1 + 8 + 4;
        TYPE_ACTION = 0;
        TYPE_DATA = 1;
        UDP_HEADER = 8;
        PACKET_MAX = 1500 - IP_HEADER - UDP_HEADER;
    }

    private byte[] data;
    private String dataString;
    private int    sequenceNo = -1; // 4 byte
    private long   timestamp  = -1; // 8 byte
    private int    type;

    private DatagramPacket() {}

    protected DatagramPacket(int type, long timestamp, int sequenceNO, byte[] data) {
        this.timestamp = timestamp;
        this.sequenceNo = sequenceNO;
        this.type = type;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public String getDataString() {
        return getDataString(Encoding.UTF8);
    }

    public String getDataString(Charset encoding) {
        if (dataString == null) {
            dataString = new String(getData(), getOffset(), getData().length - getOffset(),
                    encoding);
        }
        return dataString;
    }

    public int getOffset() {
        return PACKET_HEADER;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {

        return new StringBuilder("[data:").append(getDataString()).append(",seq:")
                .append(getSequenceNo()).append(",timestamp:").append(getTimestamp()).append("]")
                .toString();

    }

    public static DatagramPacket createPacket(ByteBuf buf) {
        byte[] data = buf.getBytes();
        DatagramPacket p = new DatagramPacket();
        p.type = data[0];
        p.timestamp = ByteUtil.byte2Long(data, 1);
        p.sequenceNo = ByteUtil.byte2Int(data, 9);
        p.data = data;
        return p;
    }

    public static DatagramPacket createSendPacket(byte[] data) {
        return createSendPacket(TYPE_ACTION, 0, 0, data);
    }

    public static DatagramPacket createSendPacket(int type, long timestamp, int sequenceNo,
            byte[] data) {
        byte[] newData = new byte[data.length + PACKET_HEADER];
        newData[0] = (byte) type;
        ByteUtil.long2Byte(newData, timestamp, 1);
        ByteUtil.int2Byte(newData, sequenceNo, 9);
        System.arraycopy(data, 0, newData, PACKET_HEADER, data.length);
        DatagramPacket p = new DatagramPacket();
        p.type = type;
        p.sequenceNo = sequenceNo;
        p.timestamp = timestamp;
        p.data = newData;
        return p;
    }

    public static DatagramPacket wrapSendPacket(byte[] data) {
        DatagramPacket p = new DatagramPacket();
        p.data = data;
        return p;
    }
}
