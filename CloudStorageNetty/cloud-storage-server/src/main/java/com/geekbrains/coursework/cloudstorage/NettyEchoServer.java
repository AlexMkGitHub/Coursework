package com.geekbrains.coursework.cloudstorage;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyEchoServer {
    protected EventLoopGroup auth;
    protected EventLoopGroup worker;

    private ServerController sc;

    public NettyEchoServer(ServerController sc) {
        this.sc = sc;
    }

    public void start() {
        Thread thread = new Thread(() -> {
            this.auth = new NioEventLoopGroup(1);
            this.worker = new NioEventLoopGroup();

            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.channel(NioServerSocketChannel.class)
                        .group(auth, worker)
                        .childHandler(new SerializablePipeline(sc));

                ChannelFuture future = serverBootstrap.bind(8189).sync();
                sc.serverInfo.setText("Сервер запущен...\n");
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                sc.serverInfo.appendText("Ошибка: " + e + "\n");
            } finally {
                auth.shutdownGracefully();
                worker.shutdownGracefully();
                sc.serverInfo.appendText("Сервер остановлен!!!\n");
            }
        });
        thread.setDaemon(true);
        thread.start();

    }
}
