package com.geekbrains.coursework.cloudstorage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {
    @FXML
    public Button serverStart;
    @FXML
    public Button serverStop;
    @FXML
    public TextArea serverInfo;

    private final NettyEchoServer nettyEchoServer = new NettyEchoServer(this);


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void startServer(ActionEvent actionEvent) {
        nettyEchoServer.start();
    }

    public void stopServer(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            nettyEchoServer.auth.shutdownGracefully();
            nettyEchoServer.worker.shutdownGracefully();
        });


    }

}
