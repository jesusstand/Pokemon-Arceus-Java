package com.Proyecto.Pokemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Clase que gestiona la captura de Pokemon usando pokebolas.
 * La efectividad de captura depende de la vida restante del Pokemon.
 */
public class CapturaPokemon {
    private Almacenamiento inventario;
    private List<Pokemon> pokemonsCapturados;

    /**
     * Constructor de CapturaPokemon.
     *
     * @param inventario Inventario del jugador que contiene las pokebolas.
     */
    public CapturaPokemon(Almacenamiento inventario) {
        this.inventario = inventario;
        this.pokemonsCapturados = new ArrayList<>();
    }

    /**
     * Intenta capturar un Pokemon usando una pokebola del inventario.
     * La probabilidad de captura aumenta cuando el Pokemon tiene menos vida.
     *
     * @param pokemon Pokemon que se intenta capturar.
     * @param tipoPokebola Tipo de pokebola a usar ("Pokeball", "PokeballEXP", "PokeballCura").
     * @return true si la captura fue exitosa, false si falló.
     * @throws ExcepcionPokebolaInsuficiente Si no hay pokebolas del tipo solicitado.
     */
    public boolean intentarCapturar(Pokemon pokemon, String tipoPokebola) throws ExcepcionPokebolaInsuficiente {
        // Verificar que el Pokemon esté vivo
        if (!pokemon.estaVivo()) {
            throw new IllegalArgumentException("No se puede capturar un Pokemon derrotado.");
        }

        // Verificar que haya pokebolas disponibles
        HashMap<String, Integer> inv = inventario.getMapa();
        int cantidadPokebolas = inv.getOrDefault(tipoPokebola, 0);

        if (cantidadPokebolas <= 0) {
            throw new ExcepcionPokebolaInsuficiente("No tienes " + tipoPokebola + " en tu inventario.");
        }

        // Calcular probabilidad de captura basada en la vida restante
        double porcentajeVida = (double) pokemon.getVida() / pokemon.getVidaMaxima();
        double probabilidadBase = 1.0 - porcentajeVida; // Menos vida = más probabilidad

        // Ajustar probabilidad según el tipo de pokebola
        double multiplicadorPokebola = obtenerMultiplicadorPokebola(tipoPokebola);
        double probabilidadFinal = probabilidadBase * multiplicadorPokebola;

        // Asegurar que la probabilidad esté entre 0.1 y 0.95
        probabilidadFinal = Math.max(0.1, Math.min(0.95, probabilidadFinal));

        // Intentar captura
        boolean capturado = Math.random() < probabilidadFinal;

        // Consumir una pokebola (siempre se consume, incluso si falla)
        inv.put(tipoPokebola, cantidadPokebolas - 1);

        if (capturado) {
            // Agregar Pokemon a la lista de capturados
            pokemonsCapturados.add(pokemon);
            System.out.println("¡" + pokemon.getNombre() + " fue capturado exitosamente!");
            return true;
        } else {
            System.out.println("La pokebola falló. " + pokemon.getNombre() + " escapó.");
            return false;
        }
    }

    /**
     * Obtiene el multiplicador de efectividad según el tipo de pokebola.
     *
     * @param tipoPokebola Tipo de pokebola.
     * @return Multiplicador de efectividad.
     */
    private double obtenerMultiplicadorPokebola(String tipoPokebola) {
        switch (tipoPokebola) {
            case "Pokeball":
                return 1.0; // Efectividad normal
            case "PokeballEXP":
                return 1.2; // 20% más efectiva
            case "PokeballCura":
                return 1.3; // 30% más efectiva
            default:
                return 1.0;
        }
    }

    /**
     * Obtiene la probabilidad de captura sin intentar capturar.
     * Útil para mostrar al jugador las chances antes de intentar.
     *
     * @param pokemon Pokemon a evaluar.
     * @param tipoPokebola Tipo de pokebola a usar.
     * @return Probabilidad de captura (0.0 a 1.0).
     */
    public double calcularProbabilidadCaptura(Pokemon pokemon, String tipoPokebola) {
        double porcentajeVida = (double) pokemon.getVida() / pokemon.getVidaMaxima();
        double probabilidadBase = 1.0 - porcentajeVida;
        double multiplicadorPokebola = obtenerMultiplicadorPokebola(tipoPokebola);
        double probabilidadFinal = probabilidadBase * multiplicadorPokebola;
        return Math.max(0.1, Math.min(0.95, probabilidadFinal));
    }

    /**
     * Obtiene la lista de Pokemon capturados.
     *
     * @return Lista de Pokemon capturados.
     */
    public List<Pokemon> getPokemonsCapturados() {
        return pokemonsCapturados;
    }

    /**
     * Obtiene la cantidad de Pokemon capturados.
     *
     * @return Cantidad de Pokemon capturados.
     */
    public int getCantidadPokemonsCapturados() {
        return pokemonsCapturados.size();
    }

    /**
     * Muestra información de todos los Pokemon capturados.
     */
    public void mostrarPokemonsCapturados() {
        System.out.println("\n=== POKEMON CAPTURADOS ===");
        if (pokemonsCapturados.isEmpty()) {
            System.out.println("No has capturado ningún Pokemon aún.");
        } else {
            for (int i = 0; i < pokemonsCapturados.size(); i++) {
                Pokemon p = pokemonsCapturados.get(i);
                System.out.println((i + 1) + ". " + p.toString());
            }
        }
        System.out.println("=========================\n");
    }
}

