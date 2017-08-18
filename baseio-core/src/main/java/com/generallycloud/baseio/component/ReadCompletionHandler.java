package com.generallycloud.baseio.component;

import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CompletionHandler;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class ReadCompletionHandler implements CompletionHandler<Integer, AioSocketChannel> {

    private Logger               logger        = LoggerFactory.getLogger(getClass());

    private ChannelByteBufReader byteBufReader = null;

    public ReadCompletionHandler(ChannelByteBufReader byteBufReader) {
        this.byteBufReader = byteBufReader;
    }

    @Override
    public void completed(Integer result, AioSocketChannel channel) {

        try {

            if (result < 1) {
                if (result == 0) {
                    channel.read(channel.getReadCache());
                    return;
                }
                CloseUtil.close(channel);
                return;
            }

            channel.active();

            ByteBuf buf = channel.getReadCache();

            buf.reverse();

            buf.flip();

            byteBufReader.accept(channel, buf);

        } catch (Exception e) {

            failed(e, channel);
        }

        channel.read(channel.getReadCache());
    }

    @Override
    public void failed(Throwable exc, AioSocketChannel channel) {

        if (exc instanceof AsynchronousCloseException) {
            //FIXME 产生该异常的原因是shutdownOutput后对方收到 read(-1)然后调用shutdownOutput,本地在收到read(-1)之前关闭了连接
            return;
        }

        logger.error(exc.getMessage() + ", channel:" + channel, exc);

        CloseUtil.close(channel);
    }

}
