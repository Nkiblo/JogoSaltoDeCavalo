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

    // Método principal que inicia a aplicação JavaFX
    @Override
    public void start(Stage primaryStage) {
        try {
            // Carrega o layout do FXML e obtém o controlador associado
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projeto/tabuleiro.fxml"));
            Parent root = loader.load();
            tabuleiroController = loader.getController();
            tabuleiroController.setPrimaryStage(primaryStage);

            // Cria a cena com o layout carregado
            Scene scene = new Scene(root);

            // Adiciona a folha de estilos CSS à cena
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            primaryStage.setTitle("Jogo do Salto do Cavalo");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Inicializa a conexão com o servidor
            initializeConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para inicializar a conexão com o servidor
    private void initializeConnection() {
        try {
            // Cria um socket para se conectar ao servidor
            Socket socket = new Socket("192.168.1.177", 12345);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Define a conexão no controlador do tabuleiro
            tabuleiroController.setConnection(socket, input, output);

            System.out.println("Conectado ao servidor");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao conectar ao servidor");
        }
    }

    // Método estático para obter o controlador do tabuleiro
    public static TabuleiroController getTabuleiroController() {
        return tabuleiroController;
    }

    // Método principal para lançar a aplicação
    public static void main(String[] args) {
        launch(args);
    }
}