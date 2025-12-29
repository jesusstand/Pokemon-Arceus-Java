package com.Proyecto.Pokemon;

/**
 * Clase que representa Pokemon de tipo Planta.
 * Implementa la interfaz IAtaquePlanta con ataques únicos de tipo Planta.
 */
public class PokePlanta extends Pokemon implements IAtaquePlanta {
    private int poderPlanta; // Poder base de ataques de planta

    /**
     * Constructor de Pokemon de tipo Planta.
     *
     * @param nombre      Nombre del Pokemon.
     * @param peso        Peso en kilogramos.
     * @param sexo        Sexo del Pokemon.
     * @param vidaMaxima  Puntos de salud máximos.
     * @param poderPlanta Poder base para ataques de planta.
     */
    public PokePlanta(String nombre, double peso, String sexo, int vidaMaxima, int poderPlanta) {
        super(nombre, peso, sexo, vidaMaxima, Tipo.PLANTA);
        this.poderPlanta = poderPlanta;
    }

    @Override
    public int hojaAfilada(Pokemon objetivo) {
        int danioBase = (int) (poderPlanta * 1.1);
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.PLANTA, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        return danio;
    }

    @Override
    public int absorber(Pokemon objetivo) {
        int danioBase = (int) (poderPlanta * 0.7);
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.PLANTA, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        // Absorbe vida: recupera toda la vida
        this.curar();
        return danio;
    }

    /**
     * Pokemon específico: Brotálamo
     */
    public static class Brotalamo extends PokePlanta {
        public Brotalamo(String sexo) {
            super("Brotálamo", 18.3, sexo, 70, 30);
        }
    }

    /**
     * Pokemon específico: Floravelo
     */
    public static class Floravelo extends PokePlanta {
        public Floravelo(String sexo) {
            super("Floravelo", 22.7, sexo, 85, 38);
        }
    }
}

