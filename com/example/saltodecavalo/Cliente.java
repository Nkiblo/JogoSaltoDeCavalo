package com.example.saltodecavalo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "192.168.1.177";
    private static final int SERVER_PORT = 12345;
    private static TabuleiroController tabuleiroController;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            Scanner scanner = new Scanner(System.in);
            String serverMessage;

            // Inicializa o controlador do tabuleiro
            initializeTabuleiroController();

            // Lê as mensagens do servidor e processa-as
            while ((serverMessage = input.readLine()) != null) {
                System.out.println(serverMessage);
                processServerMessage(serverMessage, scanner, output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para inicializar o controlador do tabuleiro
    private static void initializeTabuleiroController() {
        tabuleiroController = Projeto.getTabuleiroController();
    }