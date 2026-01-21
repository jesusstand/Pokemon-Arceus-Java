package com.Proyecto.Pokemon.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;

/**
 * Gestor que carga y almacena las texturas de los sprites de Pokémon.
 * Soporta múltiples frames de animación (3 frente, 3 atrás) para batallas.
 */
public class GestorSpritesPokemon implements Disposable {

    /**
     * Enum para la dirección del sprite (frente o atrás).
     */
    public enum DireccionSprite {
        FRENTE, ATRAS
    }

    // Mapa de texturas: "NombrePokemon_Frente_0" -> Texture
    private HashMap<String, Texture> texturasPokemon;

    /**
     * Constructor del gestor de sprites.
     */
    public GestorSpritesPokemon() {
        this.texturasPokemon = new HashMap<>();
    }

    /**
     * Obtiene la textura de un Pokémon en una dirección y frame específicos.
     * 
     * @param nombrePokemon Nombre del Pokémon.
     * @param direccion     Dirección del sprite (FRENTE o ATRAS).
     * @param frame         Número del frame de animación (0, 1 o 2).
     * @return Texture del sprite del Pokémon, o null si no existe.
     */
    public Texture obtenerSprite(String nombrePokemon, DireccionSprite direccion, int frame) {
        // Validar frame (debe ser 0, 1 o 2)
        if (frame < 0 || frame > 2) {
            System.err.println("Frame inválido: " + frame + ". Debe ser 0, 1 o 2.");
            return null;
        }

        String clave = generarClave(nombrePokemon, direccion, frame);

        // Si ya está cargado, retornarlo
        Texture texture = texturasPokemon.get(clave);
        if (texture != null) {
            return texture;
        }

        // Intentar cargar el sprite personalizado
        // Primero intentar con el nombre exacto
        String nombreArchivo = generarNombreArchivo(nombrePokemon, direccion, frame);
        try {
            if (Gdx.files.internal(nombreArchivo).exists()) {
                texture = new Texture(Gdx.files.internal(nombreArchivo));
                texturasPokemon.put(clave, texture);
                return texture;
            }
        } catch (Exception e) {
            // Continuar con el siguiente intento
        }

        // Si falla, intentar con el nombre normalizado (sin acentos)
        // Esto ayuda cuando el nombre del pokémon tiene acentos pero la carpeta no, o viceversa
        String nombreNormalizado = normalizarNombre(nombrePokemon);
        if (!nombreNormalizado.equals(nombrePokemon.toLowerCase())) {
            String nombreArchivoAlt = generarNombreArchivo(nombreNormalizado.substring(0, 1).toUpperCase() + nombreNormalizado.substring(1), direccion, frame);
            try {
                if (Gdx.files.internal(nombreArchivoAlt).exists()) {
                    texture = new Texture(Gdx.files.internal(nombreArchivoAlt));
                    texturasPokemon.put(clave, texture);
                    return texture;
                }
            } catch (Exception e) {
                // Continuar
            }
        }

        // Si no existe el sprite personalizado, retornar null
        System.err.println("No se encontró sprite: " + nombreArchivo);
        return null;
    }

    /**
     * Obtiene el sprite de frente por defecto (frame 0).
     * Para uso en inventarios y encuentros donde no se necesita animación.
     * 
     * @param nombrePokemon Nombre del Pokémon.
     * @return Texture del sprite frontal frame 0, o null si no existe.
     */
    public Texture obtenerSprite(String nombrePokemon) {
        return obtenerSprite(nombrePokemon, DireccionSprite.FRENTE, 0);
    }

    /**
     * Genera la clave única para almacenar una textura.
     */
    private String generarClave(String nombrePokemon, DireccionSprite direccion, int frame) {
        return nombrePokemon + "_" + direccion.name() + "_" + frame;
    }

    /**
     * Genera el nombre del archivo PNG según la convención.
     * Formato: "NombrePokemon_Frente_0.png" o "NombrePokemon_Atras_2.png"
     * Intenta primero con el nombre exacto, luego con variaciones si falla.
     */
    private String generarNombreArchivo(String nombrePokemon, DireccionSprite direccion, int frame) {
        String direccionStr = (direccion == DireccionSprite.FRENTE) ? "Frente" : "Atras";
        // Los sprites ahora están organizados por Pokémon:
        // assets/pokemon/<NombrePokemon>/<NombrePokemon>_Frente_0.png
        return "pokemon/" + nombrePokemon + "/" + nombrePokemon + "_" + direccionStr + "_" + frame + ".png";
    }

    /**
     * Normaliza el nombre del pokémon removiendo acentos para buscar variaciones.
     */
    private String normalizarNombre(String nombre) {
        return nombre.toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ñ", "n");
    }

    /**
     * Obtiene el sprite de frente para un frame específico de animación.
     * Útil para animaciones en batallas.
     */
    public Texture obtenerSpriteFrente(String nombrePokemon, int frame) {
        return obtenerSprite(nombrePokemon, DireccionSprite.FRENTE, frame);
    }

    /**
     * Obtiene el sprite de atrás para un frame específico de animación.
     * Útil para mostrar tu Pokémon en batallas.
     */
    public Texture obtenerSpriteAtras(String nombrePokemon, int frame) {
        return obtenerSprite(nombrePokemon, DireccionSprite.ATRAS, frame);
    }

    /**
     * Comprueba si una textura está en el mapa de texturas gestionadas.
     */
    public boolean texturasPokemonContains(Texture texture) {
        return texturasPokemon.containsValue(texture);
    }

    /**
     * Libera todos los recursos de las texturas cargadas.
     */
    @Override
    public void dispose() {
        for (Texture texture : texturasPokemon.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        texturasPokemon.clear();
    }
}
