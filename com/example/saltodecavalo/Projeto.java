package com.example.saltodecavalo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Projeto extends Application {

    private static TabuleiroController tabuleiroController;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tabuleiro.fxml"));
            Parent root = loader.load();
            tabuleiroController = loader.getController();
            tabuleiroController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);

            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            primaryStage.setTitle("Jogo do Salto do Cavalo");
            primaryStage.setScene(scene);
            primaryStage.show();

            initializeConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeConnection() {
        try {
            Socket socket = new Socket("192.168.1.177", 12345);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            tabuleiroController.setConnection(socket, input, output);

            System.out.println("Conectado ao servidor");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao conectar ao servidor");
        }
    }

    public static TabuleiroController getTabuleiroController() {
        return tabuleiroController;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
