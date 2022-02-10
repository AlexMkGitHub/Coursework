package com.geekbrains.coursework.cloudstorage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    public AnchorPane generalPanel;
    @FXML
    public Button readFile;
    @FXML
    public TextArea textArea;
    @FXML
    public MenuButton menuBtn;
    @FXML
    public Button newDir;
    @FXML
    public Button newFile;
    @FXML
    private Network network;
    @FXML
    public TextField textField;
    @FXML
    public ListView<String> clientView;
    @FXML
    public ListView<String> serverView;
    @FXML
    public Label clientLabel;
    @FXML
    public Label serverLabel;

    private File currentDir;
    private String fileName;
    private File serverDir;
    private Object obj;
    //private FileUploadFile uploadFile;
    private FileUploadFile uploadFile;

    public File getCurrentDir() {
        return currentDir;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //currentDir = new File(System.getProperty("user.home"));
        //serverDir = new File("serverDir");
        // Здесь происходит подключение к серверу
        network = new Network((msg) ->
//                Platform.runLater(() -> serverView.getItems().add((String) msg[0])));
                Platform.runLater(() -> {
                    this.uploadFile = (FileUploadFile) msg[0];
                    //serverView.getItems().clear();
                    if (uploadFile.getCommand() == null) {
                        serverView.getItems().add(uploadFile.getCommand());
                    } else if (uploadFile.getCommand().equals("#CLS")) {
                        serverView.getItems().clear();
                    } else if (uploadFile.getCommand().equals("#VISIBLE")) {
                        generalPanel.setOpacity(1);
                    } else if (uploadFile.getCommand().equals("")) {
                        serverView.getItems().add(uploadFile.getCommand());
                    } else
                        serverView.getItems().add(uploadFile.getCommand());


                }), this);

        this.uploadFile = new FileUploadFile();
        if (currentDir == null) {
            currentDirSelect();
        }
        initClickListener();
        fillCurrentDirFiles();
    }

    private String getClientFilesDetails() {
        File[] files = currentDir.listFiles();
        long size = 0;
        String label;
        if (files != null) {
            label = files.length + " files in current dir. ";
            for (File file : files) {
                size += file.length();
            }
            label += "Summary size: " + size + " bytes.";
        } else {
            label = "Current dir is empty.";
        }
        return label;
    }

    private void initClickListener() {
        clickView(clientView);
        clickView(serverView);
    }

    public void download() {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        if (serverView.getSelectionModel().getSelectedItem() == null) return;
        network.setCurrentDir(currentDir);
        Path currentPath = currentDir.toPath().normalize();
        System.out.println("Имя файла: " + fileName);
        System.out.println("Текущая дериктория: " + currentPath);
        File currentFile = currentPath.resolve(fileName).toFile();
        System.out.println(currentFile.exists());
        if (currentFile.exists()) {
            System.out.println("На компютере ФАЙЛ С ТАКИМ ИМЕНЕМ УЖЕ СУЩЕСТВУЕТ!!!!!!");
            return;
        } else {
            generalPanel.setOpacity(0.50f);
            uploadFile.setCommand("#GET#FILE");
            uploadFile.setFileName(fileName);
            network.sendMessage(uploadFile);
            fillCurrentDirFiles();
        }
    }

    public void upload() {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        if (clientView.getSelectionModel().getSelectedItem() == null) return;
        File currentFile = currentDir.toPath().resolve(fileName).toFile();
        Path currentFilePath = currentDir.toPath().resolve(fileName);
        if (currentFile.isDirectory()) {
            System.out.println("Нельзя загружать папку в облако!");
            return;
        }
        if (fileExistInCloud(fileName)) {
            System.out.println("В облаке ФАЙЛ С ТАКИМ ИМЕНЕМ УЖЕ СУЩЕСТВУЕТ!");
            return;
        }
        network.setCurrentDir(currentDir);
        System.out.println(currentFile);
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
        generalPanel.setOpacity(0.50f);
        network.sendMessage(uploadFile);
        listServerFile();
    }


    public void sendMsgAction() {
        listServerFile();
        textField.clear();
        textField.requestFocus();
    }

    public void fillCurrentDirFiles() {
        Platform.runLater(() -> {
            if (currentDir == null) return;
            clientView.getItems().clear();
            if (fileName != null) {
                Path path = currentDir.toPath().resolve(fileName);
                if (path.normalize().getParent() != null) {
                    clientView.getItems().add("..");
                } else {
                    clientView.getItems().add("root:\\");
                }
            }
//            if (currentDir.isHidden() || !currentDir.canRead() && !currentDir.isDirectory()) {
//                return;
//            }
            clientView.getItems().addAll(currentDir.list());
            clientLabel.setText(getClientFilesDetails());
        });
    }

    private void listLocalDisks() {
        Platform.runLater(() -> {
            clientView.getItems().clear();
            File[] rootDir = File.listRoots();
            for (File file : rootDir) {
                clientView.getItems().add(file.toString());
            }
        });
    }

    private void listDiskMenuButton() {
        Platform.runLater(() -> {
            File[] rootDir = File.listRoots();
            int bt = 0;
            MenuItem[] menuItem = new MenuItem[rootDir.length];
            for (int i = 0, rootDirLength = rootDir.length; i < rootDirLength; i++) {
                File file = rootDir[i];
                menuItem[i] = new MenuItem(file.toString());
                bt += i;
                menuBtn.getItems().add(menuItem[i]);
                menuItem[i].setOnAction((e) -> {
                    clientView.getItems().clear();
                    clientView.getItems().add("root:\\");
                    clientView.getItems().addAll(file.list());
                    currentDir = new File(file.toString());
                });
            }
        });

    }

    private void clickView(ListView<String> currentWindow) {
        currentWindow.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                if (currentDir == null) return;
                fileName = currentWindow.getSelectionModel().getSelectedItem();
                if (fileName.equals("root:\\")) {
                    listLocalDisks();
                    return;
                }
                System.out.println("Выбран файл: " + fileName);
                Path path = currentDir.toPath().resolve(fileName);
                System.out.println(fileName.equals("..") && path.normalize().getParent() == null);
                if (Files.isDirectory(path)) {
                    currentDir = path.normalize().toFile();
                    network.setCurrentDir(currentDir);
                    System.out.println("Дериктория по умолчанию: " + currentDir);
                    fillCurrentDirFiles();
                    textArea.setVisible(false);
                    serverView.setVisible(true);
                    listServerFile();
                }
                System.out.println("НОРМАЛИЗОВАНАЯ ДЕРИКТОРИЯ: " + path.normalize());
                System.out.println("РОДИТЕЛЬСКЯ ДЕРИКТОРИЯ: " + path.normalize().getParent());
                System.out.println("РУТ ДЕРИКТОРИЯ: " + path.getRoot());
            }
        });
    }

    private void currentDirSelect() {
        Platform.runLater(() -> {
            listLocalDisks();
            listDiskMenuButton();
            if (clientView.getItems().isEmpty()) {
                File currentDirNew = new File("C:/");
                clientView.getSelectionModel().select(String.valueOf(currentDirNew));
                Path path = currentDirNew.toPath().resolve(String.valueOf(currentDirNew));
                currentDir = path.normalize().toFile();
            }
            System.out.println(currentDir);
        });
    }

    private void createDirectoryInCloud(String dirName) throws IOException {
        if (fileExistInCloud(dirName)) {
            System.out.println("Папка с таким именем в облаке уже существует!");
            return;
        } else {
            uploadFile = new FileUploadFile();
            uploadFile.setCommand("#NEW#DIR");
            uploadFile.setFileName(dirName);
            network.sendMessage(uploadFile);
            System.out.println("В облаке создана папка " + dirName);
        }
    }

    private void createEmtyFileInCloud(String fileName) {
        if (fileExistInCloud(fileName)) {
            System.out.println("Файл с таким именем в облаке уже существует!");
            return;
        } else {
            uploadFile = new FileUploadFile();
            uploadFile.setCommand("#NEW#FILE");
            uploadFile.setFileName(fileName);
            network.sendMessage(uploadFile);
            System.out.println("В облаке создан файл " + fileName);
        }

    }

    private boolean fileExistInCloud(String objName) {
        boolean objExist = false;
        for (int i = 0; i < serverView.getItems().size(); i++) {
            String name = serverView.getItems().get(i);
            if (name.equals(objName)) {
                objExist = true;
                break;
            }
        }
        return objExist;
    }

    public void read() {
        if (clientView.getSelectionModel().getSelectedItem() == null) return;
        fileName = clientView.getSelectionModel().getSelectedItem();
        File currentFile = currentDir.toPath().resolve(fileName).toFile();
        Path currentFilePath = currentDir.toPath().resolve(fileName);
        System.out.println(Files.exists(currentFile.toPath()));
        System.out.println(currentFilePath);
        if (Files.exists(currentFile.toPath())) {
            if (Files.isRegularFile(currentFile.toPath())) {
                byte[] bytes = new byte[0];
                try {
                    bytes = Files.readAllBytes(currentFile.toPath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                String fileText = new String(bytes, StandardCharsets.UTF_8);
                serverView.setVisible(false);
                serverView.getItems().clear();
                //serverView.getItems().add(fileText);
                textArea.setText(fileText);
                textArea.setWrapText(true);
                textArea.setVisible(true);

            } else {
                System.out.println("НЕВОЗМОЖНО ПРОЧИТАТЬ ФАЙЛ!");
            }
        } else {
            System.out.println("ТАКОГО ФАЙЛА НЕ СУЩЕСТВУЕТ!");
        }
    }

    private void listServerFile() {
        uploadFile = new FileUploadFile();
        uploadFile.setCommand("#LIST");
        network.sendMessage(uploadFile);
    }

    public void createNewDir(ActionEvent actionEvent) throws IOException {
        if (textField.getText().isEmpty()) {
            System.out.println("Введите название создаваемой папки!");
            return;
        }
        String dirName = textField.getText();
        createDirectoryInCloud(dirName);
    }

    public void createNewFile(ActionEvent actionEvent) {
        if (textField.getText().isEmpty()) {
            System.out.println("Введите название создаваемого файла!");
            return;
        }
        String fileName = textField.getText();
        createEmtyFileInCloud(fileName);
    }
}