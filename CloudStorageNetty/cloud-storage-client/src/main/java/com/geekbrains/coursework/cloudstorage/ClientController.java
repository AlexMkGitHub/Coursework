package com.geekbrains.coursework.cloudstorage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

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
    public VBox generalPanel;
    @FXML
    public TextField textField;
    @FXML
    public ListView<String> clientView;
    @FXML
    public ListView<String> serverView;
    @FXML
    public Label clientLabel;

    protected FileMetods fileMetods;
    protected Network network;
    protected File currentDir;
    protected String fileName;
    protected FileUploadFile uploadFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientView.setCellFactory(list -> new AttachmentListCell());
        serverView.setCellFactory(list -> new AttachmentListCell());

        // Здесь происходит подключение к серверу
        network = new Network((msg) ->
                Platform.runLater(() -> {
                    this.uploadFile = (FileUploadFile) msg[0];

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


        this.fileMetods = new FileMetods(this, network);
        this.uploadFile = new FileUploadFile();
        if (currentDir == null) {
            fileMetods.currentDirSelect();
        }
        fileMetods.initClickListener();
        fileMetods.fillCurrentDirFiles();
    }


    public void sendMsgAction(ActionEvent actionEvent) {
        fileMetods.sendMsgAction();
    }

    public void upload(ActionEvent actionEvent) {
        fileMetods.upload();
    }

    public void read(ActionEvent actionEvent) {
        fileMetods.read();
    }

    public void createNewDir(ActionEvent actionEvent) {
        try {
            fileMetods.createNewDir();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createNewFile(ActionEvent actionEvent) {
        fileMetods.createNewFile();
    }

    public void download(ActionEvent actionEvent) {
        fileMetods.download();
    }

    protected class AttachmentListCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                setText(null);
            } else {
                Image fxImage = fileMetods.getFileIcon(item);
                ImageView imageView = new ImageView(fxImage);
                setGraphic(imageView);
                setText(item);
            }
        }
    }
}