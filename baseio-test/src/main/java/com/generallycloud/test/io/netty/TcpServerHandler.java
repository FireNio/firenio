package com.generallycloud.test.io.netty;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.common.DateUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

public class TcpServerHandler extends ChannelInboundHandlerAdapter {

    private AtomicInteger received = new AtomicInteger();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // TODO Auto-generated method stub
        // System.out.println("server receive message :"+ msg);
        msg = "yes server already accept your message" + msg;
        System.out.println(msg + "----" + received.getAndIncrement());
        ctx.channel().writeAndFlush(msg);
        // new Exception().printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("channelActive>>>>>>>>");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("hi 发生异常了:" + cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent e = (IdleStateEvent) evt;
        System.out.println(DateUtil.formatYyyy_MM_dd_HH_mm_ss_SSS() + "   " + e.state());
        super.userEventTriggered(ctx, evt);
    }

}
