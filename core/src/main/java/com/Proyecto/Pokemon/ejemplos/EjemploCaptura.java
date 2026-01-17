package com.Proyecto.Pokemon.ejemplos;

import com.Proyecto.Pokemon.pokemon.PokeFuego;
import com.Proyecto.Pokemon.pokemon.PokePlanta;
import com.Proyecto.Pokemon.jugador.Almacenamiento;
import com.Proyecto.Pokemon.sistema.CapturaPokemon;
import com.Proyecto.Pokemon.excepciones.ExcepcionInventarioLleno;
import com.Proyecto.Pokemon.excepciones.ExcepcionPokebolaInsuficiente;

/**
 * Clase de ejemplo que muestra cómo usar el sistema de captura de Pokemon.
 * Esta clase puede ser eliminada, es solo para referencia.
 */
public class EjemploCaptura {
    
    public static void ejemploCaptura() {
        // Crear inventario y sistema de captura
        Almacenamiento inventario = new Almacenamiento();
        CapturaPokemon captura = new CapturaPokemon(inventario);
        
        // Agregar pokebolas al inventario
        try {
            inventario.agregarObjeto("Pokeball", "pokeball");
            inventario.agregarObjeto("Pokeball", "pokeball");
            inventario.agregarObjeto("PokeballEXP", "pokeball");
            inventario.agregarObjeto("PokeballCura", "pokeball");
        } catch (ExcepcionInventarioLleno e) {
            System.out.println(e.getMessage());
        }
        
        // Crear un Pokemon para capturar
        PokeFuego ignirrojo = new PokeFuego.Ignirrojo("Macho");
        
        System.out.println("=== SISTEMA DE CAPTURA ===\n");
        System.out.println("Pokemon a capturar: " + ignirrojo);
        System.out.println("Vida: " + ignirrojo.getVida() + "/" + ignirrojo.getVidaMaxima());
        
        // Mostrar probabilidad de captura con vida completa
        double probCompleta = captura.calcularProbabilidadCaptura(ignirrojo, "Pokeball");
        System.out.println("\nProbabilidad de captura (vida completa): " + 
                          String.format("%.1f%%", probCompleta * 100));
        
        // Reducir vida del Pokemon (más fácil de capturar)
        ignirrojo.recibirDanio(50);
        System.out.println("\nVida después de daño: " + ignirrojo.getVida() + "/" + ignirrojo.getVidaMaxima());
        
        double probHerido = captura.calcularProbabilidadCaptura(ignirrojo, "Pokeball");
        System.out.println("Probabilidad de captura (herido): " + 
                          String.format("%.1f%%", probHerido * 100));
        
        // Intentar capturar
        System.out.println("\n--- Intentando capturar ---");
        try {
            boolean exito = captura.intentarCapturar(ignirrojo, "Pokeball");
            if (exito) {
                System.out.println("¡Captura exitosa!");
            }
        } catch (ExcepcionPokebolaInsuficiente e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Mostrar Pokemon capturados
        captura.mostrarPokemonsCapturados();
    }
    
    public static void ejemploComparacionPokebolas() {
        System.out.println("\n=== COMPARACION DE POKEBOLAS ===\n");
        
        Almacenamiento inventario = new Almacenamiento();
        CapturaPokemon captura = new CapturaPokemon(inventario);
        
        // Crear Pokemon con vida reducida
        PokePlanta brotalamo = new PokePlanta.Brotalamo("Hembra");
        brotalamo.recibirDanio(40); // Dejar con 30/70 de vida
        
        System.out.println("Pokemon: " + brotalamo.getNombre());
        System.out.println("Vida: " + brotalamo.getVida() + "/" + brotalamo.getVidaMaxima());
        
        // Comparar probabilidades con diferentes pokebolas
        double probPokeball = captura.calcularProbabilidadCaptura(brotalamo, "Pokeball");
        double probEXP = captura.calcularProbabilidadCaptura(brotalamo, "PokeballEXP");
        double probCura = captura.calcularProbabilidadCaptura(brotalamo, "PokeballCura");
        
        System.out.println("\nProbabilidades de captura:");
        System.out.println("Pokeball: " + String.format("%.1f%%", probPokeball * 100));
        System.out.println("PokeballEXP: " + String.format("%.1f%%", probEXP * 100) + " (+20%)");
        System.out.println("PokeballCura: " + String.format("%.1f%%", probCura * 100) + " (+30%)");
    }
}
