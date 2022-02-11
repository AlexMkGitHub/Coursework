package com.geekbrains.coursework.cloudstorage;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.image.Image;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;


public class FileMetods {

    private ClientController clientController;
    private Network network;
    private final HashMap<String, Image> mapOfFileExtToSmallIcon = new HashMap<String, Image>();
    private FileUploadFile uploadFile;


    public FileMetods(ClientController clientController, Network network) {
        this.clientController = clientController;
        this.network = network;

    }

    protected String getClientFilesDetails() {
        File[] files = clientController.currentDir.listFiles();
        long size = 0;
        String label;
        if (files != null) {
            label = files.length + " файлов в папке. ";
            for (File file : files) {
                size += file.length();
            }
            label += "Общий размер: " + (size / (1024 * 2)) + " Mb.";
        } else {
            label = "Текущая папка пустая.";
        }
        return label;
    }

    protected void initClickListener() {
        clickView(clientController.clientView);
        clickView(clientController.serverView);
    }

    protected void download() {
        String fileName = clientController.serverView.getSelectionModel().getSelectedItem();
        if (clientController.serverView.getSelectionModel().getSelectedItem() == null) return;
        network.setCurrentDir(clientController.currentDir);
        Path currentPath = clientController.currentDir.toPath().normalize();
        File currentFile = currentPath.resolve(fileName).toFile();
        if (currentFile.exists()) {
            clientController.clientLabel.setText("На компьютере ФАЙЛ С ТАКИМ ИМЕНЕМ УЖЕ СУЩЕСТВУЕТ!!!!!!");
            return;
        } else {
            clientController.generalPanel.setOpacity(0.50f);
            uploadFile.setCommand("#GET#FILE");
            uploadFile.setFileName(fileName);
            network.sendMessage(uploadFile);
            fillCurrentDirFiles();
        }
    }

