package com.generallycloud.test.io.netty;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class NettyClient {

    public static int                  time  = 1000000;
    public static final CountDownLatch latch = new CountDownLatch(time);

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder",
                            new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                    pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));

                    pipeline.addLast("handler", new HelloClient());
                }
            });

            System.out.println("################## Test start ####################");
            long old = System.currentTimeMillis();

            ChannelFuture f = b.connect("127.0.0.1", 5656).sync();

            System.out.println(f.isSuccess());

            Channel channel = f.channel();

            System.out.println("channel is active :" + channel.isActive() + ",channel:" + channel);

            for (int i = 0; i < time; i++) {
                String s = "hello Service! ---> :" + i;
                ChannelFuture f1 = channel.writeAndFlush(s);
                f1.isDone();
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long spend = (System.currentTimeMillis() - old);
            System.out.println("## Execute Time:" + time);
            System.out.println("## OP/S:" + new BigDecimal(time * 1000)
                    .divide(new BigDecimal(spend), 2, BigDecimal.ROUND_HALF_UP));
            System.out.println("## Expend Time:" + spend);

            f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
