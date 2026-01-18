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
    private static final double PROBABILIDAD_ENCUENTRO = 0.12; // 12% de probabilidad por paso (más equilibrado)

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
     * Inicializa la lista de Pokemon que pueden aparecer en la hierba con pesos de raridad.
     * Mayor peso = más común, menor peso = más raro.
     */
    private void inicializarPokemonsDisponibles() {
        // Pokemon comunes (peso alto) - 40% probabilidad total
        agregarPokemon(new PokePlanta.Brotalamo("Macho"), 8.0);
        agregarPokemon(new PokePlanta.Brotalamo("Hembra"), 8.0);
        agregarPokemon(new PokeFuego.Ignirrojo("Macho"), 7.0);
        agregarPokemon(new PokeFuego.Ignirrojo("Hembra"), 7.0);
        agregarPokemon(new PokeAgua.Aqualisca("Macho"), 7.0);
        agregarPokemon(new PokeAgua.Aqualisca("Hembra"), 7.0);
        
        // Pokemon poco comunes (peso medio) - 35% probabilidad total
        agregarPokemon(new PokePlanta.Floravelo("Macho"), 5.0);
        agregarPokemon(new PokePlanta.Floravelo("Hembra"), 5.0);
        agregarPokemon(new PokeFuego.Volcarex("Macho"), 4.5);
        agregarPokemon(new PokeFuego.Volcarex("Hembra"), 4.5);
        agregarPokemon(new PokeAgua.Mareonix("Macho"), 4.5);
        agregarPokemon(new PokeAgua.Mareonix("Hembra"), 4.5);
        
        // Pokemon raros (peso bajo) - 20% probabilidad total
        agregarPokemon(new PokeDragon.Dracornea("Macho"), 2.0);
        agregarPokemon(new PokeDragon.Dracornea("Hembra"), 2.0);
        
        // Pokemon muy raros (peso muy bajo) - 5% probabilidad total
        agregarPokemon(new PokeDragon.Aethergon("Macho"), 0.8);
        agregarPokemon(new PokeDragon.Aethergon("Hembra"), 0.8);
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
     *
     * @return Pokemon salvaje si hay encuentro, null si no hay encuentro.
     */
    public Pokemon verificarEncuentro() {
        // Verificar si hay encuentro basado en probabilidad
        if (random.nextDouble() < PROBABILIDAD_ENCUENTRO) {
            // Seleccionar Pokemon basado en pesos (sistema de raridad)
            Pokemon pokemonBase = seleccionarPokemonConPeso();
            
            // Crear una nueva instancia del Pokemon (clonar)
            return crearInstanciaPokemon(pokemonBase);
        }
        return null;
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
