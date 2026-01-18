package com.Proyecto.Pokemon.pokemon;

import com.Proyecto.Pokemon.ataques.IAtaqueDragon;

/**
 * Clase que representa Pokemon de tipo Dragon.
 * Implementa la interfaz IAtaqueDragon con ataques únicos de tipo Dragon.
 */
public class PokeDragon extends Pokemon implements IAtaqueDragon {
    private int poderDragon; // Poder base de ataques de dragon

    /**
     * Constructor de Pokemon de tipo Dragon.
     *
     * @param nombre      Nombre del Pokemon.
     * @param peso        Peso en kilogramos.
     * @param sexo        Sexo del Pokemon.
     * @param vidaMaxima  Puntos de salud máximos.
     * @param poderDragon Poder base para ataques de dragon.
     */
    public PokeDragon(String nombre, double peso, String sexo, int vidaMaxima, int poderDragon) {
        super(nombre, peso, sexo, vidaMaxima, Tipo.DRAGON);
        this.poderDragon = poderDragon;
    }

    @Override
    public int rayoDraconico(Pokemon objetivo) {
        int danioBase = (int) (poderDragon * 1.5);
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.DRAGON, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        return danio;
    }

    @Override
    public int colaDragon(Pokemon objetivo) {
        int danioBase = (int) (poderDragon * 1.1);
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
            super("Dracórnea", 65.8, sexo, 110, 55);
        }
    }

    /**
     * Pokemon específico: Aethergon
     */
    public static class Aethergon extends PokeDragon {
        public Aethergon(String sexo) {
            super("Aethergon", 78.3, sexo, 120, 60);
        }
    }
}
