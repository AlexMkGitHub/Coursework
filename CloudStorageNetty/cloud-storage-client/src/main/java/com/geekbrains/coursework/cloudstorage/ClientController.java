package com.geekbrains.coursework.cloudstorage;

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

    private File currentDir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Здесь происходит подключение к серверу
        network = new Network();
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
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String fileName = clientView.getSelectionModel().getSelectedItem();
                System.out.println("Выбран файл: " + fileName);
                Path path = currentDir.toPath().resolve(fileName);
                if (Files.isDirectory(path)) {
                    currentDir = path.toFile();
                }
            }
        });
    }

    public void download() throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();

    }

    public void upload() throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        File currentFile = currentDir.toPath().resolve(fileName).toFile();

    }

    public void sendMsgAction(ActionEvent actionEvent) {
        network.sendMesage(textField.getText());
        textField.clear();
        textField.requestFocus();
    }
}