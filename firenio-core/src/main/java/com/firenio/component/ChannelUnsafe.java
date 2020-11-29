package com.firenio.component;

import java.io.IOException;

/**
 * @author: wangkai
 **/
public class ChannelUnsafe {

    /**
     * Not safe API, DO NOT USE this unless you are really know how to use this.
     */
    public static void read(Channel channel) throws Exception {
        channel.read();
    }

    /**
     * Not safe API, DO NOT USE this unless you are really know how to use this.
     */
    public static void write(Channel channel){
        channel.write();
    }





}
