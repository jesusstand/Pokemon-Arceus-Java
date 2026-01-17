package com.Proyecto.Pokemon.ataques;

import com.Proyecto.Pokemon.pokemon.Pokemon;

/**
 * Interfaz que define los ataques únicos para Pokemon de tipo Planta.
 */
public interface IAtaquePlanta {
    /**
     * Ataque de tipo Planta: Lanza hojas afiladas al enemigo.
     *
     * @param objetivo Pokemon que recibe el ataque.
     * @return Daño causado.
     */
    int hojaAfilada(Pokemon objetivo);

    /**
     * Ataque de tipo Planta: Absorbe energía del enemigo.
     *
     * @param objetivo Pokemon que recibe el ataque.
     * @return Daño causado.
     */
    int absorber(Pokemon objetivo);
}
