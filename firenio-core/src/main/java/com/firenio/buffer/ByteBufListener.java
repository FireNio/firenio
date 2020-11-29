package com.firenio.buffer;

import com.firenio.component.Channel;

public interface ByteBufListener {

    ByteBufListener NOOP = channel -> {
    };

    void onComplete(Channel channel);

}
