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