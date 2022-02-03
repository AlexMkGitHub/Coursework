package com.geekbrains.coursework.cloudstorage;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.ocsp.OcspClientHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class Network {
    private SocketChannel channel;
    private static final String HOST = "localhost";
    private static final int PORT = 8189;
    private Callback messageCallback;

    public Network(Callback msgCallback) {
        this.messageCallback = msgCallback;
        Thread thread = new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                channel = socketChannel;
                                socketChannel.pipeline().addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), new EchoObjHandler() {

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        if (msgCallback != null) {
                                            msgCallback.callback(msg);
                                        }
                                    }
                                });
                            }
                        });
                ChannelFuture future = b.connect(HOST, PORT);
                future.channel().closeFuture().sync();

            } catch (Exception e) {
                log.error("Ошибка: " + e);
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendMesage(Object obj) {
        channel.writeAndFlush(obj);
    }
}
