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
package com.generallycloud.baseio.protocol;

public class DatagramPacketGroup {

    private DatagramPacket[] packets;
    private int              size;

    //	private Logger logger =LoggerFactory.getLogger(DatagramPacketGroup.class);

    public DatagramPacketGroup(int max) {
        this.packets = new DatagramPacket[max];
    }

    public void addDatagramPacket(DatagramPacket packet) {

        size++;
        packets[packet.getSequenceNo()] = packet;
        packet.getData();

        //		logger.debug("_________________________add______packet:{}",packet);

    }

    public DatagramPacket[] getDatagramPackets() {

        return packets;
    }

    public int size() {
        return size;
    }

    public void foreach(DPForeach foreach) {

        int size = this.size;

        for (int i = 0; i < size; i++) {

            DatagramPacket p = packets[i];

            if (p == null) {
                size++;
                continue;
            }

            foreach.onPacket(p);
        }
    }

    public interface DPForeach {

        void onPacket(DatagramPacket packet);
    }

}
