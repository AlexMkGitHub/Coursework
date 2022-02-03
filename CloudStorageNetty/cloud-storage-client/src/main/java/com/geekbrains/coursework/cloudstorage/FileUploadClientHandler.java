package com.geekbrains.coursework.cloudstorage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class FileUploadClientHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private volatile int lastLength = 0;
    public RandomAccessFile randomAccessFile;
    private FileUploadFile fileUploadFile;
    private File currentDir = new File("serverDir");
    private final static Logger LOGGER = LoggerFactory.getLogger(FileUploadClientHandler.class);

    public FileUploadClientHandler(FileUploadFile ef) {
        if (ef.getFile().exists()) {
            if (!ef.getFile().isFile()) {
                System.out.println("Not a file :" + ef.getFile());
                return;
            }
        }
        this.fileUploadFile = ef;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LOGGER.info("«Клиент закончил передачу файла channelInactive ()");
    }

    public void channelActive(ChannelHandlerContext ctx) {
        LOGGER.info("«Метод channelActive () выполняется ...»");
        try {
            randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(),
                    "r");
            randomAccessFile.seek(fileUploadFile.getStarPos());
            // lastLength = (int) randomAccessFile.length() / 10;
            lastLength = 1024 * 10;
            byte[] bytes = new byte[lastLength];
            if ((byteRead = randomAccessFile.read(bytes)) != -1) {
                fileUploadFile.setEndPos(byteRead);
                fileUploadFile.setBytes(bytes);
                ctx.writeAndFlush(fileUploadFile);   // Отправить сообщение на сервер
            } else {
            }
            LOGGER.info("Файл channelActive () был прочитан" + byteRead);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("выполнение метода channelActive () заканчивается");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof Integer) {
            start = (Integer) msg;
            if (start != -1) {
                randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
                randomAccessFile.seek(start); // Найдите файл для запуска
                LOGGER.info("«Длина:»" + (randomAccessFile.length() - start));
                int a = (int) (randomAccessFile.length() - start);
                int b = (int) (randomAccessFile.length() / 1024 * 2);
                if (a < lastLength) {
                    lastLength = a;
                }
                LOGGER.info("Длина файла:" + (randomAccessFile.length()) + ",start:" + start + ",a:" + a + ",b:" + b + ",lastLength:" + lastLength);
                byte[] bytes = new byte[lastLength];
                LOGGER.info("Длина байтов равна =" + bytes.length);
                if ((byteRead = randomAccessFile.read(bytes)) != -1 && (randomAccessFile.length() - start) > 0) {
                    LOGGER.info("byteRead = " + byteRead);
                    fileUploadFile.setEndPos(byteRead);
                    fileUploadFile.setBytes(bytes);
                    try {
                        ctx.writeAndFlush(fileUploadFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    randomAccessFile.close();
                    ctx.close();
                    LOGGER.info("Файл был прочитан channelRead () --------" + byteRead);
                }
            }
        }
    }

}