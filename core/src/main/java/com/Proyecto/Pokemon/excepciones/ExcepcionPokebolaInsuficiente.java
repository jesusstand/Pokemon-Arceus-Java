package com.Proyecto.Pokemon.excepciones;

/**
 * Excepcion que se lanza cuando no hay suficientes pokebolas
 * para intentar capturar un Pokemon.
 */
public class ExcepcionPokebolaInsuficiente extends Exception {
    public ExcepcionPokebolaInsuficiente(String message) {
        super(message);
    }
}
