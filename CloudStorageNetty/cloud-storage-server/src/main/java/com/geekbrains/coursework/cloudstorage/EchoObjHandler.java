package com.geekbrains.coursework.cloudstorage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class EchoObjHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private static final int SIZE = 256;
    private volatile int start = 0;
    private Path serverDir = Paths.get("serverDir");
    private File currentDir = new File("serverDir");
    //private File currentDir = new File(System.getProperty("user.home"));

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("Client connected");
        listServerFile(ctx);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        listServerFile(ctx);
        super.channelInactive(ctx);
        log.info("Client disconnected from server!");

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("received: msg");
        if (msg instanceof String) {
            String[] token = msg.toString().split("\\s+");

            if (token.length <= 0) {
                return;
            }

            if (token[0].equals("ls")) {
                listServerFile(ctx);
            } else ctx.writeAndFlush("From server: " + msg + "\n");

            if (token[0].equals("#GET#FILE")) {
                File fileName = currentDir.toPath().resolve(token[1]).toFile();
                System.out.println(fileName);
                if (fileName.exists()) {
                    //FileUploadFile ef = (FileUploadFile) fileName;
                    ctx.writeAndFlush(fileName);
                }
                listServerFile(ctx);
            }
        }
        if (msg instanceof FileUploadFile) {
            log.info("Файл, полученный от клиента, обрабатывается ...");
            FileUploadFile ef = (FileUploadFile) msg;
            byte[] bytes = ef.getBytes();
            byteRead = ef.getEndPos();
            System.out.println(byteRead);
            String md5 = ef.getFile_md5();//имя файла
            System.out.println(md5);
            String path = currentDir + File.separator + md5;
            File file = new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");// r: режим только для чтения rw: режим чтения-записи
            randomAccessFile.seek(start);// Перемещаем позицию указателя записи файла,
            randomAccessFile.write(bytes);// Вызывается метод seek (start), который относится к позиционированию указателя записи файла на позицию начального байта. Другими словами, программа начнет запись данных из начального байта
            start = start + byteRead;
            if (byteRead > 0) {
                ctx.writeAndFlush(start);// Отправить сообщение клиенту
                listServerFile(ctx);
                randomAccessFile.close();
                listServerFile(ctx);
                if (byteRead != 1024 * 10) {
                    Thread.sleep(1000);
                    listServerFile(ctx);
                    channelInactive(ctx);
                }
            } else {
                listServerFile(ctx);
                ctx.close();
            }
            log.info("Обработано, путь к файлу: " + path + "," + byteRead);
            listServerFile(ctx);
        }
    }

    private void listServerFile(ChannelHandlerContext ctx) {
        String msg;
        ctx.writeAndFlush("cls");
        File[] arrFiles = currentDir.listFiles();
        List<File> lst = Arrays.asList(arrFiles);
        for (File file : lst) {
            msg = file.getName();
            ctx.writeAndFlush(msg + "\n");
        }
    }

    public void inputFile(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("Файл, полученный от клиента, обрабатывается ...");
        if (msg instanceof FileUploadFile) {
            FileUploadFile ef = (FileUploadFile) msg;
            byte[] bytes = ef.getBytes();
            byteRead = ef.getEndPos();
            String md5 = ef.getFile_md5();//имя файла
            String path = currentDir + File.separator + md5;
            File file = new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");// r: режим только для чтения rw: режим чтения-записи
            randomAccessFile.seek(start);// Перемещаем позицию указателя записи файла,
            randomAccessFile.write(bytes);// Вызывается метод seek (start), который относится к позиционированию указателя записи файла на позицию начального байта. Другими словами, программа начнет запись данных из начального байта
            start = start + byteRead;
            if (byteRead > 0) {
                ctx.writeAndFlush(start);// Отправить сообщение клиенту
                randomAccessFile.close();
                if (byteRead != 1024 * 10) {
                    Thread.sleep(1000);
                    channelInactive(ctx);
                }
            } else {
                ctx.close();
            }
            log.info("Обработано, путь к файлу:" + path + "," + byteRead);
        }
    }
}


//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
//        log.info("received: {}", s);
//        String[] token = s.split("\\s+");
//
//        if (token.length <= 0) {
//            return;
//        }
//
//        if (token[0].equals("ls")) {
//            ctx.writeAndFlush("cls");
//            File[] arrFiles = currentDir.listFiles();
//            List<File> lst = Arrays.asList(arrFiles);
//            for (File file : lst) {
//                s = file.getName();
//                ctx.writeAndFlush(s + "\n");
//            }
//        } else ctx.writeAndFlush("From server: " + s + "\n");
//
//        if (token[0].equals("#GET#FILE")) {
//            File file = currentDir.toPath().resolve(token[1]).toFile();
//            System.out.println(token[1]);
//            System.out.println(file);
//
//        }
//    }

