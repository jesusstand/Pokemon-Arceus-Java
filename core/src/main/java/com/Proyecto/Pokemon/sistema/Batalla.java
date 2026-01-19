package com.Proyecto.Pokemon.sistema;

import com.Proyecto.Pokemon.pokemon.Pokemon;
import com.Proyecto.Pokemon.pokemon.PokeFuego;
import com.Proyecto.Pokemon.pokemon.PokeAgua;
import com.Proyecto.Pokemon.pokemon.PokePlanta;
import com.Proyecto.Pokemon.pokemon.PokeDragon;

/**
 * Clase que gestiona las batallas entre Pokemon.
 * Maneja los turnos, ataques y determina el ganador.
 */
public class Batalla {
    private Pokemon pokemon1;
    private Pokemon pokemon2;
    private Pokemon atacante;
    private Pokemon defensor;
    private boolean batallaTerminada;

    /**
     * Constructor de Batalla.
     *
     * @param pokemon1 Primer Pokemon (inicia atacando).
     * @param pokemon2 Segundo Pokemon.
     */
    public Batalla(Pokemon pokemon1, Pokemon pokemon2) {
        this.pokemon1 = pokemon1;
        this.pokemon2 = pokemon2;
        this.atacante = pokemon1;
        this.defensor = pokemon2;
        this.batallaTerminada = false;
    }

    /**
     * Realiza un ataque del Pokemon atacante al defensor.
     * El ataque se determina automáticamente según el tipo del atacante.
     *
     * @return Mensaje descriptivo del ataque realizado.
     */
    public String realizarAtaque() {
        if (batallaTerminada) {
            return "La batalla ya ha terminado.";
        }

        if (!atacante.estaVivo()) {
            batallaTerminada = true;
            return atacante.getNombre() + " está derrotado y no puede atacar.";
        }

        if (!defensor.estaVivo()) {
            batallaTerminada = true;
            return defensor.getNombre() + " ya está derrotado.";
        }

        int danio = 0;
        String nombreAtaque = "";
        String mensajeEfectividad = "";

        // Determinar el ataque según el tipo del Pokemon
        if (atacante instanceof PokeFuego) {
            PokeFuego fuego = (PokeFuego) atacante;
            // Usar lanzallamas o llamarada (alternando)
            if (Math.random() > 0.5) {
                danio = fuego.lanzallamas(defensor);
                nombreAtaque = "Lanzallamas";
            } else {
                danio = fuego.llamarada(defensor);
                nombreAtaque = "Llamarada";
            }
        } else if (atacante instanceof PokeAgua) {
            PokeAgua agua = (PokeAgua) atacante;
            if (Math.random() > 0.5) {
                danio = agua.hidrochorro(defensor);
                nombreAtaque = "Hidrochorro";
            } else {
                danio = agua.burbuja(defensor);
                nombreAtaque = "Burbuja";
            }
        } else if (atacante instanceof PokePlanta) {
            PokePlanta planta = (PokePlanta) atacante;
            if (Math.random() > 0.5) {
                danio = planta.hojaAfilada(defensor);
                nombreAtaque = "Hoja Afilada";
            } else {
                danio = planta.absorber(defensor);
                nombreAtaque = "Absorber";
            }
        } else if (atacante instanceof PokeDragon) {
            PokeDragon dragon = (PokeDragon) atacante;
            if (Math.random() > 0.5) {
                danio = dragon.rayoDraconico(defensor);
                nombreAtaque = "Rayo Draconico";
            } else {
                danio = dragon.colaDragon(defensor);
                nombreAtaque = "Cola Dragon";
            }
        }

        // Calcular efectividad
        double multiplicador = Pokemon.calcularMultiplicador(atacante.getTipo(), defensor.getTipo());
        if (multiplicador == 2.0) {
            mensajeEfectividad = " ¡Es super efectivo!";
        } else if (multiplicador == 0.5) {
            mensajeEfectividad = " No es muy efectivo...";
        }

        // Construir mensaje
        StringBuilder mensaje = new StringBuilder();
        mensaje.append(atacante.getNombre()).append(" usa ").append(nombreAtaque)
                .append(" contra ").append(defensor.getNombre())
                .append(" causando ").append(danio).append(" puntos de daño")
                .append(mensajeEfectividad).append("\n");

        mensaje.append("Vida restante de ").append(defensor.getNombre())
                .append(": ").append(defensor.getVida()).append("/")
                .append(defensor.getVidaMaxima());

        // Verificar si el defensor fue derrotado
        if (!defensor.estaVivo()) {
            mensaje.append("\n").append(defensor.getNombre()).append(" ha sido derrotado!");
            batallaTerminada = true;
        }

        // Cambiar turnos
        cambiarTurnos();

        return mensaje.toString();
    }

    /**
     * Cambia los turnos entre los Pokemon.
     */
    private void cambiarTurnos() {
        if (atacante == pokemon1) {
            atacante = pokemon2;
            defensor = pokemon1;
        } else {
            atacante = pokemon1;
            defensor = pokemon2;
        }
    }

    /**
     * Obtiene el Pokemon ganador de la batalla.
     *
     * @return Pokemon ganador, o null si la batalla no ha terminado.
     */
    public Pokemon obtenerGanador() {
        if (!batallaTerminada) {
            return null;
        }

        if (pokemon1.estaVivo() && !pokemon2.estaVivo()) {
            return pokemon1;
        } else if (pokemon2.estaVivo() && !pokemon1.estaVivo()) {
            return pokemon2;
        }

        return null; // Empate (no debería pasar normalmente)
    }

    /**
     * Verifica si la batalla ha terminado.
     *
     * @return true si la batalla terminó.
     */
    public boolean estaTerminada() {
        return batallaTerminada || !pokemon1.estaVivo() || !pokemon2.estaVivo();
    }

    /**
     * Obtiene el Pokemon atacante actual.
     *
     * @return Pokemon que ataca en este turno.
     */
    public Pokemon getAtacante() {
        return atacante;
    }

    /**
     * Obtiene el Pokemon defensor actual.
     *
     * @return Pokemon que defiende en este turno.
     */
    public Pokemon getDefensor() {
        return defensor;
    }

    /**
     * Obtiene el primer Pokemon de la batalla.
     *
     * @return Primer Pokemon.
     */
    public Pokemon getPokemon1() {
        return pokemon1;
    }

    /**
     * Obtiene el segundo Pokemon de la batalla.
     *
     * @return Segundo Pokemon.
     */
    public Pokemon getPokemon2() {
        return pokemon2;
    }
}