    protected void upload() {
        String fileName = clientController.clientView.getSelectionModel().getSelectedItem();
        if (clientController.clientView.getSelectionModel().getSelectedItem() == null) return;
        File currentFile = clientController.currentDir.toPath().resolve(fileName).toFile();
        Path currentFilePath = clientController.currentDir.toPath().resolve(fileName);
        if (currentFile.isDirectory()) {
            clientController.clientLabel.setText("Нельзя загружать папку в облако!");
            return;
        }
        if (fileExistInCloud(fileName)) {
            clientController.clientLabel.setText("В облаке ФАЙЛ С ТАКИМ ИМЕНЕМ УЖЕ СУЩЕСТВУЕТ!");
            return;
        }
        network.setCurrentDir(clientController.currentDir);
        uploadFile.setCommand("#ADD#FILE");
        uploadFile.setFile(currentFile);
        String currentFileName = currentFile.getName();
        uploadFile.setFileName(currentFileName);
        uploadFile.setStarPos(0);
        try {
            uploadFile.setBytes(Files.readAllBytes(currentFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientController.generalPanel.setOpacity(0.50f);
        network.sendMessage(uploadFile);
        listServerFile();
    }

    protected void sendMsgAction() {
        listServerFile();
        clientController.textField.clear();
        clientController.textField.requestFocus();
    }

    protected void fillCurrentDirFiles() {

        Platform.runLater(() -> {
            if (clientController.currentDir == null) return;
            clientController.clientView.getItems().clear();
            if (clientController.fileName != null) {
                Path path = clientController.currentDir.toPath().resolve(clientController.fileName);
                if (path.normalize().getParent() != null) {
                    clientController.clientView.getItems().add("..");
                } else {
                    clientController.clientView.getItems().add("root:\\");
                }
            }

            Path p = clientController.currentDir.toPath();
            if (Files.notExists(p)) clientController.clientLabel.setText("ФАЙЛ ОТСУТСТВУТЕТ!");
            if (!Files.isWritable(p) && !Files.isReadable(p)) {
                clientController.clientLabel.setText("НЕТ ДОСТУПА К ПАПКЕ ИЛИ ФАЙЛУ");
                return;
            }

            clientController.clientView.getItems().addAll(clientController.currentDir.list());
            clientController.clientLabel.setText(getClientFilesDetails());
        });
    }

    protected void listLocalDisks() {
        Platform.runLater(() -> {
            clientController.clientView.getItems().clear();
            File[] rootDir = File.listRoots();
            for (File file : rootDir) {
                clientController.clientView.getItems().add(file.toString());
            }
            clientController.clientLabel.setText("Здесь информация...");
        });
    }

    protected void listDiskMenuButton() {
        Platform.runLater(() -> {
            File[] rootDir = File.listRoots();
            int bt = 0;
            MenuItem[] menuItem = new MenuItem[rootDir.length];
            for (int i = 0, rootDirLength = rootDir.length; i < rootDirLength; i++) {
                File file = rootDir[i];
                menuItem[i] = new MenuItem(file.toString());
                clientController.fileName = file.toString();
                clientController.clientLabel.setText("Имя файла: " + clientController.fileName);
                bt += i;
                clientController.menuBtn.getItems().add(menuItem[i]);
                menuItem[i].setOnAction((e) -> {
                    clientController.clientView.getItems().clear();
                    clientController.clientView.getItems().add("root:\\");
                    clientController.clientView.getItems().addAll(file.list());
                    clientController.currentDir = new File(file.toString());
                });
            }
        });

    }

    protected void clickView(ListView<String> currentWindow) {

        currentWindow.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                if (clientController.clientView.getSelectionModel().getSelectedItem() == null) return;
                clientController.fileName = clientController.clientView.getSelectionModel().getSelectedItem();
                if (clientController.fileName.equals("root:\\")) return;
                File currentFile = clientController.currentDir.toPath().resolve(clientController.fileName).toFile();
                if (currentFile.canRead()) {
                    long size = currentFile.length();
                    double sizeFile;
                    if (size - 1024 <= 0) {
                        clientController.clientLabel.setText("Размер файла на локальном диске: " + size + " byte.");
                        return;
                    }
                    if (size - Math.pow(1024, 2) <= 0) {
                        sizeFile = size / 1024.0;
                        clientController.clientLabel.setText("Размер файла на локальном диске: " + Math.round(sizeFile) + " Kb.");
                        return;
                    }
                    if (size - Math.pow(1024, 3) <= 0) {
                        sizeFile = size / Math.pow(1024, 2);
                        clientController.clientLabel.setText("Размер файла на локальном диске: " + Math.round(sizeFile) + " Mb.");
                        return;
                    }
                    if (size - Math.pow(1024, 4) <= 0) {
                        sizeFile = size / Math.pow(1024, 3);
                        clientController.clientLabel.setText("Размер файла на локальном диске: " + Math.round(sizeFile) + " Gb.");
                        return;
                    }
                    if (size - Math.pow(1024, 5) <= 0) {
                        sizeFile = size / Math.pow(1024, 4);
                        clientController.clientLabel.setText("Размер файла на локальном диске: " + Math.round(sizeFile) + " Tb.");
                    } else clientController.clientLabel.setText("Доступ к файлу запрещен!");
                }
            }

            if (e.getClickCount() == 2) {
                if (clientController.currentDir == null) return;
                clientController.fileName = currentWindow.getSelectionModel().getSelectedItem();
                if (clientController.fileName == null) {
                    return;
                }
                if (clientController.fileName.equals("root:\\")) {
                    listLocalDisks();
                    return;
                }
                clientController.clientLabel.setText("Выбран файл: " + clientController.fileName);
                Path path = clientController.currentDir.toPath().resolve(clientController.fileName);
                if (Files.isDirectory(path)) {
                    clientController.currentDir = path.normalize().toFile();
                    network.setCurrentDir(clientController.currentDir);
                    clientController.clientLabel.setText("Дериктория по умолчанию: " + clientController.currentDir);
                    fillCurrentDirFiles();
                    clientController.textArea.setVisible(false);
                    clientController.serverView.setVisible(true);
                    listServerFile();
                }
            }
        });
    }

    protected void currentDirSelect() {
        Platform.runLater(() -> {
            listLocalDisks();
            listDiskMenuButton();
            if (clientController.clientView.getItems().isEmpty()) {
                File currentDirNew = new File("C:/");
                clientController.clientView.getSelectionModel().select(String.valueOf(currentDirNew));
                Path path = currentDirNew.toPath().resolve(String.valueOf(currentDirNew));
                clientController.currentDir = path.normalize().toFile();
            }
        });
    }

    protected void createDirectoryInCloud(String dirName) throws IOException {
        if (fileExistInCloud(dirName)) {
            clientController.clientLabel.setText("Папка с таким именем в облаке уже существует!");
            return;
        } else {
            uploadFile = new FileUploadFile();
            uploadFile.setCommand("#NEW#DIR");
            uploadFile.setFileName(dirName);
            network.sendMessage(uploadFile);
            clientController.clientLabel.setText("В облаке создана папка " + dirName);
        }
    }

