package com.Proyecto.Pokemon.sistema;

import com.Proyecto.Pokemon.pokemon.Pokemon;
import com.Proyecto.Pokemon.pokemon.PokePlanta;
import com.Proyecto.Pokemon.pokemon.PokeFuego;
import com.Proyecto.Pokemon.pokemon.PokeAgua;
import com.Proyecto.Pokemon.pokemon.PokeDragon;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Clase que gestiona el spawn (aparición) de Pokemon salvajes en la hierba.
 * Genera Pokemon aleatorios cuando el jugador camina sobre tiles de hierba.
 */
public class SpawnPokemon {
    private Random random;

    // Listas separadas por tipo para asegurar variedad
    private List<Supplier<Pokemon>> tipoPlanta;
    private List<Supplier<Pokemon>> tipoFuego;
    private List<Supplier<Pokemon>> tipoAgua;
    private List<Supplier<Pokemon>> tipoDragon;

    private static final double PROBABILIDAD_ENCUENTRO = 0.15; // 15% de probabilidad por paso

    /**
     * Constructor de SpawnPokemon.
     * Inicializa las listas de Pokemon disponibles para spawn.
     */
    public SpawnPokemon() {
        this.random = new Random();
        this.tipoPlanta = new ArrayList<>();
        this.tipoFuego = new ArrayList<>();
        this.tipoAgua = new ArrayList<>();
        this.tipoDragon = new ArrayList<>();

        inicializarPokemonsDisponibles();
    }

    /**
     * Inicializa las listas de Pokemon que pueden aparecer en la hierba.
     * Usamos Suppliers (proveedores) para crear nuevas instancias frescas cada vez.
     */
    private void inicializarPokemonsDisponibles() {
        // Pokemon de tipo Planta
        tipoPlanta.add(() -> new PokePlanta.Brotalamo("Macho"));
        tipoPlanta.add(() -> new PokePlanta.Brotalamo("Hembra"));
        tipoPlanta.add(() -> new PokePlanta.Floravelo("Macho"));
        tipoPlanta.add(() -> new PokePlanta.Floravelo("Hembra"));

        // Pokemon de tipo Fuego
        tipoFuego.add(() -> new PokeFuego.Ignirrojo("Macho"));
        tipoFuego.add(() -> new PokeFuego.Ignirrojo("Hembra"));
        tipoFuego.add(() -> new PokeFuego.Volcarex("Macho"));
        tipoFuego.add(() -> new PokeFuego.Volcarex("Hembra"));

        // Pokemon de tipo Agua
        tipoAgua.add(() -> new PokeAgua.Aqualisca("Macho"));
        tipoAgua.add(() -> new PokeAgua.Aqualisca("Hembra"));
        tipoAgua.add(() -> new PokeAgua.Mareonix("Macho"));
        tipoAgua.add(() -> new PokeAgua.Mareonix("Hembra"));

        // Pokemon de tipo Dragon (más raros)
        tipoDragon.add(() -> new PokeDragon.Dracornea("Macho"));
        tipoDragon.add(() -> new PokeDragon.Dracornea("Hembra"));
        tipoDragon.add(() -> new PokeDragon.Aethergon("Macho"));
        tipoDragon.add(() -> new PokeDragon.Aethergon("Hembra"));
    }

    /**
     * Verifica si debe aparecer un Pokemon salvaje al caminar sobre hierba.
     *
     * @return Pokemon salvaje si hay encuentro, null si no hay encuentro.
     */
    public Pokemon verificarEncuentro() {
        if (random.nextDouble() < PROBABILIDAD_ENCUENTRO) {
            return obtenerPokemonAleatorio();
        }
        return null;
    }

    /**
     * Obtiene la probabilidad de encuentro actual.
     *
     * @return Probabilidad de encuentro (0.0 a 1.0).
     */
    public double getProbabilidadEncuentro() {
        return PROBABILIDAD_ENCUENTRO;
    }

    /**
     * Obtiene un Pokemon aleatorio asegurando variedad de tipos.
     * 1. Elige un tipo al azar.
     * 2. Elige un Pokemon al azar de ese tipo.
     *
     * @return Una nueva instancia de un Pokemon aleatorio.
     */
    public Pokemon obtenerPokemonAleatorio() {
        // 1. Elegir tipo (0=Planta, 1=Fuego, 2=Agua)
        int tipoSeleccionado = random.nextInt(3);
        List<Supplier<Pokemon>> listaElegida;

        switch (tipoSeleccionado) {
            case 0:
                listaElegida = tipoPlanta;
                break;
            case 1:
                listaElegida = tipoFuego;
                break;
            case 2:
                listaElegida = tipoAgua;
                break;
            default:
                listaElegida = tipoPlanta;
                break; // Fallback seguro
        }
        // 2. Elegir un proveedor de la lista y crear la instancia
        if (!listaElegida.isEmpty()) {
            int indice = random.nextInt(listaElegida.size());
            return listaElegida.get(indice).get();
        }

        // Fallback de seguridad
        return new PokePlanta.Brotalamo("Salvaje");
    }
}
