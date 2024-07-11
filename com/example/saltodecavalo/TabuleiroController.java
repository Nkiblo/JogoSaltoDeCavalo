package com.example.saltodecavalo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.Node;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.effect.DropShadow;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class TabuleiroController {
    @FXML
    private GridPane gridPane;
    @FXML
    private Text turnText;
    @FXML
    private Text playerText;
    @FXML
    private Text jogadaText;

    private Stage primaryStage;

    // Define a stage principal
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    // Define o botão sair
    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    // Define o botão recomeçar
    private void handleRestart() {
        try {
            // Carrega o ficheiro FXML novamente para reiniciar a aplicação
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tabuleiro.fxml"));
            Parent root = loader.load();

            // Define a referência da stage principal no novo controlador
            TabuleiroController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            // Cria uma nova scene e define-a na stage principal
            Scene newScene = new Scene(root);

            // Aplica a folha de estilo CSS
            newScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            primaryStage.setScene(newScene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
