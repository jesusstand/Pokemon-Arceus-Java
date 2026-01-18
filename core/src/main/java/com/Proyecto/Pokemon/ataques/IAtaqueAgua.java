package com.Proyecto.Pokemon.ataques;

import com.Proyecto.Pokemon.pokemon.Pokemon;

/**
 * Interfaz que define los ataques únicos para Pokemon de tipo Agua.
 */
public interface IAtaqueAgua {
    /**
     * Ataque de tipo Agua: Lanza un chorro de agua al enemigo.
     *
     * @param objetivo Pokemon que recibe el ataque.
     * @return Daño causado.
     */
    int hidrochorro(Pokemon objetivo);

    /**
     * Ataque de tipo Agua: Crea una burbuja que atrapa al enemigo.
     *
     * @param objetivo Pokemon que recibe el ataque.
     * @return Daño causado.
     */
    int burbuja(Pokemon objetivo);
}
