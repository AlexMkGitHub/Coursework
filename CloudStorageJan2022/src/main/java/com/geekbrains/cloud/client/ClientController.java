package com.geekbrains.cloud.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    public ListView<String> listView;
    @FXML
    public ListView<String> listViewCloud;
    @FXML
    public TextField textField;


    private DataInputStream is;
    private DataOutputStream os;

    private File currentDir;
    private File cloudDir;

    private byte[] buf;

    public void sendMessage(ActionEvent actionEvent) throws IOException {
        String fileName = textField.getText();
        File currentFile = currentDir.toPath().resolve(fileName).toFile();
        os.writeUTF("#SEND#FILE#");
        os.writeUTF(fileName);
        os.writeLong(currentFile.length());
        try (FileInputStream is = new FileInputStream(currentFile)) {
            while (true) {
                int read = is.read(buf);
                if (read == -1) {
                    break;
                }
                os.write(buf, 0, read);
            }
        }
        os.flush();
        textField.clear();
        listViewCloud.getItems().clear();
        listViewCloud.getItems().addAll(cloudDir.list());
//        listView.getItems().add(message);
    }

    private void read() {
        try {
            while (true) {
                String message = is.readUTF();
                Platform.runLater(() -> textField.setText(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillCurrentDirFilesClient() {
        listView.getItems().clear();
        listView.getItems().add("..");
        listView.getItems().addAll(currentDir.list());
    }

    private void fillCurrentDirFilesServer() {
        listViewCloud.getItems().clear();
        listViewCloud.getItems().addAll(cloudDir.list());
    }

    private void initClickListener() {
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String fileName = listView.getSelectionModel().getSelectedItem();
                System.out.println("Выбран файл: " + fileName);
                Path path = currentDir.toPath().resolve(fileName);
                if (Files.isDirectory(path)) {
                    currentDir = path.toFile();
                    fillCurrentDirFilesClient();
                    fillCurrentDirFilesServer();
                    textField.clear();
                } else {
                    textField.setText(fileName);
                    textField.requestFocus();
                }
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            buf = new byte[256];
            currentDir = new File(System.getProperty("user.home"));
            cloudDir = new File("serverDir");
            initClickListener();
            fillCurrentDirFilesClient();
            fillCurrentDirFilesServer();
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}