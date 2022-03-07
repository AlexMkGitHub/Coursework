package com.geekbrains.coursework.cloudstorage;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class SerializablePipeline extends ChannelInitializer<SocketChannel> {
    private ServerController sc;

    public SerializablePipeline(ServerController sc) {
        this.sc = sc;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline()
                .addLast(
                        new ObjectEncoder(),
                        new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                        new EchoObjHandler(sc, channel)

                );
    }
}
