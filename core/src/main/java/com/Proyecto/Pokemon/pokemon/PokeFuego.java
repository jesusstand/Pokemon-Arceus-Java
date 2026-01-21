package com.Proyecto.Pokemon.pokemon;

import com.Proyecto.Pokemon.ataques.IAtaqueFuego;

/**
 * Clase que representa Pokemon de tipo Fuego.
 * Implementa la interfaz IAtaqueFuego con ataques únicos de tipo Fuego.
 */
public class PokeFuego extends Pokemon implements IAtaqueFuego {
    private int poderFuego; // Poder ACTUAL de ataques de fuego
    private int basePoderFuego; // Poder BASE de ataques de fuego

    /**
     * Constructor de Pokemon de tipo Fuego.
     */
    public PokeFuego(String nombre, double peso, String sexo, int vidaMaxima, int poderFuego, int nivel) {
        super(nombre, peso, sexo, vidaMaxima, Tipo.FUEGO, nivel);
        this.basePoderFuego = poderFuego;
        calcularEstadisticas();
    }

    // Constructor compatibilidad
    public PokeFuego(String nombre, double peso, String sexo, int vidaMaxima, int poderFuego) {
        this(nombre, peso, sexo, vidaMaxima, poderFuego, 1);
    }

    @Override
    protected void calcularEstadisticas() {
        super.calcularEstadisticas();
        // Poder aumenta 10% por nivel extra
        double porcentajePoder = 1.0 + (0.10 * (nivel - 1));
        this.poderFuego = (int) (basePoderFuego * porcentajePoder);
    }

    @Override
    public int lanzallamas(Pokemon objetivo) {
        int danioBase = (int) (poderFuego * 0.6); // Reducido de 1.2 a 0.6
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.FUEGO, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        return danio;
    }

    @Override
    public int llamarada(Pokemon objetivo) {
        int danioBase = (int) (poderFuego * 0.75); // Reducido de 1.4 a 0.75
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.FUEGO, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        return danio;
    }

    /**
     * Pokemon específico: Ignirrojo
     */
    public static class Ignirrojo extends PokeFuego {
        public Ignirrojo(String sexo) {
            super("Ignirrojo", 28.5, sexo, 75, 42, 1);
        }

        public Ignirrojo(String sexo, int nivel) {
            super("Ignirrojo", 28.5, sexo, 75, 42, nivel);
        }
    }

    /**
     * Pokemon específico: Volcárex
     */
    public static class Volcarex extends PokeFuego {
        public Volcarex(String sexo) {
            super("Volcárex", 45.2, sexo, 100, 50, 1); // Default lvl 1
        }

        public Volcarex(String sexo, int nivel) {
            super("Volcárex", 45.2, sexo, 100, 50, nivel);
        }
    }
}
