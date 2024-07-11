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


    private BufferedReader input;
    private PrintWriter output;
    private Jogador jogador1;
    private Jogador jogador2;
    private Circle[][] markingBalls;
    private boolean isBlueTurn;

    private boolean isMyTurn = false;
    private int lastMoveRowPlayer1 = -1;
    private int lastMoveColPlayer1 = -1;
    private int lastMoveRowPlayer2 = -1;
    private int lastMoveColPlayer2 = -1;

    // Movimentos válidos de um cavalo no xadrez (em forma de "L")
    private final int[][] validMoves = {
            { 2, 1 }, { 1, 2 }, { -1, 2 }, { -2, 1 },
            { -2, -1 }, { -1, -2 }, { 1, -2 }, { 2, -1 }
    };


    // Método de inicialização
    public void initialize() {
        jogador1 = new Jogador(Color.BLUE, "Jogador 1 (Azul)");
        jogador2 = new Jogador(Color.RED, "Jogador 2 (Vermelho)");

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                final int finalRow = row;
                final int finalCol = col;
                StackPane cell = new StackPane();
                cell.setPrefSize(100, 100);
                cell.getStyleClass().add("grid-cell");
                cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> handleCellClick(finalRow, finalCol));
                gridPane.add(cell, col, row);
            }
        }

        markingBalls = new Circle[5][5];

        gridPane.getChildren().addAll(jogador1.getArc(), jogador2.getArc());

        moveArcToPosition(jogador1.getArc(), 0, 0);
        markStartingPosition(0, 0, jogador1.getColor());

        moveArcToPosition(jogador2.getArc(), 4, 4);
        markStartingPosition(4, 4, jogador2.getColor());

        isBlueTurn = true;
        updateTurnText();
    }

    // Define a conexão com o servidor
    public void setConnection(Socket socket, BufferedReader input, PrintWriter output) {
        this.input = input;
        this.output = output;

        new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = input.readLine()) != null) {
                    final String message = serverMessage;
                    Platform.runLater(() -> processServerMessage(message));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Processa a mensagem recebida do servidor
    private void processServerMessage(String message) {
        Platform.runLater(() -> {
            System.out.println("Received message: " + message); // Log da mensagem recebida
            try {
                if (message.startsWith("Jogador")) {
                    jogadaText.setText(message);
                } else if (message.equals("SUA_VEZ")) {
                    isMyTurn = true;
                    turnText.setText("Sua vez de jogar");
                    // Verifica se o jogador atual tem movimentos válidos
                    if (!hasValidMoves(0)) {
                        // O outro jogador vence se o jogador atual não tiver movimentos válidos
                        declareWinner(isBlueTurn); // Passa a flag do turno do outro jogador
                    }
                } else if (message.startsWith("MOVIMENTO:")) {
                    String[] parts = message.substring(10).split(":");
                    if (parts.length == 2) {
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);
                        updateBoard(row, col);
                        isMyTurn = false; // Termina o turno deste jogador
                    } else {
                        System.err.println("Erro: Formato de mensagem MOVIMENTO inválido: " + message);
                    }
                } else if (message.startsWith("ATUALIZAR:")) {
                    String[] parts = message.substring(10).split(":");
                    if (parts.length == 2) {
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);
                        updateBoard(row, col);
                        isMyTurn = true;
                        turnText.setText("Sua vez de jogar");
                        // Verifica se o jogador atual tem movimentos válidos
                        if (!hasValidMoves(0)) {
                            // O outro jogador vence se o jogador atual não tiver movimentos válidos
                            declareWinner(!isBlueTurn); // Passa a flag do turno do outro jogador
                        }
                    } else {
                        System.err.println("Erro: Formato de mensagem ATUALIZAR inválido: " + message);
                    }
                } else if (message.startsWith("Você é o jogador")) {
                    System.out.println(message);
                } else {
                    System.err.println("Erro: Mensagem do servidor desconhecida: " + message);
                }
            } catch (NumberFormatException e) {
                System.err.println("Erro: Número inválido na mensagem: " + message);
            }
        });
    }