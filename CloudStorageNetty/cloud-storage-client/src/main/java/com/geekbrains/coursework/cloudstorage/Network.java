package com.geekbrains.coursework.cloudstorage;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


@Slf4j

public class Network {
    private SocketChannel channel;
    private static final String HOST = "localhost";
    private static final int PORT = 8189;
    private Callback messageCallback;
    private boolean allRead = true;
    private File currentDir;
    private ClientController clientController;
    private FileUploadFile ef;


    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public boolean isAllRead() {
        return allRead;
    }

    public Network(Callback msgCallback, ClientController clientController) {
        this.clientController = clientController;
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
                                socketChannel.pipeline().addLast(new ObjectEncoder(),
                                        new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                                        new EchoObjHandler() {

                                            @Override
                                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                                if (msgCallback != null) {
                                                    msgCallback.callback(msg);
                                                }
                                                getFile(msg);
                                            }

                                            @Override
                                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                                clientController.generalPanel.setOpacity(1.0);
                                                clientController.setChannelActive(true);
                                                clientController.buttonPanel.setDisable(false);
                                                clientController.connected.setVisible(false);
                                                Platform.runLater(() -> clientController.serverLabel
                                                        .setText("Облачное хранилище."));
                                                clientController.clientView.refresh();
                                                clientController.serverView.refresh();

                                            }

                                            @Override
                                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                                clientController.generalPanel.setOpacity(1.0);
                                                clientController.setChannelActive(false);
                                                clientController.connected.setVisible(true);
                                                clientController.buttonPanel.setDisable(true);
                                                Platform.runLater(() -> clientController.serverLabel
                                                        .setText("Отсутствует подключение к серверу!"));
                                                clientController.clientView.refresh();
                                                clientController.serverView.refresh();
                                                ctx.channel().close();
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

    public void sendMessage(Object msg) {
        allRead = false;
        channel.writeAndFlush(msg);
    }

    public void getFile(Object msg) throws IOException {
        ef = (FileUploadFile) msg;
        if (ef.getCommand() == null) {
            ef.setCommand("#LIST");
            channel.writeAndFlush(ef);
            clientController.fileMetods.fillCurrentDirFiles();
        } else if (ef.getCommand().equals("#GET#FILE")) {
            String fileName = ef.getFileName();
            byte[] bytes = ef.getBytes();
            Files.write(currentDir.toPath().resolve(fileName), bytes);
            ef.setCommand("#LIST");
            channel.writeAndFlush(ef);
            clientController.fileMetods.fillCurrentDirFiles();
        }

    }


}
