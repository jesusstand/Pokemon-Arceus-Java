package com.Proyecto.Pokemon.sistema;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

/**
 * Gestiona la reproducción de música del juego.
 * Controla qué música está reproduciéndose y permite cambiar entre diferentes pistas.
 */
public class GestorMusica {
    
    private static Music musicaActual = null;
    private static String musicaActualNombre = null;
    private static float volumen = 0.7f; // Volumen por defecto (70%)
    private static boolean musicaHabilitada = true;
    
    /**
     * Tipos de música disponibles en el juego.
     */
    public enum TipoMusica {
        BATALLA("Musica/BatallaM.mp3"),
        MAPA_VERDE("Musica/VerdeM.mp3"),
        MAPA_AZUL("Musica/AzulM.mp3"),
        MENU("Musica/MenuM.mp3"),
        GANADOR("Musica/GanadorM.mp3");
        
        private final String ruta;
        
        TipoMusica(String ruta) {
            this.ruta = ruta;
        }
        
        public String getRuta() {
            return ruta;
        }
    }
    
    /**
     * Reproduce una música específica.
     * Si ya hay una música reproduciéndose, la detiene y reproduce la nueva.
     * 
     * @param tipo Tipo de música a reproducir.
     */
    public static void reproducirMusica(TipoMusica tipo) {
        if (!musicaHabilitada) {
            return;
        }
        
        String ruta = tipo.getRuta();
        
        // Si ya está reproduciendo esta música, no hacer nada
        if (musicaActual != null && ruta.equals(musicaActualNombre) && musicaActual.isPlaying()) {
            return;
        }
        
        // Detener la música actual si existe
        detenerMusica();
        
        try {
            // Cargar y reproducir la nueva música
            FileHandle archivoMusica = Gdx.files.internal(ruta);
            if (archivoMusica.exists()) {
                musicaActual = Gdx.audio.newMusic(archivoMusica);
                musicaActual.setVolume(volumen);
                musicaActual.setLooping(true); // Reproducir en loop
                musicaActual.play();
                musicaActualNombre = ruta;
                System.out.println("Reproduciendo música: " + tipo.name());
            } else {
                System.err.println("No se encontró el archivo de música: " + ruta);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar música: " + ruta);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Detiene la música actual si está reproduciéndose.
     */
    public static void detenerMusica() {
        if (musicaActual != null) {
            if (musicaActual.isPlaying()) {
                musicaActual.stop();
            }
            musicaActual.dispose();
            musicaActual = null;
            musicaActualNombre = null;
        }
    }
    
    /**
     * Pausa la música actual si está reproduciéndose.
     */
    public static void pausarMusica() {
        if (musicaActual != null && musicaActual.isPlaying()) {
            musicaActual.pause();
        }
    }
    
    /**
     * Reanuda la música actual si está pausada.
     */
    public static void reanudarMusica() {
        if (musicaActual != null && !musicaActual.isPlaying()) {
            musicaActual.play();
        }
    }
    
    /**
     * Establece el volumen de la música.
     * 
     * @param nuevoVolumen Volumen entre 0.0f (silencioso) y 1.0f (máximo).
     */
    public static void setVolumen(float nuevoVolumen) {
        volumen = Math.max(0.0f, Math.min(1.0f, nuevoVolumen));
        if (musicaActual != null) {
            musicaActual.setVolume(volumen);
        }
    }
    
    /**
     * Obtiene el volumen actual.
     * 
     * @return Volumen actual (0.0f a 1.0f).
     */
    public static float getVolumen() {
        return volumen;
    }
    
    /**
     * Habilita o deshabilita la música.
     * 
     * @param habilitada true para habilitar, false para deshabilitar.
     */
    public static void setMusicaHabilitada(boolean habilitada) {
        musicaHabilitada = habilitada;
        if (!habilitada) {
            detenerMusica();
        }
    }
    
    /**
     * Verifica si la música está habilitada.
     * 
     * @return true si está habilitada, false en caso contrario.
     */
    public static boolean isMusicaHabilitada() {
        return musicaHabilitada;
    }
    
    /**
     * Limpia los recursos de música.
     * Debe llamarse al cerrar el juego.
     */
    public static void dispose() {
        detenerMusica();
    }
}
