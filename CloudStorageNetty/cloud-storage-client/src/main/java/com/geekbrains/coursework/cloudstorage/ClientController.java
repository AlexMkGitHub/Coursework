package com.geekbrains.coursework.cloudstorage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
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

    private File userDir;
    private File serverDir;
    private Object obj;
    private String fileName;
    private FileUploadFile uploadFile;
    private FileUploadClient fileUploadClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDir = new File(System.getProperty("user.home"));
        serverDir = new File("serverDir");
        // Здесь происходит подключение к серверу
        network = new Network((msg) ->
//                Platform.runLater(() -> serverView.getItems().add((String) msg[0])));
                Platform.runLater(() -> {
                    //serverView.getItems().clear();
                    if (msg[0] instanceof String) {
                        if (msg[0].toString().equals("cls")) {
                            serverView.getItems().clear();
                        } else serverView.getItems().add((String) msg[0]);
                    }
                }));


//        if(EchoStringHandler.msg!=null){
//            serverView.getItems().add(EchoStringHandler.msg);
//        }
        this.uploadFile = new FileUploadFile();
        this.fileUploadClient = new FileUploadClient();
        fillCurrentDirFiles();
        initClickListener();
    }


    private String getClientFilesDetails() {
        File[] files = userDir.listFiles();
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
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                fileName = clientView.getSelectionModel().getSelectedItem();
                System.out.println("Выбран файл: " + fileName);
                Path path = userDir.toPath().resolve(fileName);
                if (Files.isDirectory(path)) {
                    userDir = path.toFile();
                    fillCurrentDirFiles();
                }
            }
        });
    }


    public void download() throws IOException {
        fileName = serverView.getSelectionModel().getSelectedItem();
        network.sendMesage("#GET#FILE " + fileName);
    }

    public void upload() throws Exception {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        File currentFile = userDir.toPath().resolve(fileName).toFile();
        System.out.println(currentFile);
        uploadFile.setFile(currentFile);
        String fileMd5 = currentFile.getName();
        uploadFile.setFile_md5(fileMd5);
        uploadFile.setStarPos(0);
        fileUploadClient.connect(8189, "localhost", uploadFile);
    }

    public void sendMsgAction(ActionEvent actionEvent) {
        network.sendMesage(textField.getText());
        textField.clear();
        textField.requestFocus();
    }

    private void fillCurrentDirFiles() {
        clientView.getItems().clear();
        clientView.getItems().add("..");
        clientView.getItems().addAll(userDir.list());
        clientLabel.setText(getClientFilesDetails());
    }


}