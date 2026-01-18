package com.Proyecto.Pokemon.ataques;

import com.Proyecto.Pokemon.pokemon.Pokemon;

/**
 * Interfaz que define los ataques únicos para Pokemon de tipo Dragon.
 */
public interface IAtaqueDragon {
    /**
     * Ataque de tipo Dragon: Lanza un rayo draconico al enemigo.
     *
     * @param objetivo Pokemon que recibe el ataque.
     * @return Daño causado.
     */
    int rayoDraconico(Pokemon objetivo);

    /**
     * Ataque de tipo Dragon: Crea una ráfaga de poder draconico.
     *
     * @param objetivo Pokemon que recibe el ataque.
     * @return Daño causado.
     */
    int colaDragon(Pokemon objetivo);
}
