package com.Proyecto.Pokemon.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.Proyecto.Pokemon.Main;

/** Lanza la aplicacion de escritorio (LWJGL3). */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired())
            return; // Maneja el soporte para macOS y ayuda en Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Pokemon");

        // El Vsync limita los cuadros por segundo a los que el hardware puede mostrar y
        // ayuda a eliminar el desgarro de pantalla.
        configuration.useVsync(true);

        // Limita los cuadros por segundo a la tasa de refresco del monitor activo
        // actual mas 1.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

        // Establece el tamaño de la ventana de juego.
        configuration.setWindowedMode(960, 600);

        // Iconos de la aplicacion (deben estar en la carpeta assets).
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        // Mejora la compatibilidad con controladores OpenGL problematicos en Windows y
        // macOS.
        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        return configuration;
    }
}
