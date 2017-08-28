package com.generallycloud.test.io.netty;

import com.generallycloud.baseio.common.ThreadUtil;

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

public class NettyClientThread {

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

            ChannelFuture f = b.connect("127.0.0.1", 5656).sync();

            System.out.println(f.isSuccess());

            Channel channel = f.channel();

            System.out.println("channel is active :" + channel.isActive() + ",channel:" + channel);

            int len = 1024 * 64;
            StringBuilder s = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                s.append(len % 10);
            }
            final String msg = s.toString();
            ThreadUtil.execute(new Runnable() {

                @Override
                public void run() {
                    int i = 0;
                    for (;;) {
                        //						String s = "hello Service! ---> :" + i;
                        ChannelFuture f = channel.writeAndFlush(msg);
                        ThreadUtil.sleep(1);
                        System.out.println(f.isDone() + "--------" + i);
                        i++;
                    }
                }
            });

            ThreadUtil.sleep(Integer.MAX_VALUE);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
