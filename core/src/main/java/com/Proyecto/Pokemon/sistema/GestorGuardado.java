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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Gestiona el guardado y carga de partidas en Progreso.txt.
 */
public class GestorGuardado {
    private static final String ARCHIVO_GUARDADO = "Progreso.txt";
    private static final Json json = new Json();

    static {
        json.setOutputType(JsonWriter.OutputType.json);
    }

    /**
     * Datos individuales de un Pokemon para guardar.
     */
    public static class DatosPokemon {
        public String nombre;
        public String tipo;
        public String genero;
        public int vida;

        public DatosPokemon() {
        }

        public DatosPokemon(Pokemon p) {
            this.nombre = p.getNombre();
            this.tipo = p.getTipo().name();
            this.genero = p.getSexo();
            this.vida = p.getVida();
        }
    }

    /**
     * Datos de la partida guardada.
     */
    public static class DatosPartida {
        public float posicionX;
        public float posicionY;
        public HashMap<String, Integer> inventario;
        public HashMap<String, Integer> investigacion;
        public List<DatosPokemon> equipo;
        public String mapaActual;

        public DatosPartida() {
            this.equipo = new ArrayList<>();
            this.investigacion = new HashMap<>(); // Inicializar para evitar nulos
        }

        public DatosPartida(Player jugador, String mapa) {
            this.posicionX = jugador.getX();
            this.posicionY = jugador.getY();
            this.inventario = new HashMap<>(jugador.getInventario().getMapa());
            this.investigacion = new HashMap<>(jugador.getMapaPuntosInvestigacion());
            this.mapaActual = mapa;
            this.equipo = new ArrayList<>();

            // Guardar todos los Pokemon del equipo
            for (Pokemon p : jugador.getEquipo().getPokemons()) {
                this.equipo.add(new DatosPokemon(p));
            }
        }
    }

    public static boolean guardarPartida(Player jugador, String mapaActual) {
        try {
            DatosPartida datos = new DatosPartida(jugador, mapaActual);
            FileHandle archivo = Gdx.files.local(ARCHIVO_GUARDADO);
            archivo.writeString(json.prettyPrint(datos), false);
            System.out.println("Partida guardada en: " + ARCHIVO_GUARDADO);
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar: " + e.getMessage());
            return false;
        }
    }

    public static DatosPartida cargarPartida() {
        try {
            FileHandle archivo = Gdx.files.local(ARCHIVO_GUARDADO);
            if (!archivo.exists())
                return null;
            return json.fromJson(DatosPartida.class, archivo.readString());
        } catch (Exception e) {
            System.err.println("Error al cargar: " + e.getMessage());
            return null;
        }
    }

    public static boolean existePartidaGuardada() {
        return Gdx.files.local(ARCHIVO_GUARDADO).exists();
    }

    public static boolean eliminarPartidaGuardada() {
        FileHandle archivo = Gdx.files.local(ARCHIVO_GUARDADO);
        if (archivo.exists()) {
            archivo.delete();
            return true;
        }
        return false;
    }

    public static Pokemon recrearPokemon(DatosPokemon datos) {
        if (datos == null)
            return null;

        try {
            Tipo tipo = Tipo.valueOf(datos.tipo);
            String genero = datos.genero != null ? datos.genero : "Macho";
            String nombre = datos.nombre.toLowerCase();
            Pokemon p = null;

            if (tipo == Tipo.FUEGO) {
                if (nombre.contains("ignirrojo"))
                    p = new PokeFuego.Ignirrojo(genero);
                else if (nombre.contains("volcarex"))
                    p = new PokeFuego.Volcarex(genero);
            } else if (tipo == Tipo.AGUA) {
                if (nombre.contains("aqualisca"))
                    p = new PokeAgua.Aqualisca(genero);
                else if (nombre.contains("mareonix"))
                    p = new PokeAgua.Mareonix(genero);
            } else if (tipo == Tipo.PLANTA) {
                if (nombre.contains("brotalamo"))
                    p = new PokePlanta.Brotalamo(genero);
                else if (nombre.contains("floravelo"))
                    p = new PokePlanta.Floravelo(genero);
            }

            if (p == null) {
                // Fallback por tipo
                if (tipo == Tipo.FUEGO)
                    p = new PokeFuego.Ignirrojo(genero);
                else if (tipo == Tipo.AGUA)
                    p = new PokeAgua.Aqualisca(genero);
                else
                    p = new PokePlanta.Brotalamo(genero);
            }

            // Restaurar vida
            p.curar();
            int danio = p.getVidaMaxima() - datos.vida;
            if (danio > 0)
                p.recibirDanio(danio);

            return p;
        } catch (Exception e) {
            return null;
        }
    }

    public static void restaurarEquipo(Player jugador, DatosPartida datos) {
        if (datos == null || datos.equipo == null || jugador == null)
            return;
        jugador.getEquipo().limpiar();
        for (DatosPokemon dp : datos.equipo) {
            Pokemon p = recrearPokemon(dp);
            if (p != null)
                jugador.getEquipo().agregarPokemon(p);
        }
    }

    public static void restaurarInventario(Player jugador, DatosPartida datos) {
        if (datos == null || datos.inventario == null || jugador == null)
            return;
        jugador.getInventario().getMapa().clear();
        jugador.getInventario().getMapa().putAll(datos.inventario);
    }

    public static void restaurarInvestigacion(Player jugador, DatosPartida datos) {
        if (datos == null || datos.investigacion == null || jugador == null)
            return;
        jugador.getMapaPuntosInvestigacion().clear();
        jugador.getMapaPuntosInvestigacion().putAll(datos.investigacion);
    }
}
