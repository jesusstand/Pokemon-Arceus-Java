package com.Proyecto.Pokemon.pokemon;

import com.Proyecto.Pokemon.ataques.IAtaquePlanta;

/**
 * Clase que representa Pokemon de tipo Planta.
 * Implementa la interfaz IAtaquePlanta con ataques únicos de tipo Planta.
 */
public class PokePlanta extends Pokemon implements IAtaquePlanta {
    private int poderPlanta; // Poder ACTUAL de ataques de planta
    private int basePoderPlanta; // Poder BASE

    /**
     * Constructor de Pokemon de tipo Planta.
     */
    public PokePlanta(String nombre, double peso, String sexo, int vidaMaxima, int poderPlanta, int nivel) {
        super(nombre, peso, sexo, vidaMaxima, Tipo.PLANTA, nivel);
        this.basePoderPlanta = poderPlanta;
        calcularEstadisticas();
    }

    // Constructor compatibilidad
    public PokePlanta(String nombre, double peso, String sexo, int vidaMaxima, int poderPlanta) {
        this(nombre, peso, sexo, vidaMaxima, poderPlanta, 1);
    }

    @Override
    protected void calcularEstadisticas() {
        super.calcularEstadisticas();
        // Poder aumenta 10% por nivel extra
        double porcentajePoder = 1.0 + (0.10 * (nivel - 1));
        this.poderPlanta = (int) (basePoderPlanta * porcentajePoder);
    }

    @Override
    public int hojaAfilada(Pokemon objetivo) {
        int danioBase = (int) (poderPlanta * 0.55); // Reducido de 1.1 a 0.55
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.PLANTA, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        return danio;
    }

    @Override
    public int absorber(Pokemon objetivo) {
        int danioBase = (int) (poderPlanta * 0.35); // Reducido de 0.7 a 0.35
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.PLANTA, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        // Absorbe vida: recupera parte de la vida (25% del daño causado)
        int vidaRecuperada = (int) (danio * 0.25);
        this.vida = Math.min(this.vidaMaxima, this.vida + vidaRecuperada);
        return danio;
    }

    /**
     * Pokemon específico: Brotálamo
     */
    public static class Brotalamo extends PokePlanta {
        public Brotalamo(String sexo) {
            super("Brotálamo", 18.3, sexo, 70, 30, 1);
        }

        public Brotalamo(String sexo, int nivel) {
            super("Brotálamo", 18.3, sexo, 70, 30, nivel);
        }
    }

    /**
     * Pokemon específico: Floravelo
     */
    public static class Floravelo extends PokePlanta {
        public Floravelo(String sexo) {
            super("Floravelo", 22.7, sexo, 85, 38, 1);
        }

        public Floravelo(String sexo, int nivel) {
            super("Floravelo", 22.7, sexo, 85, 38, nivel);
        }
    }
}
