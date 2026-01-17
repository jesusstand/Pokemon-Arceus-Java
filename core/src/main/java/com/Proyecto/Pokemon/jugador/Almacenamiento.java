package com.Proyecto.Pokemon.jugador;

import com.Proyecto.Pokemon.excepciones.ExcepcionInventarioLleno;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona el inventario del jugador, almacenando objetos y sus cantidades.
 */
public class Almacenamiento {
    private HashMap<String, Integer> inventario;

    private static final int MAX_ITEMS = 10;

    public Almacenamiento() {
        this.inventario = new HashMap<>();
    }

    /**
     * Agrega un objeto al inventario o incrementa su cantidad si ya existe.
     * 
     * @param nombre    Nombre del objeto recogido.
     * @param categoria Categoria del objeto (pokeball o item).
     * @throws ExcepcionInventarioLleno Si el objeto excede el limite de 10.
     */
    public void agregarObjeto(String nombre, String categoria) throws ExcepcionInventarioLleno {
        if (nombre == null || nombre.isEmpty()) {
            nombre = "Objeto Desconocido";
        }

        int cantidadActual = inventario.getOrDefault(nombre, 0);

        if (cantidadActual >= MAX_ITEMS) {
            throw new ExcepcionInventarioLleno(
                    "Inventario lleno: No puedes llevar mas de " + MAX_ITEMS + " " + nombre + "s.");
        }

        inventario.put(nombre, cantidadActual + 1);

        System.out.println("[Inventario] +1 " + nombre + " (" + categoria + ")");
    }

    /**
     * Imprime el contenido del inventario en la terminal.
     */
    public void imprimirInventario() {
        System.out.println("\n--- INVENTARIO ACTUAL ---");
        if (inventario.isEmpty()) {
            System.out.println("El inventario esta vacio.");
        } else {
            for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
                System.out.println("- " + entry.getKey() + ": x" + entry.getValue());
            }
        }
        System.out.println("-------------------------\n");
    }

    /**
     * Devuelve el mapa de inventario para su lectura.
     */
    public HashMap<String, Integer> getMapa() {
        return inventario;
    }
}
