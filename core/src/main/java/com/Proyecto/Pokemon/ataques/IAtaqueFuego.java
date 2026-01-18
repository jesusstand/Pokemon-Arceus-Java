package com.Proyecto.Pokemon.ataques;

import com.Proyecto.Pokemon.pokemon.Pokemon;

/**
 * Interfaz que define los ataques únicos para Pokemon de tipo Fuego.
 */
public interface IAtaqueFuego {
    /**
     * Ataque de tipo Fuego: Lanza una bola de fuego al enemigo.
     *
     * @param objetivo Pokemon que recibe el ataque.
     * @return Daño causado.
     */
    int lanzallamas(Pokemon objetivo);

    /**
     * Ataque de tipo Fuego: Envuelve al enemigo en llamas.
     *
     * @param objetivo Pokemon que recibe el ataque.
     * @return Daño causado.
     */
    int llamarada(Pokemon objetivo);
}
