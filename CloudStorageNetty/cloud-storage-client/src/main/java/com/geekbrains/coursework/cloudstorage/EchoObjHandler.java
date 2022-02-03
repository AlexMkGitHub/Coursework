package com.geekbrains.coursework.cloudstorage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class EchoObjHandler extends ChannelInboundHandlerAdapter {

    private Path serverDir = Paths.get("serverDir");
    private static final int SIZE = 256;
    //private File currentDir = new File("serverDir");
    private File currentDir = new File(System.getProperty("user.home"));

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client disconnected");
        //ctx.writeAndFlush("ls");

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("received: {}", msg);
        if (msg instanceof String) {
            String[] token = msg.toString().split("\\s+");

            if (token.length <= 0) {
                return;
            }

            if (token[0].equals("ls")) {
                ctx.writeAndFlush("cls");
                File[] arrFiles = currentDir.listFiles();
                List<File> lst = Arrays.asList(arrFiles);
                for (File file : lst) {
                    msg = file.getName();
                    ctx.writeAndFlush(msg + "\n");
                }
            } else ctx.writeAndFlush("From server: " + msg + "\n");

            if (token[0].equals("#GET#FILE")) {
                File file = currentDir.toPath().resolve(token[1]).toFile();
                System.out.println(token[1]);
                System.out.println(file);

            }

        }

    }
}