    protected void createEmtyFileInCloud(String fileName) {
        if (fileExistInCloud(fileName)) {
            clientController.clientLabel.setText("Файл с таким именем в облаке уже существует!");
            return;
        } else {
            uploadFile = new FileUploadFile();
            uploadFile.setCommand("#NEW#FILE");
            uploadFile.setFileName(fileName);
            network.sendMessage(uploadFile);
            clientController.clientLabel.setText("В облаке создан файл " + fileName);
        }

    }

    protected boolean fileExistInCloud(String objName) {
        boolean objExist = false;
        for (int i = 0; i < clientController.serverView.getItems().size(); i++) {
            String name = clientController.serverView.getItems().get(i);
            if (name.equals(objName)) {
                objExist = true;
                break;
            }
        }
        return objExist;
    }

    protected void read() {
        if (clientController.clientView.getSelectionModel().getSelectedItem() == null) return;
        clientController.fileName = clientController.clientView.getSelectionModel().getSelectedItem();
        File currentFile = clientController.currentDir.toPath().resolve(clientController.fileName).toFile();
        Path currentFilePath = clientController.currentDir.toPath().resolve(clientController.fileName);

        if (Files.exists(currentFile.toPath())) {
            if (Files.isRegularFile(currentFile.toPath()) && !getFileExtension(clientController.fileName).equals("mp4")) {
                byte[] bytes = new byte[0];
                try {
                    bytes = Files.readAllBytes(currentFile.toPath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                String fileText = new String(bytes, StandardCharsets.UTF_8);
                clientController.serverView.setVisible(false);
                clientController.serverView.getItems().clear();
                clientController.textArea.setText(fileText);
                clientController.textArea.setWrapText(true);
                clientController.textArea.setVisible(true);

            } else {
                clientController.clientLabel.setText("НЕВОЗМОЖНО ПРОЧИТАТЬ ФАЙЛ!");
            }
        } else {
            clientController.clientLabel.setText("ТАКОГО ФАЙЛА НЕ СУЩЕСТВУЕТ!");
        }
    }

    protected void listServerFile() {
        uploadFile = new FileUploadFile();
        uploadFile.setCommand("#LIST");
        network.sendMessage(uploadFile);
    }

    protected void createNewDir() throws IOException {
        if (clientController.textField.getText().isEmpty()) {
            clientController.clientLabel.setText("Введите название создаваемой папки!");
            return;
        }
        String dirName = clientController.textField.getText();
        createDirectoryInCloud(dirName);
    }

    protected void createNewFile() {
        if (clientController.textField.getText().isEmpty()) {
            clientController.clientLabel.setText("Введите название создаваемого файла!");
            return;
        }
        String fileName = clientController.textField.getText();
        createEmtyFileInCloud(fileName);
    }

    protected String getFileExtension(String fname) {
        String ext;
        int p = fname.lastIndexOf('.');
        if (p > 0) {
            ext = fname.substring(p);
            return ext.toLowerCase();
        }
        return ext = ".dirrrrrrrr1";
    }

    protected Icon getJSwingIconFromFileSystem(File file) {
        FileSystemView view = FileSystemView.getFileSystemView();
        return view.getSystemIcon(file);
    }

    protected Image getFileIcon(String fname) {
        String ext = getFileExtension(fname);
        Image fileIcon = mapOfFileExtToSmallIcon.get(ext);
        if (fileIcon == null) {
            Icon jswingIcon = null;
            File file = new File(fname);
            if (file.exists()) {
                jswingIcon = getJSwingIconFromFileSystem(file);
            } else {
                File tempFile = null;
                try {
                    tempFile = File.createTempFile("icon", ext);
                    jswingIcon = getJSwingIconFromFileSystem(tempFile);
                } catch (IOException ignored) {
                } finally {
                    if (tempFile != null) tempFile.delete();
                }
            }

            if (jswingIcon != null) {
                fileIcon = jswingIconToImage(jswingIcon);
                mapOfFileExtToSmallIcon.put(ext, fileIcon);
            }
            if (ext.equals(".dirrrrrrrr1")) {
                fileIcon = new Image(String.valueOf(getClass().getResource("/img/folder.png")));
                mapOfFileExtToSmallIcon.put(ext, fileIcon);
            }
            return fileIcon;
        }
        return fileIcon;
    }

    protected Image jswingIconToImage(Icon jswingIcon) {
        BufferedImage bufferedImage = new BufferedImage(jswingIcon.getIconWidth(), jswingIcon.getIconHeight(),
                BufferedImage.TYPE_4BYTE_ABGR_PRE);

        jswingIcon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

}