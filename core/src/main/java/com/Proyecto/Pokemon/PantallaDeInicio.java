package com.Proyecto.Pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Input.Keys;

/**
 * Representa la pantalla inicial del juego (Splash/Title Screen).
 * Muestra el fondo (título) y un botón parpadeante para iniciar el menú
 * principal.
 */
public class PantallaDeInicio implements Screen {

    private Main game;
    private Texture fondoPantalla, boton;

    // --- CONSTANTES PARA LA LÓGICA DE PARPADEO ---
    private float tiempoAcumulado = 0;
    private final float intervaloParpadeo = 0.5f; // El botón cambia de estado cada 0.5 segundos.
    private boolean esVisible = true; // El botón empieza visible.

    // --- CONSTANTES DE POSICION Y TAMANO RELATIVO (Responsivas) ---
    // Definen el tamaño del botón como un porcentaje de la pantalla.
    private static final float BUTTON_WIDTH_FACTOR = 0.28f; // 30% del ancho de la pantalla.
    private static final float BUTTON_HEIGHT_FACTOR = 0.10f; // 10% del alto de la pantalla.

    // Posición Vertical del botón (20% desde la parte inferior).
    private static final float BUTTON_Y_POS = 0.02f;

    public PantallaDeInicio(Main game) {
        this.game = game;

        // Carga de recursos (deben estar en la carpeta 'assets').
        fondoPantalla = new Texture(Gdx.files.internal("pantalladeinicio.jpg"));
        boton = new Texture(Gdx.files.internal("presionecualquierboton.png"));
    }

    // --- MÉTODOS DE LA INTERFAZ SCREEN ---

    @Override
    public void show() {
    }

    /**
     * El bucle principal de la pantalla, llamado continuamente para actualizar y
     * dibujar.
     * 
     * @param delta El tiempo transcurrido desde el último frame.
     */
    @Override
    public void render(float delta) {
        // Limpieza de pantalla (establecer color y limpiar el buffer).
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- LOGICA DE TIEMPO PARA EL PARPADEO ---
        tiempoAcumulado += delta;

        // Si el tiempo acumulado supera el intervalo, cambiamos el estado de
        // visibilidad.
        if (tiempoAcumulado >= intervaloParpadeo) {
            esVisible = !esVisible;
            tiempoAcumulado = 0;
        }

        // --- CALCULO DE DIMENSIONES Y POSICION RESPONSIVA ---
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Calcular tamaño del botón basado en los factores relativos.
        float anchoBoton = screenWidth * BUTTON_WIDTH_FACTOR;
        float altoBoton = screenHeight * BUTTON_HEIGHT_FACTOR;

        // Calcular la posición X para que el botón esté CENTRADO.
        // Se calcula: (Ancho Total / 2) - (Ancho del Botón / 2)
        float posX = (screenWidth / 2) - (anchoBoton / 2);

        // Calcular la posición Y relativa (20% desde el borde inferior).
        float posY = screenHeight * BUTTON_Y_POS;

        // --- INICIO DEL DIBUJO ---
        game.batch.begin();

        // 1. Dibuja el Fondo (Responsivo: ocupa el 100% del ancho y alto).
        game.batch.draw(fondoPantalla, 0, 0, screenWidth, screenHeight);

        // 2. Dibuja el botón parpadeante (solo si es visible).
        if (esVisible) {
            // Usamos las posiciones y dimensiones calculadas (responsivas).
            game.batch.draw(boton, posX, posY, anchoBoton, altoBoton);
        }

        game.batch.end();

        // --- LÓGICA DE ENTRADA: CAMBIO DE PANTALLA ---
        if (Gdx.input.isKeyJustPressed(Keys.ANY_KEY) || Gdx.input.justTouched()) {
            // Cambiar al menú principal
            game.setScreen(new MenuPrincipal(game));
            dispose(); // Liberar los recursos de esta pantalla
        }
    }

    // Llamado cuando la ventana cambia de tamaño.
    @Override
    public void resize(int width, int height) {
    }

    // Llamado cuando la aplicación pierde el foco.
    @Override
    public void pause() {
    }

    // Llamado cuando la aplicación recupera el foco.
    @Override
    public void resume() {
    }

    // Llamado cuando se establece otra pantalla.
    @Override
    public void hide() {
    }

    /**
     * Método dispose: CRÍTICO. Se llama justo antes de que la pantalla se destruya.
     * Libera todos los recursos (Texturas) de la memoria para evitar fugas.
     */
    @Override
    public void dispose() {
        fondoPantalla.dispose();
        boton.dispose();
    }
}
