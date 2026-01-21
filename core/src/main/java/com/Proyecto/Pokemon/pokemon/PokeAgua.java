package com.Proyecto.Pokemon.pokemon;

import com.Proyecto.Pokemon.ataques.IAtaqueAgua;

/**
 * Clase que representa Pokemon de tipo Agua.
 * Implementa la interfaz IAtaqueAgua con ataques únicos de tipo Agua.
 */
public class PokeAgua extends Pokemon implements IAtaqueAgua {
    private int poderAgua;
    private int basePoderAgua;

    /**
     * Constructor de Pokemon de tipo Agua.
     */
    public PokeAgua(String nombre, double peso, String sexo, int vidaMaxima, int poderAgua, int nivel) {
        super(nombre, peso, sexo, vidaMaxima, Tipo.AGUA, nivel);
        this.basePoderAgua = poderAgua;
        calcularEstadisticas();
    }

    // Constructor compatibilidad
    public PokeAgua(String nombre, double peso, String sexo, int vidaMaxima, int poderAgua) {
        this(nombre, peso, sexo, vidaMaxima, poderAgua, 1);
    }

    @Override
    protected void calcularEstadisticas() {
        super.calcularEstadisticas();
        // Poder aumenta 10% por nivel extra
        double porcentajePoder = 1.0 + (0.10 * (nivel - 1));
        this.poderAgua = (int) (basePoderAgua * porcentajePoder);
    }

    @Override
    public int hidrochorro(Pokemon objetivo) {
        int danioBase = (int) (poderAgua * 0.6); // Reducido de 1.2 a 0.6
        double multiplicador = Pokemon.calcularMultiplicador(Tipo.AGUA, objetivo.getTipo());
        int danio = (int) (danioBase * multiplicador);
        objetivo.recibirDanio(danio);
        return danio;
    }

    @Override
    public int burbuja(Pokemon objetivo) {
        int danioBase = (int) (poderAgua * 0.4); // Reducido de 0.8 a 0.4
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
            super("Aqualisca", 25.5, sexo, 80, 35, 1);
        }

        public Aqualisca(String sexo, int nivel) {
            super("Aqualisca", 25.5, sexo, 80, 35, nivel);
        }
    }

    /**
     * Pokemon específico: Mareónix
     */
    public static class Mareonix extends PokeAgua {
        public Mareonix(String sexo) {
            super("Mareónix", 32.0, sexo, 95, 40, 1);
        }

        public Mareonix(String sexo, int nivel) {
            super("Mareónix", 32.0, sexo, 95, 40, nivel);
        }
    }
}
