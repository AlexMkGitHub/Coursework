package com.geekbrains.coursework.cloudstorage;

import java.io.File;
import java.io.Serializable;

public class FileUploadFile implements Serializable {

    private static final long serialVersionUID = 1L;
    private File file;
    private String fileName;
    private int starPos;
    private byte[] bytes;
    private int endPos;
    private String path;
    private String command;

    public File getFile() {
        return file;
    }

    public String getPath() {
        return path;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getStarPos() {
        return starPos;
    }

    public void setStarPos(int starPos) {
        this.starPos = starPos;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}
