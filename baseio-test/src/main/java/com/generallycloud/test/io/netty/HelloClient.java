package com.generallycloud.test.io.netty;

import java.util.concurrent.CountDownLatch;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HelloClient extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        CountDownLatch latch = NettyClient.latch;

        latch.countDown();
        long count = latch.getCount();
        if (count < 50) {
            System.out.println("************************================" + count);
        }

        //		System.out.println(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("client exception is general");
    }
}
