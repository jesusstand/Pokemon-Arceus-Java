package com.Proyecto.Pokemon.jugador;

import com.Proyecto.Pokemon.pokemon.Pokemon;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que gestiona el equipo actual de Pokémon del jugador.
 * Limitado a un máximo de 6 Pokémon.
 */
public class EquipoPokemon {
    private List<Pokemon> pokemons;
    private static final int MAX_EQUIPO = 6;

    public EquipoPokemon() {
        this.pokemons = new ArrayList<>();
    }

    /**
     * Intenta añadir un Pokémon al equipo.
     * 
     * @param pokemon Pokémon a añadir.
     * @return true si se añadió (hay espacio), false si el equipo está lleno.
     */
    public boolean agregarPokemon(Pokemon pokemon) {
        if (pokemons.size() < MAX_EQUIPO) {
            pokemons.add(pokemon);
            return true;
        }
        return false;
    }

    public List<Pokemon> getPokemons() {
        return pokemons;
    }

    public int getCantidad() {
        return pokemons.size();
    }

    public boolean estaLleno() {
        return pokemons.size() >= MAX_EQUIPO;
    }

    public Pokemon getPokemon(int index) {
        if (index >= 0 && index < pokemons.size()) {
            return pokemons.get(index);
        }
        return null;
    }

    /**
     * Cambia la posición de dos Pokémon en el equipo.
     */
    public void intercambiar(int i, int j) {
        if (i >= 0 && i < pokemons.size() && j >= 0 && j < pokemons.size()) {
            Pokemon temp = pokemons.get(i);
            pokemons.set(i, pokemons.get(j));
            pokemons.set(j, temp);
        }
    }

    public void eliminarPokemon(int index) {
        if (index >= 0 && index < pokemons.size()) {
            pokemons.remove(index);
        }
    }

    public void limpiar() {
        pokemons.clear();
    }
}
