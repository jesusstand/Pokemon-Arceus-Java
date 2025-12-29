package com.Proyecto.Pokemon;

/**
 * Clase padre que representa un Pokemon base.
 * Contiene las propiedades comunes de todos los Pokemon.
 */
public abstract class Pokemon {
    protected String nombre;
    protected double peso;
    protected String sexo; // "Macho", "Hembra" o "Sin género"
    protected int vida; // PS (Puntos de Salud)
    protected int vidaMaxima; // PS máximos
    protected Tipo tipo; // Tipo del Pokemon

    /**
     * Constructor de Pokemon.
     *
     * @param nombre     Nombre del Pokemon.
     * @param peso       Peso en kilogramos.
     * @param sexo       Sexo del Pokemon ("Macho", "Hembra" o "Sin género").
     * @param vidaMaxima Puntos de salud máximos.
     * @param tipo       Tipo del Pokemon.
     */
    public Pokemon(String nombre, double peso, String sexo, int vidaMaxima, Tipo tipo) {
        this.nombre = nombre;
        this.peso = peso;
        this.sexo = sexo;
        this.vidaMaxima = vidaMaxima;
        this.vida = vidaMaxima; // Al crear, la vida está al máximo
        this.tipo = tipo;
    }

    /**
     * Obtiene el nombre del Pokemon.
     *
     * @return Nombre del Pokemon.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el peso del Pokemon.
     *
     * @return Peso en kilogramos.
     */
    public double getPeso() {
        return peso;
    }

    /**
     * Obtiene el sexo del Pokemon.
     *
     * @return Sexo del Pokemon.
     */
    public String getSexo() {
        return sexo;
    }

    /**
     * Obtiene la vida actual del Pokemon.
     *
     * @return Puntos de salud actuales.
     */
    public int getVida() {
        return vida;
    }

    /**
     * Obtiene la vida máxima del Pokemon.
     *
     * @return Puntos de salud máximos.
     */
    public int getVidaMaxima() {
        return vidaMaxima;
    }

    /**
     * Reduce la vida del Pokemon.
     *
     * @param danio Cantidad de daño a recibir.
     */
    public void recibirDanio(int danio) {
        vida = Math.max(0, vida - danio);
    }

    /**
     * Restaura toda la vida del Pokemon al máximo.
     */
    public void curar() {
        vida = vidaMaxima;
    }

    /**
     * Verifica si el Pokemon está vivo.
     *
     * @return true si tiene vida mayor a 0.
     */
    public boolean estaVivo() {
        return vida > 0;
    }

    /**
     * Obtiene el tipo del Pokemon.
     *
     * @return Tipo del Pokemon.
     */
    public Tipo getTipo() {
        return tipo;
    }

    /**
     * Obtiene el nombre del tipo como String (para compatibilidad).
     *
     * @return Nombre del tipo como String.
     */
    public String getTipoString() {
        return tipo.name();
    }

    /**
     * Calcula el multiplicador de daño según la efectividad de tipos.
     *
     * @param tipoAtaque Tipo del ataque.
     * @param tipoDefensa Tipo del defensor.
     * @return Multiplicador de daño (2.0 = super efectivo, 0.5 = no efectivo, 1.0 = neutro).
     */
    public static double calcularMultiplicador(Tipo tipoAtaque, Tipo tipoDefensa) {
        // Fuego es super efectivo contra Planta
        if (tipoAtaque == Tipo.FUEGO && tipoDefensa == Tipo.PLANTA) return 2.0;
        // Planta es super efectivo contra Agua
        if (tipoAtaque == Tipo.PLANTA && tipoDefensa == Tipo.AGUA) return 2.0;
        // Agua es super efectivo contra Fuego
        if (tipoAtaque == Tipo.AGUA && tipoDefensa == Tipo.FUEGO) return 2.0;
        // Dragon es super efectivo contra Dragon
        if (tipoAtaque == Tipo.DRAGON && tipoDefensa == Tipo.DRAGON) return 2.0;

        // Planta no es efectivo contra Fuego
        if (tipoAtaque == Tipo.PLANTA && tipoDefensa == Tipo.FUEGO) return 0.5;
        // Fuego no es efectivo contra Agua
        if (tipoAtaque == Tipo.FUEGO && tipoDefensa == Tipo.AGUA) return 0.5;
        // Agua no es efectivo contra Planta
        if (tipoAtaque == Tipo.AGUA && tipoDefensa == Tipo.PLANTA) return 0.5;

        return 1.0; // Neutro para el resto de combinaciones
    }

    /**
     * Representación en texto del Pokemon.
     *
     * @return String con la información del Pokemon.
     */
    @Override
    public String toString() {
        return String.format("%s [%s] - PS: %d/%d - Peso: %.1f kg - Sexo: %s",
                nombre, tipo.name(), vida, vidaMaxima, peso, sexo);
    }
}

