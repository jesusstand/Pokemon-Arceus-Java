package com.Proyecto.Pokemon.gui;

import com.Proyecto.Pokemon.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Input.Keys;

/**
 * Representa la primera pantalla de carga o presentacion (Splash Screen).
 * Muestra una imagen de fondo y un mensaje parpadeante para que el usuario
 * inicie.
 */
public class PantallaDeInicio implements Screen {

    private Main game;
    private Texture fondoPantalla, boton;

    // --- VARIABLES PARA EL EFECTO DE PARPADEO ---
    private float tiempoAcumulado = 0;
    private final float intervaloParpadeo = 0.5f; // Cambia el estado cada medio segundo.
    private boolean esVisible = true;

    // --- FACTORES DE ESCALA RESPONSIVOS ---
    private static final float BUTTON_WIDTH_FACTOR = 0.28f;
    private static final float BUTTON_HEIGHT_FACTOR = 0.10f;
    private static final float BUTTON_Y_POS = 0.02f; // Altura desde el fondo de la pantalla.

    /**
     * Constructor que inicializa las texturas de la pantalla inicial.
     * 
     * @param game Instancia principal del juego.
     */
    public PantallaDeInicio(Main game) {
        this.game = game;

        // Cargamos las imagenes desde la carpeta de recursos (assets).
        fondoPantalla = new Texture(Gdx.files.internal("pantalladeinicio.jpg"));
        boton = new Texture(Gdx.files.internal("presionecualquierboton.png"));
    }

    @Override
    public void show() {
    }

    /**
     * Metodo render que se encarga de la logica de parpadeo y del dibujado de la
     * pantalla.
     * 
     * @param delta Tiempo transcurrido entre frames.
     */
    @Override
    public void render(float delta) {
        // Limpiamos el fondo en negro.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizamos logica de parpadeo.
        tiempoAcumulado += delta;
        if (tiempoAcumulado >= intervaloParpadeo) {
            esVisible = !esVisible;
            tiempoAcumulado = 0;
        }

        // Calculamos posiciones y tamaños relativos a la resolucion actual.
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float anchoBoton = screenWidth * BUTTON_WIDTH_FACTOR;
        float altoBoton = screenHeight * BUTTON_HEIGHT_FACTOR;
        float posX = (screenWidth / 2) - (anchoBoton / 2); // Centrado horizontal.
        float posY = screenHeight * BUTTON_Y_POS;

        // Iniciamos el proceso de dibujado.
        game.batch.begin();

        // 1. Dibujamos el fondo ocupando toda la ventana.
        game.batch.draw(fondoPantalla, 0, 0, screenWidth, screenHeight);

        // 2. Dibujamos el mensaje solo si toca por el parpadeo.
        if (esVisible) {
            game.batch.draw(boton, posX, posY, anchoBoton, altoBoton);
        }

        game.batch.end();

        // Si se pulsa cualquier tecla o la pantalla, pasamos al menu principal.
        if (Gdx.input.isKeyJustPressed(Keys.ANY_KEY) || Gdx.input.justTouched()) {
            game.setScreen(new MenuPrincipal(game));
            dispose(); // Liberamos los recursos de esta pantalla inmediatamente.
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    /**
     * Libera las texturas cargadas al cerrar esta pantalla para ahorrar memoria.
     */
    @Override
    public void dispose() {
        fondoPantalla.dispose();
        boton.dispose();
    }
}
