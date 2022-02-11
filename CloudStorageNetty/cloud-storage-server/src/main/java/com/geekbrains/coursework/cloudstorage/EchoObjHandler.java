package com.geekbrains.coursework.cloudstorage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class EchoObjHandler extends ChannelInboundHandlerAdapter {
    private File currentDir = new File("serverDir");
    private FileUploadFile ef;
    private FileUploadFile fileUploadFile;
    private FileUploadFile commandFile;
    private File currentFile;
    private Path currentFilePath;
    private String fileName;
    private ServerController sc;
    private SocketChannel channel;

    public EchoObjHandler(ServerController sc, SocketChannel channel) {
        this.channel = channel;
        this.sc = sc;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Клиент подключился к серверу!");
        sc.serverInfo.appendText("Клиент " + channel.remoteAddress() + " подключился к серверу!\n");
        listServerFile(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Клиент отключился от сервера!");
        sc.serverInfo.appendText("Клиент " + channel.remoteAddress() + " отключился от серврера!\n");
        channel.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
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
        getFileInfo(msg);
        if (!Files.exists(currentFile.toPath())) {
            try {
                Files.createFile(currentFile.toPath());
                listServerFile(ctx);
                sc.serverInfo.appendText("Создан файл  " + fileName + "\n");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else sc.serverInfo.appendText("ФАЙЛ " + fileName + " УЖЕ СОЗДАН!\n");
    }

    private void createNewDir(ChannelHandlerContext ctx, Object msg) {
        getFileInfo(msg);
        if (!Files.exists(currentFile.toPath())) {
            try {
                Files.createDirectory(currentFile.toPath());
                listServerFile(ctx);
                sc.serverInfo.appendText("Создана папка  " + fileName + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else sc.serverInfo.appendText("ПАПКА " + fileName + " УЖЕ СОЗДАНА!\n");
    }

    private void getFileFromCloud(ChannelHandlerContext ctx, Object msg) {
        getFileInfo(msg);
        this.ef = new FileUploadFile();
        ef.setFile(currentFile);
        String currentFileName = currentFile.getName();
        ef.setFileName(currentFileName);
        ef.setStarPos(0);
        ef.setCommand("#GET#FILE");
        try {
            currentFilePath = currentFilePath.toAbsolutePath().normalize();
            ef.setBytes(Files.readAllBytes(currentFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctx.writeAndFlush(ef);
        sc.serverInfo.appendText("Файл " + fileName + " отправлен.\n");
        listServerFile(ctx);
        commandFile = new FileUploadFile();
        commandFile.setCommand("#VISIBLE");
        ctx.writeAndFlush(commandFile);
    }

    private void addFileInCloud(ChannelHandlerContext ctx, Object msg) {
        ef = (FileUploadFile) msg;
        fileName = ef.getFileName();
        byte[] bytes = ef.getBytes();
        try {
            Files.write(currentDir.toPath().resolve(fileName), bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        commandFile = new FileUploadFile();
        commandFile.setCommand("#VISIBLE");
        ctx.writeAndFlush(commandFile);
        sc.serverInfo.appendText("Получен файл " + fileName + " \n");
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
        }
    }

    private void getFileInfo(Object msg) {
        ef = (FileUploadFile) msg;
        fileName = ef.getFileName();
        currentFile = currentDir.toPath().resolve(fileName).toFile();
        currentFilePath = currentDir.toPath().resolve(fileName).normalize();
    }

}