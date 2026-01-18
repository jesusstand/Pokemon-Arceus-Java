package com.Proyecto.Pokemon.pokemon;

import com.Proyecto.Pokemon.ataques.IAtaqueFuego;

/**
 * Clase que representa Pokemon de tipo Fuego.
 * Implementa la interfaz IAtaqueFuego con ataques únicos de tipo Fuego.
 */
public class PokeFuego extends Pokemon implements IAtaqueFuego {
    private int poderFuego; // Poder base de ataques de fuego

    /**
     * Constructor de Pokemon de tipo Fuego.
     *
     * @param nombre     Nombre del Pokemon.
     * @param peso       Peso en kilogramos.
     * @param sexo       Sexo del Pokemon.
     * @param vidaMaxima Puntos de salud máximos.
     * @param poderFuego Poder base para ataques de fuego.
     */
    public PokeFuego(String nombre, double peso, String sexo, int vidaMaxima, int poderFuego) {
        super(nombre, peso, sexo, vidaMaxima, Tipo.FUEGO);
        this.poderFuego = poderFuego;
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
            super("Ignirrojo", 28.5, sexo, 75, 42);
        }
    }

    /**
     * Pokemon específico: Volcárex
     */
    public static class Volcarex extends PokeFuego {
        public Volcarex(String sexo) {
            super("Volcárex", 45.2, sexo, 100, 50);
        }
    }
}
