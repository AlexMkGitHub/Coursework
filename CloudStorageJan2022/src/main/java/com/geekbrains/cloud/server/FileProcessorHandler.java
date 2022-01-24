package com.geekbrains.cloud.server;

import com.geekbrains.cloud.utils.SenderUtils;

import java.io.*;
import java.net.Socket;

public class FileProcessorHandler implements Runnable {

    private File currentDir;
    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buf;
    private static final int SIZE = 256;

    public FileProcessorHandler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        buf = new byte[SIZE];
        currentDir = new File("serverDir");
        SenderUtils.sendFileListToOutputStream(os, currentDir);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = is.readUTF();
                System.out.println("Recived server command: " + command);
                if (command.equals("#SEND#FILE")) {
                    SenderUtils.getFileFromInputStream(is, currentDir);
                    //server state update
                SenderUtils.sendFileListToOutputStream(os, currentDir);
                }
                if (command.equals("#GET#FILE")) {
                    String fileName = is.readUTF();
                    File file = currentDir.toPath().resolve(fileName).toFile();
                    SenderUtils.loadFileOutputStream(os, file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
