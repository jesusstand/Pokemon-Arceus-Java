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
        // Usar ataque aleatorio por defecto
        return realizarAtaque(null);
    }
    
    /**
     * Realiza un ataque específico del Pokemon atacante al defensor.
     * Si el nombreAtaque es null, usa un ataque aleatorio.
     *
     * @param nombreAtaque Nombre del ataque a usar (null para aleatorio).
     * @return Mensaje descriptivo del ataque realizado.
     */
    public String realizarAtaque(String nombreAtaque) {
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
        String nombreAtaqueUsado = "";
        String mensajeEfectividad = "";

        // Determinar el ataque según el tipo del Pokemon
        // Si nombreAtaque es null o vacío, usar ataque aleatorio
        boolean usarAtaqueAleatorio = (nombreAtaque == null || nombreAtaque.isEmpty());
        
        if (atacante instanceof PokeFuego) {
            PokeFuego fuego = (PokeFuego) atacante;
            if (usarAtaqueAleatorio || nombreAtaque.equals("Lanzallamas")) {
                danio = fuego.lanzallamas(defensor);
                nombreAtaqueUsado = "Lanzallamas";
            } else if (nombreAtaque.equals("Llamarada")) {
                danio = fuego.llamarada(defensor);
                nombreAtaqueUsado = "Llamarada";
            } else {
                // Ataque por defecto si no coincide
                danio = fuego.lanzallamas(defensor);
                nombreAtaqueUsado = "Lanzallamas";
            }
        } else if (atacante instanceof PokeAgua) {
            PokeAgua agua = (PokeAgua) atacante;
            if (usarAtaqueAleatorio || nombreAtaque.equals("Hidrochorro")) {
                danio = agua.hidrochorro(defensor);
                nombreAtaqueUsado = "Hidrochorro";
            } else if (nombreAtaque.equals("Burbuja")) {
                danio = agua.burbuja(defensor);
                nombreAtaqueUsado = "Burbuja";
            } else {
                danio = agua.hidrochorro(defensor);
                nombreAtaqueUsado = "Hidrochorro";
            }
        } else if (atacante instanceof PokePlanta) {
            PokePlanta planta = (PokePlanta) atacante;
            if (usarAtaqueAleatorio || nombreAtaque.equals("Hoja Afilada")) {
                danio = planta.hojaAfilada(defensor);
                nombreAtaqueUsado = "Hoja Afilada";
            } else if (nombreAtaque.equals("Absorber")) {
                danio = planta.absorber(defensor);
                nombreAtaqueUsado = "Absorber";
            } else {
                danio = planta.hojaAfilada(defensor);
                nombreAtaqueUsado = "Hoja Afilada";
            }
        } else if (atacante instanceof PokeDragon) {
            PokeDragon dragon = (PokeDragon) atacante;
            if (usarAtaqueAleatorio || nombreAtaque.equals("Rayo Draconico")) {
                danio = dragon.rayoDraconico(defensor);
                nombreAtaqueUsado = "Rayo Draconico";
            } else if (nombreAtaque.equals("Cola Dragon")) {
                danio = dragon.colaDragon(defensor);
                nombreAtaqueUsado = "Cola Dragon";
            } else {
                danio = dragon.rayoDraconico(defensor);
                nombreAtaqueUsado = "Rayo Draconico";
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
        mensaje.append(atacante.getNombre()).append(" usa ").append(nombreAtaqueUsado)
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
     * Consume el turno del atacante actual cambiando al siguiente turno.
     * Útil cuando se usa un objeto (como pokebola) que consume el turno sin atacar.
     */
    public void consumirTurno() {
        if (!batallaTerminada) {
            cambiarTurnos();
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
    
    /**
     * Cambia el Pokemon del jugador durante la batalla.
     * Asume que pokemon1 es siempre el del jugador.
     *
     * @param nuevoPokemon Nuevo Pokemon del jugador.
     */
    public void cambiarPokemonJugador(Pokemon nuevoPokemon) {
        Pokemon viejoPokemon = pokemon1;
        pokemon1 = nuevoPokemon;
        
        // Actualizar referencias de atacante y defensor si estaban usando el pokemon anterior
        if (atacante == viejoPokemon) {
            atacante = nuevoPokemon;
        }
        if (defensor == viejoPokemon) {
            defensor = nuevoPokemon;
        }
    }
}
