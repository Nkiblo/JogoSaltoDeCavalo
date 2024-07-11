package com.example.saltodecavalo;

import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

public class Jogador {
    private Color color;
    private String name;
    private Arc arc;

    // Construtor da classe Jogador que define a cor e o nome do jogador e cria o arco
    public Jogador(Color color, String name) {
        this.color = color;
        this.name = name;
        this.arc = createArc(color);
    }

    // Método privado para criar um arco com a cor especificada
    private Arc createArc(Color color) {
        Arc arc = new Arc(50, 50, 40, 40, 0, 360);
        arc.setType(ArcType.OPEN);
        arc.setStroke(color); // Define a cor da linha do arco
        arc.setFill(null); // O arco não tem preenchimento
        arc.setStrokeWidth(5); // Define a largura da linha do arco
        return arc;
    }

    // Método para obter a cor do jogador
    public Color getColor() {
        return color;
    }

    // Método para obter o nome do jogador
    public String getName() {
        return name;
    }

    // Método para obter o arco do jogador
    public Arc getArc() {
        return arc;
    }
}
