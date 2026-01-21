package com.Proyecto.Pokemon.sistema;

import java.util.HashMap;

/**
 * Clase que gestiona el progreso de investigacion (Pokedex).
 * Mantiene el registro de puntos de investigacion para cada Pokemon.
 */
public class Pokedex {
    private HashMap<String, Integer> puntosInvestigacion;

    public Pokedex() {
        this.puntosInvestigacion = new HashMap<>();
    }

    /**
     * Registra puntos de investigacion para un Pokemon.
     * 
     * @param nombrePokemon Nombre del Pokemon.
     * @param puntos        Puntos a agregar.
     */
    public void registrarInvestigacion(String nombrePokemon, int puntos) {
        puntosInvestigacion.put(nombrePokemon, puntosInvestigacion.getOrDefault(nombrePokemon, 0) + puntos);
    }

    /**
     * Obtiene los puntos actuales de un Pokemon.
     * 
     * @param nombrePokemon Nombre del Pokemon.
     * @return Puntos acumulados.
     */
    public int getPuntos(String nombrePokemon) {
        return puntosInvestigacion.getOrDefault(nombrePokemon, 0);
    }

    /**
     * Verifica si una entrada de la Pokedex esta completa (nivel 10).
     */
    public boolean esEntradaCompleta(String nombrePokemon) {
        return getPuntos(nombrePokemon) >= 10;
    }

    public HashMap<String, Integer> getMapaPuntos() {
        return puntosInvestigacion;
    }

    public void setMapaPuntos(HashMap<String, Integer> mapa) {
        this.puntosInvestigacion.clear();
        if (mapa != null) {
            this.puntosInvestigacion.putAll(mapa);
        }
    }

    /**
     * Verifica si la Pokedex esta completa (las 6 entradas principales completas).
     * Los Pokemon son: Brotálamo, Floravelo, Ignirrojo, Volcárex, Aqualisca,
     * Mareónix.
     */
    public boolean esPokedexCompleta() {
        String[] pokemonsRequeridos = {
                "Brotálamo", "Floravelo",
                "Ignirrojo", "Volcárex",
                "Aqualisca", "Mareónix"
        };

        for (String nombre : pokemonsRequeridos) {
            if (!esEntradaCompleta(nombre)) {
                return false;
            }
        }
        return true;
    }
}
