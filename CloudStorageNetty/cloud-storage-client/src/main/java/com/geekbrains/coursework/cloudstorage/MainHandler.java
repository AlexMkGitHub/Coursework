package com.geekbrains.coursework.cloudstorage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j

public class MainHandler extends ChannelInboundHandlerAdapter {
    private Path serverDir = Paths.get("serverDir");
    //private File dir = new File("serverDir");
    private File dir = new File(System.getProperty("user.home"));


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент подключился.");
        log.debug("Клиент подключился.");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Ошибка соединения: " + cause);
        ctx.close();
    }
}
