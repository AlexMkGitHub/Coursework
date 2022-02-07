package com.geekbrains.coursework.cloudstorage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class EchoObjHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private static final int SIZE = 256;
    private volatile int start = 0;
    private Path serverDir = Paths.get("serverDir").normalize();
    private File currentDir = new File("serverDir");
    private FileUploadFile ef;
    private FileUploadFile fileUploadFile;
    private FileUploadFile commandFile;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected");
        listServerFile(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client disconnected from server!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileUploadFile) {
            fileUploadFile = (FileUploadFile) msg;

            if (fileUploadFile.getCommand().equals("#LIST")) {
                listServerFile(ctx);
            }

            if (fileUploadFile.getCommand().equals("#GET#FILE")) {
                getFileFromCloud(ctx, msg);
            }

            if (fileUploadFile.getCommand().equals("#ADD#FILE")) {
                addFileInCloud(ctx, msg);
            }

            if (fileUploadFile.getCommand().equals("#NEW#DIR")) {
                createNewDir(ctx, msg);
            }

            if (fileUploadFile.getCommand().equals("#NEW#FILE")) {
                createNewFile(ctx, msg);
            }
        }

    }

    private void createNewFile(ChannelHandlerContext ctx, Object msg) {
        ef = (FileUploadFile) msg;
        String fileName = ef.getFileName();
        File currentFile = currentDir.toPath().resolve(fileName).toFile();
        Path currentFilePath = currentDir.toPath().resolve(fileName).normalize();
        System.out.println(Files.exists(currentFile.toPath()));
        System.out.println(currentFilePath);
        if (!Files.exists(currentFile.toPath())) {
            try {
                Files.createFile(currentFile.toPath());
                listServerFile(ctx);
                System.out.println("Создан файл " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else System.out.println("ТАКОЙ ФАЙЛ УЖЕ СОЗДАНА!");
    }

    private void createNewDir(ChannelHandlerContext ctx, Object msg) {
        ef = (FileUploadFile) msg;
        String fileName = ef.getFileName();
        File currentFile = currentDir.toPath().resolve(fileName).toFile();
        Path currentFilePath = currentDir.toPath().resolve(fileName).normalize();
        System.out.println(Files.exists(currentFile.toPath()));
        System.out.println(currentFilePath);
        if (!Files.exists(currentFile.toPath())) {
            try {
                Files.createDirectory(currentFile.toPath());
                listServerFile(ctx);
                System.out.println("Создана папка " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else System.out.println("ТАКАЯ ПАПКА УЖЕ СОЗДАНА!");
    }

    private void getFileFromCloud(ChannelHandlerContext ctx, Object msg) {
        ef = (FileUploadFile) msg;
        String fileName = ef.getFileName();
        System.out.println(fileName);
        File currentFile = currentDir.toPath().resolve(fileName).toFile();
        Path currentFilePath = currentDir.toPath().resolve(fileName).normalize();
        System.out.println(currentFile);
        this.ef = new FileUploadFile();
        ef.setFile(currentFile);
        String currentFileName = currentFile.getName();
        ef.setFileName(currentFileName);
        ef.setStarPos(0);
        ef.setCommand("#GET#FILE");
        System.out.println(ef.getFileName());
        try {
            currentFilePath = currentFilePath.toAbsolutePath().normalize();
            ef.setBytes(Files.readAllBytes(currentFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctx.writeAndFlush(ef);
        System.out.println("Файл " + fileName + " отправлен.");
        listServerFile(ctx);
        commandFile = new FileUploadFile();
        commandFile.setCommand("#VISIBLE");
        ctx.writeAndFlush(commandFile);
    }

    private void addFileInCloud(ChannelHandlerContext ctx, Object msg) {
        ef = (FileUploadFile) msg;
        String fileName = ef.getFileName();
        byte[] bytes = ef.getBytes();
        try {
            Files.write(currentDir.toPath().resolve(fileName), bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        commandFile = new FileUploadFile();
        commandFile.setCommand("#VISIBLE");
        ctx.writeAndFlush(commandFile);
        log.info("Файл, полученный от клиента, обрабатывается ...");
    }

    private void listServerFile(ChannelHandlerContext ctx) {
        commandFile = new FileUploadFile();
        commandFile.setCommand("#CLS");
        ctx.writeAndFlush(commandFile);
        File[] arrFiles = currentDir.listFiles();
        List<File> lst = Arrays.asList(arrFiles);
        for (File file : lst) {
            commandFile.setCommand(file.getName());
            ctx.writeAndFlush(commandFile);
            //ctx.writeAndFlush("lst");
        }
    }

}