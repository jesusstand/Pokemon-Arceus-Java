package com.Proyecto.Pokemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Clase que gestiona el spawn (aparición) de Pokemon salvajes en la hierba.
 * Genera Pokemon aleatorios cuando el jugador camina sobre tiles de hierba.
 */
public class SpawnPokemon {
    private Random random;
    private List<Pokemon> pokemonsDisponibles;
    private static final double PROBABILIDAD_ENCUENTRO = 0.15; // 15% de probabilidad por paso

    /**
     * Constructor de SpawnPokemon.
     * Inicializa la lista de Pokemon disponibles para spawn.
     */
    public SpawnPokemon() {
        this.random = new Random();
        this.pokemonsDisponibles = new ArrayList<>();
        inicializarPokemonsDisponibles();
    }

    /**
     * Inicializa la lista de Pokemon que pueden aparecer en la hierba.
     * Aquí puedes agregar todos los Pokemon que quieras que aparezcan.
     */
    private void inicializarPokemonsDisponibles() {
        // Pokemon de tipo Planta
        pokemonsDisponibles.add(new PokePlanta.Brotalamo("Macho"));
        pokemonsDisponibles.add(new PokePlanta.Brotalamo("Hembra"));
        pokemonsDisponibles.add(new PokePlanta.Floravelo("Macho"));
        pokemonsDisponibles.add(new PokePlanta.Floravelo("Hembra"));

        // Pokemon de tipo Fuego
        pokemonsDisponibles.add(new PokeFuego.Ignirrojo("Macho"));
        pokemonsDisponibles.add(new PokeFuego.Ignirrojo("Hembra"));
        pokemonsDisponibles.add(new PokeFuego.Volcarex("Macho"));
        pokemonsDisponibles.add(new PokeFuego.Volcarex("Hembra"));

        // Pokemon de tipo Agua
        pokemonsDisponibles.add(new PokeAgua.Aqualisca("Macho"));
        pokemonsDisponibles.add(new PokeAgua.Aqualisca("Hembra"));
        pokemonsDisponibles.add(new PokeAgua.Mareonix("Macho"));
        pokemonsDisponibles.add(new PokeAgua.Mareonix("Hembra"));

        // Pokemon de tipo Dragon (más raros)
        pokemonsDisponibles.add(new PokeDragon.Dracornea("Macho"));
        pokemonsDisponibles.add(new PokeDragon.Dracornea("Hembra"));
        pokemonsDisponibles.add(new PokeDragon.Aethergon("Macho"));
        pokemonsDisponibles.add(new PokeDragon.Aethergon("Hembra"));
    }

    /**
     * Verifica si debe aparecer un Pokemon salvaje al caminar sobre hierba.
     *
     * @return Pokemon salvaje si hay encuentro, null si no hay encuentro.
     */
    public Pokemon verificarEncuentro() {
        if (random.nextDouble() < PROBABILIDAD_ENCUENTRO) {
            // Seleccionar un Pokemon aleatorio de la lista
            int indice = random.nextInt(pokemonsDisponibles.size());
            Pokemon pokemonBase = pokemonsDisponibles.get(indice);
            
            // Crear una nueva instancia del Pokemon (clonar)
            return crearInstanciaPokemon(pokemonBase);
        }
        return null;
    }

    /**
     * Crea una nueva instancia de un Pokemon basado en otro.
     * Esto asegura que cada encuentro sea independiente.
     *
     * @param original Pokemon original del cual crear una copia.
     * @return Nueva instancia del Pokemon con vida completa.
     */
    private Pokemon crearInstanciaPokemon(Pokemon original) {
        String nombre = original.getNombre();
        String sexo = original.getSexo();

        // Crear nueva instancia según el tipo
        if (original instanceof PokePlanta) {
            if (nombre.equals("Brotálamo")) {
                return new PokePlanta.Brotalamo(sexo);
            } else if (nombre.equals("Floravelo")) {
                return new PokePlanta.Floravelo(sexo);
            }
        } else if (original instanceof PokeFuego) {
            if (nombre.equals("Ignirrojo")) {
                return new PokeFuego.Ignirrojo(sexo);
            } else if (nombre.equals("Volcárex")) {
                return new PokeFuego.Volcarex(sexo);
            }
        } else if (original instanceof PokeAgua) {
            if (nombre.equals("Aqualisca")) {
                return new PokeAgua.Aqualisca(sexo);
            } else if (nombre.equals("Mareónix")) {
                return new PokeAgua.Mareonix(sexo);
            }
        } else if (original instanceof PokeDragon) {
            if (nombre.equals("Dracórnea")) {
                return new PokeDragon.Dracornea(sexo);
            } else if (nombre.equals("Aethergon")) {
                return new PokeDragon.Aethergon(sexo);
            }
        }

        // Fallback (no debería llegar aquí)
        return original;
    }

    /**
     * Obtiene la probabilidad de encuentro actual.
     *
     * @return Probabilidad de encuentro (0.0 a 1.0).
     */
    public double getProbabilidadEncuentro() {
        return PROBABILIDAD_ENCUENTRO;
    }
}

