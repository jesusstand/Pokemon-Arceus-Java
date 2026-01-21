package com.Proyecto.Pokemon.sistema;

import com.Proyecto.Pokemon.pokemon.Pokemon;
import com.Proyecto.Pokemon.pokemon.PokePlanta;
import com.Proyecto.Pokemon.pokemon.PokeFuego;
import com.Proyecto.Pokemon.pokemon.PokeAgua;
import com.Proyecto.Pokemon.pokemon.PokeDragon;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Clase que gestiona el spawn (aparición) de Pokemon salvajes en la hierba.
 * Genera Pokemon aleatorios cuando el jugador camina sobre tiles de hierba.
 * Usa un sistema de pesos para controlar la rareza de cada Pokemon.
 */
public class SpawnPokemon {
    /**
     * Clase interna para representar un Pokemon con su peso/raridad.
     */
    private static class PokemonConPeso {
        Pokemon pokemon;
        double peso; // Mayor peso = más común

        PokemonConPeso(Pokemon pokemon, double peso) {
            this.pokemon = pokemon;
            this.peso = peso;
        }
    }

    private Random random;
    private List<PokemonConPeso> pokemonsConPeso;
    private double pesoTotal;

    // Historial eliminado para permitir azar total

    /**
     * Constructor de SpawnPokemon.
     * Inicializa la lista de Pokemon disponibles para spawn con pesos.
     */
    public SpawnPokemon() {
        this.random = new Random();
        this.pokemonsConPeso = new ArrayList<>();
        this.pesoTotal = 0.0;
        inicializarPokemonsDisponibles();
    }

    /**
     * Inicializa la lista de Pokemon que pueden aparecer en la hierba con pesos de
     * raridad.
     * Mayor peso = más común, menor peso = más raro.
     * Todos los Pokemon no-Dragon tienen el mismo peso (misma probabilidad de
     * aparición).
     * Los Pokemon tipo Dragon (legendarios) NO aparecen en estado salvaje.
     */
    private void inicializarPokemonsDisponibles() {
        // Todos los Pokemon no-Dragon tienen el mismo peso (misma probabilidad)
        double pesoComun = 10.0; // Peso igual para todos los no-legendarios

        // Pokemon tipo Planta
        agregarPokemon(new PokePlanta.Brotalamo("Macho"), pesoComun);
        agregarPokemon(new PokePlanta.Brotalamo("Hembra"), pesoComun);
        agregarPokemon(new PokePlanta.Floravelo("Macho"), pesoComun);
        agregarPokemon(new PokePlanta.Floravelo("Hembra"), pesoComun);

        // Pokemon tipo Fuego
        agregarPokemon(new PokeFuego.Ignirrojo("Macho"), pesoComun);
        agregarPokemon(new PokeFuego.Ignirrojo("Hembra"), pesoComun);
        agregarPokemon(new PokeFuego.Volcarex("Macho"), pesoComun);
        agregarPokemon(new PokeFuego.Volcarex("Hembra"), pesoComun);

        // Pokemon tipo Agua
        agregarPokemon(new PokeAgua.Aqualisca("Macho"), pesoComun);
        agregarPokemon(new PokeAgua.Aqualisca("Hembra"), pesoComun);
        agregarPokemon(new PokeAgua.Mareonix("Macho"), pesoComun);
        agregarPokemon(new PokeAgua.Mareonix("Hembra"), pesoComun);

        // NOTA: Los Pokemon tipo Dragon (Dracornea y Aethergon) son legendarios
        // y NO aparecen en estado salvaje. Solo se pueden obtener mediante eventos
        // especiales o batallas específicas.
    }

    /**
     * Agrega un Pokemon con su peso a la lista de disponibles.
     */
    private void agregarPokemon(Pokemon pokemon, double peso) {
        pokemonsConPeso.add(new PokemonConPeso(pokemon, peso));
        pesoTotal += peso;
    }

    /**
     * Verifica si debe aparecer un Pokemon salvaje al caminar sobre hierba.
     * Usa un sistema de pesos para determinar qué Pokemon aparece.
     * Evita que el mismo Pokémon aparezca 3 veces seguidas.
     * Evita que el mismo tipo (Fuego, Agua, Planta) aparezca consecutivamente.
     *
     * @return Pokemon salvaje si hay encuentro, null si no hay encuentro.
     */
    /**
     * Genera un Pokemon salvaje aleatorio.
     * Ya no utiliza filtros de historial ni probabilidad interna (se controla
     * externamente).
     * Todos los Pokemon tienen la misma probabilidad (peso) según la configuración.
     *
     * @return Nueva instancia de Pokemon salvaje.
     */
    public Pokemon verificarEncuentro() {
        // Seleccionar Pokemon basado en pesos (pure random)
        Pokemon pokemonBase = seleccionarPokemonConPeso();
        System.out.println("DEBUG SPAWN: Generando " + pokemonBase.getNombre() + " (AZAR PURO)");
        return crearInstanciaPokemon(pokemonBase);
    }

    /**
     * Selecciona un Pokemon aleatorio basado en su peso/raridad.
     * Pokemon con mayor peso tienen más probabilidad de aparecer.
     *
     * @return Pokemon seleccionado según su peso.
     */
    private Pokemon seleccionarPokemonConPeso() {
        // Generar un número aleatorio entre 0 y pesoTotal
        double valorAleatorio = random.nextDouble() * pesoTotal;

        // Recorrer la lista hasta encontrar el Pokemon correspondiente
        double acumulado = 0.0;
        for (PokemonConPeso pcp : pokemonsConPeso) {
            acumulado += pcp.peso;
            if (valorAleatorio <= acumulado) {
                return pcp.pokemon;
            }
        }

        // Fallback: devolver el último Pokemon (no debería llegar aquí)
        return pokemonsConPeso.get(pokemonsConPeso.size() - 1).pokemon;
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

        // Nivel aleatorio entre 1 y 3 para salvajes
        int nivel = random.nextInt(3) + 1;

        // Crear nueva instancia según el tipo
        if (original instanceof PokePlanta) {
            if (nombre.equals("Brotálamo")) {
                return new PokePlanta.Brotalamo(sexo, nivel);
            } else if (nombre.equals("Floravelo")) {
                return new PokePlanta.Floravelo(sexo, nivel);
            }
        } else if (original instanceof PokeFuego) {
            if (nombre.equals("Ignirrojo")) {
                return new PokeFuego.Ignirrojo(sexo, nivel);
            } else if (nombre.equals("Volcárex")) {
                return new PokeFuego.Volcarex(sexo, nivel);
            }
        } else if (original instanceof PokeAgua) {
            if (nombre.equals("Aqualisca")) {
                return new PokeAgua.Aqualisca(sexo, nivel);
            } else if (nombre.equals("Mareónix")) {
                return new PokeAgua.Mareonix(sexo, nivel);
            }
        } else if (original instanceof PokeDragon) {
            // Los dragones no aparecen salvajes normalmente, pero si lo hicieran aqui...
            if (nombre.equals("Dracórnea")) {
                return new PokeDragon.Dracornea(sexo, nivel);
            } else if (nombre.equals("Aethergon")) {
                return new PokeDragon.Aethergon(sexo, nivel);
            }
        }

        // Fallback (no debería llegar aquí)
        return original;
    }
}
