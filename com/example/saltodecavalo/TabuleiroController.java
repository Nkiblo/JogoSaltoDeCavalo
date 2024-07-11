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
    private Arc currentArcHovered;
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
                cell.setOnMouseEntered(event -> handleCellHover(finalRow, finalCol)); // Event handler for hover
                cell.setOnMouseExited(event -> handleCellExit(finalRow, finalCol)); // Event handler for exit hover
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


    private void handleCellHover(int row, int col) {
        if (isMyTurn) {
            Jogador currentPlayer = isBlueTurn ? jogador1 : jogador2;
            Arc arcToMove = currentPlayer.getArc();

            StackPane currentCell = getCellForArc(arcToMove);
            if (currentCell == null) {
                return;
            }

            // Check if the hovered cell contains the player's piece
            if (GridPane.getRowIndex(currentCell) == row && GridPane.getColumnIndex(currentCell) == col) {
                currentArcHovered = arcToMove;

                // Iterate through valid moves and highlight corresponding cells
                for (int[] move : validMoves) {
                    int dx = move[0];
                    int dy = move[1];
                    int newRow = row + dx;
                    int newCol = col + dy;

                    // Check if the new position is within bounds
                    if (newRow >= 0 && newRow < 5 && newCol >= 0 && newCol < 5) {
                        StackPane nextCell = getCellFromGridPane(gridPane, newRow, newCol);
                        if (nextCell != null && nextCell.getChildren().isEmpty()) {
                            // Highlight the cell
                            nextCell.setStyle("-fx-background-color: yellow;");
                        }
                    }
                }
            } else {
                clearCellHighlights();
            }
        }
    }

    private void handleCellExit(int row, int col) {
        if (isMyTurn) {
            Jogador currentPlayer = isBlueTurn ? jogador1 : jogador2;
            Arc arcToMove = currentPlayer.getArc();

            StackPane currentCell = getCellForArc(arcToMove);
            if (currentCell != null && GridPane.getRowIndex(currentCell) == row && GridPane.getColumnIndex(currentCell) == col) {
                clearCellHighlights();
            }
        }
    }

    private void clearCellHighlights() {
        for (Node node : gridPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane cell = (StackPane) node;
                cell.setStyle("");
            }
        }
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

    // Verifica se o movimento é válido e aplica o efeito de hover
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
                    // Aplica o efeito de hover
                    nextCell.setStyle("-fx-background-color: yellow;");
                    nextCell.setOnMouseExited(event -> nextCell.setStyle(""));
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

    // Marca a posição inicial
    private void markStartingPosition(int row, int col, Color color) {
        Circle ball = new Circle(10, color);
        ball.setEffect(new DropShadow(5, color.darker()));
        StackPane cell = getCellFromGridPane(gridPane, row, col);
        if (cell != null) {
            cell.getChildren().add(ball);
            markingBalls[row][col] = ball;
        }
    }

    // Declara o vencedor
    private void declareWinner(boolean isBlueWinner) {
        String winnerName = isBlueWinner ? jogador2.getName() : jogador1.getName();

        // Mostra um diálogo indicando o vencedor
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fim do Jogo");
        alert.setHeaderText("O jogador " + winnerName + "venceu!");
        alert.setContentText("Parabéns!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Fecha a interface de utilizador após reconhecer o vencedor
            Platform.exit();
        }
    }

    // Atualiza o tabuleiro
    void updateBoard(int row, int col) {
        Jogador currentPlayer = isBlueTurn ? jogador1 : jogador2;
        Arc arcToMove = currentPlayer.getArc();
        moveArcToPosition(arcToMove, row, col);
        addMarkingBall(row, col, currentPlayer.getColor());
        isBlueTurn = !isBlueTurn;
        updateTurnText();

        // Atualiza as coordenadas do último movimento com base no jogador atual
        if (currentPlayer == jogador1) {
            lastMoveRowPlayer1 = row;
            lastMoveColPlayer1 = col;
        } else if (currentPlayer == jogador2) {
            lastMoveRowPlayer2 = row;
            lastMoveColPlayer2 = col;
        }
    }

    // Adiciona uma bola de marcação
    private void addMarkingBall(int row, int col, Color color) {
        Circle ball = new Circle(10, color);
        ball.setEffect(new DropShadow(5, color.darker()));
        StackPane cell = getCellFromGridPane(gridPane, row, col);
        if (cell != null) {
            cell.getChildren().add(ball);
            markingBalls[row][col] = ball;
        }
    }

    // Atualiza o texto do turno
    private void updateTurnText() {
        if (isBlueTurn) {
            turnText.setText("Turno: " + jogador1.getName());
            playerText.setText(jogador1.getName());
        } else {
            turnText.setText("Turno: " + jogador2.getName());
            playerText.setText(jogador2.getName());
        }
    }
}
