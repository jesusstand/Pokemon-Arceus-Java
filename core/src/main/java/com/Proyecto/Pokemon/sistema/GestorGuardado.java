package com.Proyecto.Pokemon.sistema;

import com.Proyecto.Pokemon.jugador.Player;
import com.Proyecto.Pokemon.pokemon.Pokemon;
import com.Proyecto.Pokemon.pokemon.PokeFuego;
import com.Proyecto.Pokemon.pokemon.PokeAgua;
import com.Proyecto.Pokemon.pokemon.PokePlanta;
import com.Proyecto.Pokemon.pokemon.Tipo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.HashMap;

/**
 * Gestiona el guardado y carga de partidas.
 */
public class GestorGuardado {
    private static final String ARCHIVO_GUARDADO = "partida_guardada.json";
    private static final Json json = new Json();
    
    static {
        // Configurar JSON para que sea legible
        json.setOutputType(JsonWriter.OutputType.json);
    }
    
    /**
     * Datos de la partida guardada.
     */
    public static class DatosPartida {
        public float posicionX;
        public float posicionY;
        public HashMap<String, Integer> inventario;
        public String nombrePokemon;
        public String tipoPokemon;
        public String generoPokemon;
        public String mapaActual;
        
        public DatosPartida() {
            // Constructor vacío para JSON
        }
        
        public DatosPartida(Player jugador, Pokemon pokemon, String mapa) {
            this.posicionX = jugador.getX();
            this.posicionY = jugador.getY();
            this.inventario = new HashMap<>(jugador.getInventario().getMapa());
            this.nombrePokemon = pokemon.getNombre();
            this.tipoPokemon = pokemon.getTipo().name();
            this.generoPokemon = pokemon.getSexo();
            this.mapaActual = mapa;
        }
    }
    
    /**
     * Guarda el estado actual de la partida.
     * 
     * @param jugador El jugador actual.
     * @param pokemon El Pokémon inicial del jugador.
     * @param mapaActual El nombre del mapa actual (sin extensión .tmx).
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static boolean guardarPartida(Player jugador, Pokemon pokemon, String mapaActual) {
        try {
            DatosPartida datos = new DatosPartida(jugador, pokemon, mapaActual);
            
            // Usar FileHandle local para guardar en el directorio local del usuario
            FileHandle archivo = Gdx.files.local(ARCHIVO_GUARDADO);
            String jsonString = json.prettyPrint(datos);
            archivo.writeString(jsonString, false);
            
            System.out.println("Partida guardada correctamente en: " + archivo.path());
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar la partida: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Carga una partida guardada.
     * 
     * @return Los datos de la partida guardada, o null si no existe o hay error.
     */
    public static DatosPartida cargarPartida() {
        try {
            FileHandle archivo = Gdx.files.local(ARCHIVO_GUARDADO);
            
            if (!archivo.exists()) {
                System.out.println("No existe partida guardada.");
                return null;
            }
            
            String jsonString = archivo.readString();
            DatosPartida datos = json.fromJson(DatosPartida.class, jsonString);
            
            System.out.println("Partida cargada correctamente desde: " + archivo.path());
            return datos;
        } catch (Exception e) {
            System.err.println("Error al cargar la partida: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Verifica si existe una partida guardada.
     * 
     * @return true si existe una partida guardada, false en caso contrario.
     */
    public static boolean existePartidaGuardada() {
        FileHandle archivo = Gdx.files.local(ARCHIVO_GUARDADO);
        return archivo.exists();
    }
    
    /**
     * Elimina la partida guardada.
     * 
     * @return true si se eliminó correctamente, false en caso contrario.
     */
    public static boolean eliminarPartidaGuardada() {
        try {
            FileHandle archivo = Gdx.files.local(ARCHIVO_GUARDADO);
            if (archivo.exists()) {
                archivo.delete();
                System.out.println("Partida guardada eliminada.");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error al eliminar la partida guardada: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Recrea un Pokemon desde los datos guardados.
     * 
     * @param datos Los datos de la partida guardada.
     * @return El Pokemon recreado, o null si hay error.
     */
    public static Pokemon recrearPokemon(DatosPartida datos) {
        if (datos == null) {
            return null;
        }
        
        try {
            String nombre = datos.nombrePokemon;
            String genero = datos.generoPokemon != null ? datos.generoPokemon : "Macho";
            Tipo tipo = null;
            
            // Intentar obtener el tipo desde el string guardado
            try {
                tipo = Tipo.valueOf(datos.tipoPokemon);
            } catch (IllegalArgumentException e) {
                System.err.println("Tipo de Pokemon inválido: " + datos.tipoPokemon);
                return null;
            }
            
            // Recrear el Pokemon según su nombre
            if (nombre == null || nombre.isEmpty()) {
                System.err.println("Nombre de Pokemon no válido.");
                return null;
            }
            
            // Crear el Pokemon según su nombre (case-insensitive)
            String nombreLower = nombre.toLowerCase();
            
            if (tipo == Tipo.FUEGO) {
                if (nombreLower.equals("ignirrojo")) {
                    return new PokeFuego.Ignirrojo(genero);
                } else if (nombreLower.equals("volcárex") || nombreLower.equals("volcarex")) {
                    return new PokeFuego.Volcarex(genero);
                }
            } else if (tipo == Tipo.AGUA) {
                if (nombreLower.equals("aqualisca")) {
                    return new PokeAgua.Aqualisca(genero);
                } else if (nombreLower.equals("mareónix") || nombreLower.equals("mareonix")) {
                    return new PokeAgua.Mareonix(genero);
                }
            } else if (tipo == Tipo.PLANTA) {
                if (nombreLower.equals("brotálamo") || nombreLower.equals("brotalamo")) {
                    return new PokePlanta.Brotalamo(genero);
                } else if (nombreLower.equals("floravelo")) {
                    return new PokePlanta.Floravelo(genero);
                }
            }
            
            // Si no se encuentra el Pokemon específico, crear uno por defecto según el tipo
            System.out.println("Pokemon específico no encontrado: " + nombre + ", usando default para tipo " + tipo);
            switch (tipo) {
                case FUEGO:
                    return new PokeFuego.Ignirrojo(genero);
                case AGUA:
                    return new PokeAgua.Aqualisca(genero);
                case PLANTA:
                    return new PokePlanta.Brotalamo(genero);
                default:
                    return new PokeFuego.Ignirrojo(genero); // Fallback
            }
        } catch (Exception e) {
            System.err.println("Error al recrear Pokemon: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Restaura el inventario del jugador desde los datos guardados.
     * 
     * @param jugador El jugador al que se le restaurará el inventario.
     * @param datos Los datos de la partida guardada.
     */
    public static void restaurarInventario(Player jugador, DatosPartida datos) {
        if (datos == null || datos.inventario == null || jugador == null) {
            return;
        }
        
        try {
            // Limpiar el inventario actual
            HashMap<String, Integer> inventarioActual = jugador.getInventario().getMapa();
            inventarioActual.clear();
            
            // Restaurar desde los datos guardados
            inventarioActual.putAll(datos.inventario);
            
            System.out.println("Inventario restaurado con " + inventarioActual.size() + " tipos de objetos.");
        } catch (Exception e) {
            System.err.println("Error al restaurar inventario: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
