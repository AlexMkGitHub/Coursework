package com.geekbrains.coursework.cloudstorage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class EchoStringHandler extends SimpleChannelInboundHandler<String> {
    private Path serverDir = Paths.get("serverDir");
    //private File currentDir = new File("serverDir");
    private File currentDir = new File(System.getProperty("user.home"));

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client disconnected");
        ctx.flush();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        log.info("received: {}", s);
        String[] token = s.split("\\s+");

        if (token.length <= 0) {
            return;
        }

        if (token[0].equals("ls")) {
            ctx.writeAndFlush("cls");
            File[] arrFiles = currentDir.listFiles();
            List<File> lst = Arrays.asList(arrFiles);
            for (File file : lst) {
                s = file.getName();
                ctx.writeAndFlush(s + "\n");
            }
        } else ctx.writeAndFlush("From server: " + s + "\n");

//        if (token.length > 1) {
//            System.out.println(Arrays.toString(token));
//            System.out.println(token[1]);
//        }

        if (token[0].equals("#GET#FILE")) {
            File file = currentDir.toPath().resolve(token[1]).toFile();
            System.out.println(token[1]);
            System.out.println(file);

        }
    }
}
