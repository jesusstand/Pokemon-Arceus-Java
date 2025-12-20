package com.Proyecto.Pokemon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Clase principal que inicializa el juego y gestiona el ciclo de vida de la
 * aplicacion.
 * Extiende de {@link com.badlogic.gdx.Game} para manejar diferentes pantallas.
 */
public class Main extends Game {
    /**
     * SpriteBatch utilizado para dibujar todas las texturas en pantalla.
     * Se declara publico para que las diferentes pantallas puedan acceder a el.
     */
    public SpriteBatch batch;

    @Override
    public void create() {
        // Se inicializa el SpriteBatch para el dibujado de graficos.
        batch = new SpriteBatch();

        // Se establece la pantalla inicial del juego, que es la pantalla de
        // presentacion.
        setScreen(new PantallaDeInicio(this));
    }

    @Override
    public void dispose() {
        // Se liberan los recursos del SpriteBatch al cerrar la aplicacion.
        batch.dispose();
    }
}
