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

    // Lida com o clique na célula
    private void handleCellClick(int row, int col) {
        if (isMyTurn) {
            Jogador currentPlayer = isBlueTurn ? jogador1 : jogador2;
            Arc arcToMove = currentPlayer.getArc();

            // Verifica se o jogador atual tem movimentos válidos
            if (hasValidMoves(0)) {
                if (isValidMove(arcToMove, row, col)) {
                    output.println(row + ":" + col);
                    isMyTurn = false; // Termina o turno somente se o movimento for válido e enviado ao servidor
                } else {
                    jogadaText.setText("Jogada inválida");
                }
            } else {
                jogadaText.setText("Você não tem movimentos válidos!");
            }
        }
    }

    // Verifica se o movimento é válido
    private boolean isValidMove(Arc arc, int newRow, int newCol) {
        StackPane currentCell = getCellForArc(arc);
        if (currentCell == null) {
            return false;
        }

        int currentRow = GridPane.getRowIndex(currentCell);
        int currentCol = GridPane.getColumnIndex(currentCell);

        // Verifica se o movimento é um dos movimentos válidos do cavalo
        for (int[] move : validMoves) {
            int dx = move[0];
            int dy = move[1];
            if (currentRow + dx == newRow && currentCol + dy == newCol) {
                StackPane nextCell = getCellFromGridPane(gridPane, newRow, newCol);
                if (nextCell != null && nextCell.getChildren().isEmpty()) {
                    return true; // Movimento válido se a célula estiver vazia
                }
            }
        }

        return false; // Movimento inválido se não corresponder a nenhum dos movimentos válidos do cavalo ou a célula não estiver vazia
    }

    // Verifica se o jogador tem movimentos válidos
    boolean hasValidMoves(int i) {
        Jogador currentPlayer = isBlueTurn ? jogador1 : jogador2;
        Arc arcToCheck = currentPlayer.getArc();
        StackPane currentCell = getCellForArc(arcToCheck);
        if (currentCell == null) {
            return false;
        }

        int currentRow = GridPane.getRowIndex(currentCell);
        int currentCol = GridPane.getColumnIndex(currentCell);

        // Verifica se algum dos movimentos do cavalo é válido
        for (int[] move : validMoves) {
            int newRow = currentRow + move[0];
            int newCol = currentCol + move[1];

            if (isValidMove(arcToCheck, newRow, newCol)) {
                return true;
            }
        }

        return false;
    }

    // Obtém a célula da GridPane
    private StackPane getCellFromGridPane(GridPane gridPane, int row, int col) {
        ObservableList<Node> children = gridPane.getChildren();
        for (Node node : children) {
            if (GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == row &&
                    GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == col) {
                if (node instanceof StackPane) {
                    return (StackPane) node;
                } else {
                    System.err.println("Erro: O nó encontrado não é um StackPane na posição " + row + ", " + col);
                    return null;
                }
            }
        }
        System.err.println("Erro: Nenhum StackPane encontrado na posição " + row + ", " + col);
        return null;
    }

    // Obtém a célula para o arco
    private StackPane getCellForArc(Arc arc) {
        for (Node node : gridPane.getChildren()) {
            if (node instanceof StackPane && ((StackPane) node).getChildren().contains(arc)) {
                return (StackPane) node;
            }
        }
        return null;
    }

    // Move o arco para a posição especificada
    private void moveArcToPosition(Arc arc, int row, int col) {
        StackPane cell = getCellFromGridPane(gridPane, row, col);
        if (cell != null) {
            GridPane.setColumnIndex(arc, col);
            GridPane.setRowIndex(arc, row);
            cell.getChildren().clear();
            cell.getChildren().add(arc);
            arc.setEffect(new DropShadow(10, Color.GRAY));
        } else {
            System.err.println("Erro ao mover Arc: não foi possível encontrar o StackPane na posição " + row + ", " + col);
        }
    }



}




