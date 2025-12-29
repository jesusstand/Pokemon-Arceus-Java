package com.Proyecto.Pokemon;

/**
 * Clase de ejemplo que muestra cómo usar el sistema de batalla Pokemon.
 * Esta clase puede ser eliminada, es solo para referencia.
 */
public class EjemploBatalla {
    
    public static void ejemploBatalla() {
        // Crear Pokemon para la batalla
        PokeFuego ignirrojo = new PokeFuego.Ignirrojo("Macho");
        PokePlanta brotalamo = new PokePlanta.Brotalamo("Hembra");
        
        System.out.println("=== BATALLA POKEMON ===\n");
        System.out.println(ignirrojo);
        System.out.println(brotalamo);
        System.out.println();
        
        // Crear batalla
        Batalla batalla = new Batalla(ignirrojo, brotalamo);
        
        // Simular batalla hasta que termine
        int turno = 1;
        while (!batalla.estaTerminada()) {
            System.out.println("--- Turno " + turno + " ---");
            System.out.println(batalla.realizarAtaque());
            System.out.println();
            turno++;
        }
        
        // Mostrar ganador
        Pokemon ganador = batalla.obtenerGanador();
        if (ganador != null) {
            System.out.println("¡" + ganador.getNombre() + " es el ganador!");
        }
    }
    
    public static void ejemploEfectividad() {
        System.out.println("\n=== DEMOSTRACION DE EFECTIVIDAD ===\n");
        
        // Fuego vs Planta (super efectivo)
        PokeFuego volcarex = new PokeFuego.Volcarex("Macho");
        PokePlanta floravelo = new PokePlanta.Floravelo("Hembra");
        
        System.out.println("Fuego ataca a Planta:");
        volcarex.lanzallamas(floravelo);
        System.out.println("Vida de " + floravelo.getNombre() + ": " + 
                          floravelo.getVida() + "/" + floravelo.getVidaMaxima());
        
        // Restaurar vida para siguiente ejemplo
        floravelo.curar();
        
        // Planta vs Fuego (no efectivo)
        System.out.println("\nPlanta ataca a Fuego:");
        floravelo.hojaAfilada(volcarex);
        System.out.println("Vida de " + volcarex.getNombre() + ": " + 
                          volcarex.getVida() + "/" + volcarex.getVidaMaxima());
    }
}

