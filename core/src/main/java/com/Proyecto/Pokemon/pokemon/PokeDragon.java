package com.Proyecto.Pokemon.pokemon;

import com.Proyecto.Pokemon.ataques.IAtaqueDragon;

/**
 * Clase que representa Pokemon de tipo Dragon.
 * Implementa la interfaz IAtaqueDragon con ataques únicos de tipo Dragon.
 */
public class PokeDragon extends Pokemon implements IAtaqueDragon {
    private int poderDragon;
    private int basePoderDragon;

    /**
     * Constructor de Pokemon de tipo Dragon.
     */
    public PokeDragon(String nombre, double peso, String sexo, int vidaMaxima, int poderDragon, int nivel) {
        super(nombre, peso, sexo, vidaMaxima, Tipo.DRAGON, nivel);
        this.basePoderDragon = poderDragon;
        calcularEstadisticas();
    }

    // Constructor compatibilidad
    public PokeDragon(String nombre, double peso, String sexo, int vidaMaxima, int poderDragon) {
        this(nombre, peso, sexo, vidaMaxima, poderDragon, 1);
    }

    @Override
    protected void calcularEstadisticas() {
        super.calcularEstadisticas();
        // Poder aumenta 10% por nivel extra
        double porcentajePoder = 1.0 + (0.10 * (nivel - 1));
        this.poderDragon = (int) (basePoderDragon * porcentajePoder);
    }

    @Override
    public int rayoDraconico(Pokemon objetivo) {
        int danioBase = (int) (poderDragon * 0.8); // Reducido de 1.5 a 0.8
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.DRAGON, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        return danio;
    }

    @Override
    public int colaDragon(Pokemon objetivo) {
        int danioBase = (int) (poderDragon * 0.55); // Reducido de 1.1 a 0.55
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.DRAGON, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        return danio;
    }

    /**
     * Pokemon específico: Dracórnea
     */
    public static class Dracornea extends PokeDragon {
        public Dracornea(String sexo) {
            super("Dracórnea", 65.8, sexo, 110, 55, 1);
        }

        public Dracornea(String sexo, int nivel) {
            super("Dracórnea", 65.8, sexo, 110, 55, nivel);
        }
    }

    /**
     * Pokemon específico: Aethergon
     */
    public static class Aethergon extends PokeDragon {
        public Aethergon(String sexo) {
            super("Aethergon", 78.3, sexo, 120, 60, 1);
        }

        public Aethergon(String sexo, int nivel) {
            super("Aethergon", 78.3, sexo, 120, 60, nivel);
        }
    }
}
