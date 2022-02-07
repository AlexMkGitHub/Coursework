package com.geekbrains.coursework.cloudstorage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class EchoObjHandler extends ChannelInboundHandlerAdapter {

    private Path serverDir = Paths.get("serverDir");
    private static final int SIZE = 256;
    //private File currentDir = new File("serverDir");
    private File currentDir = new File(System.getProperty("user.home"));

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected!!!!!!!!!!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client disconnected!!!!!!!!!!");
    }
}
