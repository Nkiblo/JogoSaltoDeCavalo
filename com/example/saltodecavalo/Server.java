package com.example.saltodecavalo;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 12345;
    private static final ExecutorService pool = Executors.newFixedThreadPool(2);
    private static final Socket[] players = new Socket[2];
    private static final boolean[] playerReady = {false, false};
    private static int currentPlayer = 0;
    private static final Object lock = new Object();

    // Método principal que inicia o servidor
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado. Aguardando conexões...");
            int playerID = 0;

            // Aceita conexões até que dois jogadores estejam conectados
            while (playerID < 2) {
                Socket playerSocket = serverSocket.accept();
                players[playerID] = playerSocket;
                System.out.println("Jogador " + (playerID + 1) + " conectado.");
                pool.execute(new PlayerHandler(playerSocket, playerID));
                playerID++;
            }
            System.out.println("Dois jogadores conectados. Fechando servidor para novas conexões.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe interna que lida com a comunicação do jogador
    private static class PlayerHandler implements Runnable {
        private final Socket socket;
        private final int playerID;
        private BufferedReader input;
        private PrintWriter output;

        public PlayerHandler(Socket socket, int playerID) {
            this.socket = socket;
            this.playerID = playerID;
        }

        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                output.println("Você é o jogador " + (playerID + 1));

                // Loop principal do jogo
                while (true) {
                    synchronized (lock) {
                        if (playerID == currentPlayer) {
                            output.println("SUA_VEZ"); // Envia mensagem de turno ao jogador atual
                            String move = input.readLine(); // Aguarda movimento do jogador atual
                            if (move == null || move.trim().isEmpty()) {
                                throw new IOException("Player " + (playerID + 1) + " disconnected.");
                            }
                            System.out.println("Player " + (playerID + 1) + " moved: " + move);

                            // Processa o movimento (exemplo assumindo que o movimento está no formato linha:coluna)
                            String[] moveParts = move.split(":");
                            if (moveParts.length == 2) {
                                int row = Integer.parseInt(moveParts[0]);
                                int col = Integer.parseInt(moveParts[1]);

                                // Envia o movimento para todos os jogadores
                                outputToAllPlayers("MOVIMENTO:" + row + ":" + col);

                                // Alterna para o turno do próximo jogador
                                currentPlayer = 1 - currentPlayer;
                                lock.notifyAll(); // Notifica todas as threads em espera (jogadores)
                            } else {
                                System.err.println("Invalid move format: " + move);
                            }
                        } else {
                            while (playerID != currentPlayer) {
                                lock.wait(); // Espera se não for o turno deste jogador
                            }
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Método para enviar mensagens a todos os jogadores
        private void outputToAllPlayers(String message) {
            for (Socket player : players) {
                if (player != null) {
                    try {
                        PrintWriter playerOutput = new PrintWriter(player.getOutputStream(), true);
                        playerOutput.println(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
