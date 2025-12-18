package com.Proyecto.Pokemon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Clase principal que inicializa el juego y gestiona el ciclo de vida de la
 * aplicacion.
 * Extiende de {@link com.badlogic.gdx.Game}.
 */
public class Main extends Game {
    /** SpriteBatch utilizado para dibujar texturas en pantalla. */
    public SpriteBatch batch;

    @Override
    public void create() {
        // Se inicializa el SpriteBatch para el dibujado
        batch = new SpriteBatch();

        // Establece la pantalla inicial del juego
        setScreen(new PantallaDeInicio(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
