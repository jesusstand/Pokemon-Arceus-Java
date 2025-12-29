package com.Proyecto.Pokemon;

/**
 * Clase que representa Pokemon de tipo Agua.
 * Implementa la interfaz IAtaqueAgua con ataques únicos de tipo Agua.
 */
public class PokeAgua extends Pokemon implements IAtaqueAgua {
    private int poderAgua; // Poder base de ataques de agua

    /**
     * Constructor de Pokemon de tipo Agua.
     *
     * @param nombre     Nombre del Pokemon.
     * @param peso       Peso en kilogramos.
     * @param sexo       Sexo del Pokemon.
     * @param vidaMaxima Puntos de salud máximos.
     * @param poderAgua  Poder base para ataques de agua.
     */
    public PokeAgua(String nombre, double peso, String sexo, int vidaMaxima, int poderAgua) {
        super(nombre, peso, sexo, vidaMaxima, Tipo.AGUA);
        this.poderAgua = poderAgua;
    }

    @Override
    public int hidrochorro(Pokemon objetivo) {
        int danioBase = (int) (poderAgua * 1.2);
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.AGUA, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        return danio;
    }

    @Override
    public int burbuja(Pokemon objetivo) {
        int danioBase = (int) (poderAgua * 0.8);
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.AGUA, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        return danio;
    }

    /**
     * Pokemon específico: Aqualisca
     */
    public static class Aqualisca extends PokeAgua {
        public Aqualisca(String sexo) {
            super("Aqualisca", 25.5, sexo, 80, 35);
        }
    }

    /**
     * Pokemon específico: Mareónix
     */
    public static class Mareonix extends PokeAgua {
        public Mareonix(String sexo) {
            super("Mareónix", 32.0, sexo, 95, 40);
        }
    }
}

