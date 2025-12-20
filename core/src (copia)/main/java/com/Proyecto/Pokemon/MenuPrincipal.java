package com.Proyecto.Pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;

/**
 * Representa la pantalla del menu principal del juego.
 * Controla la navegacion entre las opciones de Partida Individual,
 * Multijugador, Opciones y Salir.
 * Incluye logica responsiva para ajustar botones segun el tamaño de la ventana.
 */
public class MenuPrincipal implements Screen {

    private Main game;
    // Texturas para el fondo y los botones en sus diferentes estados.
    private Texture fondoMenu;
    private Texture partidaIndividual, partidaIndividualC, salirMenu, salirMenuC, multiplayer, multiplayerC,
            opcionesMenu, opcionesMenuC;

    // --- CONSTANTES DEL ESTADO DEL MENU ---
    private static final int PARTIDA_INDIVIDUAL = 0;
    private static final int MULTIPLAYER = 1;
    private static final int OPCIONES_MENU = 2;
    private static final int SALIR = 3;
    private static final int OPCIONES = 4; // Cantidad total de botones.

    private int opcionSeleccionada; // Indice de la opcion que el usuario tiene destacada actualmente.

    // --- CONSTANTES DE DISENO RESPONSIVO ---
    private static final float BUTTON_X_POS = 0.08f; // Posicion X (8% del ancho).
    private static final float BUTTON_Y_PARTIDA_INDIVIDUAL = 0.70f;
    private static final float BUTTON_Y_MULTIPLAYER = 0.55f;
    private static final float BUTTON_Y_OPCIONES = 0.40f;
    private static final float BUTTON_Y_SALIR = 0.25f;

    private static final float BUTTON_WIDTH_FACTOR = 0.35f; // Ancho base (35% del ancho de pantalla).
    private static final float BUTTON_HEIGHT_FACTOR = 0.10f; // Alto base (10% del alto de pantalla).

    /**
     * Constructor del menu. Carga todas las texturas necesarias desde assets.
     *
     * @param game Instancia principal para cambiar pantallas.
     */
    public MenuPrincipal(Main game) {
        this.game = game;

        // Carga de imagenes de fondo y botones (normales y resaltados con 'C').
        fondoMenu = new Texture(Gdx.files.internal("arceus.jpg"));
        partidaIndividual = new Texture(Gdx.files.internal("PartidaIndividual.png"));
        partidaIndividualC = new Texture(Gdx.files.internal("PartidaIndividualC.png"));
        salirMenu = new Texture(Gdx.files.internal("Salir.png"));
        salirMenuC = new Texture(Gdx.files.internal("SalirC.png"));
        multiplayer = new Texture(Gdx.files.internal("Multiplayer.png"));
        multiplayerC = new Texture(Gdx.files.internal("MultiplayerC.png"));
        opcionesMenu = new Texture(Gdx.files.internal("Opciones.png"));
        opcionesMenuC = new Texture(Gdx.files.internal("OpcionesC.png"));

        opcionSeleccionada = PARTIDA_INDIVIDUAL;
    }

    /**
     * Ciclo principal de dibujo y logica del menu.
     * 
     * @param delta Tiempo en segundos desde el ultimo renderizado.
     */
    @Override
    public void render(float delta) {
        // Dimensiones base segun el tamaño actual de la ventana.
        float baseWidth = Gdx.graphics.getWidth() * BUTTON_WIDTH_FACTOR;
        float baseHeight = Gdx.graphics.getHeight() * BUTTON_HEIGHT_FACTOR;
        float scaleFactor = 1.1f; // Los botones crecen un 10% cuando se seleccionan.

        // Seleccion de textura y tamaño segun el estado actual.
        Texture texturaBotonPartidaIndividual = partidaIndividual;
        Texture texturaBotonMultiplayer = multiplayer;
        Texture texturaBotonOpciones = opcionesMenu;
        Texture texturaBotonSalir = salirMenu;

        float anchoPartidaIndividual = baseWidth;
        float alturaPartidaIndividual = baseHeight;
        float anchoMultiplayer = baseWidth;
        float alturaMultiplayer = baseHeight;
        float anchoOpciones = baseWidth;
        float alturaOpciones = baseHeight;
        float anchoSalir = baseWidth;
        float alturaSalir = baseHeight;

        // Cambiamos la apariencia de la opcion seleccionada.
        if (opcionSeleccionada == PARTIDA_INDIVIDUAL) {
            texturaBotonPartidaIndividual = partidaIndividualC;
            anchoPartidaIndividual *= scaleFactor;
            alturaPartidaIndividual *= scaleFactor;
        } else if (opcionSeleccionada == MULTIPLAYER) {
            texturaBotonMultiplayer = multiplayerC;
            anchoMultiplayer *= scaleFactor;
            alturaMultiplayer *= scaleFactor;
        } else if (opcionSeleccionada == OPCIONES_MENU) {
            texturaBotonOpciones = opcionesMenuC;
            anchoOpciones *= scaleFactor;
            alturaOpciones *= scaleFactor;
        } else if (opcionSeleccionada == SALIR) {
            texturaBotonSalir = salirMenuC;
            anchoSalir *= scaleFactor;
            alturaSalir *= scaleFactor;
        }

        // Limpieza de pantalla.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- MANEJO DE ENTRADAS DEL USUARIO ---
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            opcionSeleccionada = (opcionSeleccionada - 1 + OPCIONES) % OPCIONES;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            opcionSeleccionada = (opcionSeleccionada + 1) % OPCIONES;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            // Ejecutar accion segun la opcion resaltada.
            switch (opcionSeleccionada) {
                case PARTIDA_INDIVIDUAL:
                    game.setScreen(new Mapa(game, "MapaVerdePokemon.tmx"));
                    dispose();
                    break;
                case MULTIPLAYER:
                    Gdx.app.log("MENU", "Modo Multijugador no implementado.");
                    break;
                case OPCIONES_MENU:
                    Gdx.app.log("MENU", "Opciones no implementadas.");
                    break;
                case SALIR:
                    Gdx.app.exit();
                    break;
            }
        }

        // --- DIBUJADO ---
        game.batch.begin();

        // Fondo con ligera transparencia.
        game.batch.setColor(1f, 1f, 1f, 0.8f);
        game.batch.draw(fondoMenu, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.setColor(1f, 1f, 1f, 1f); // Reset de color.

        // Dibujado de los botones en sus posiciones relativas.
        game.batch.draw(texturaBotonPartidaIndividual, Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_PARTIDA_INDIVIDUAL, anchoPartidaIndividual,
                alturaPartidaIndividual);

        game.batch.draw(texturaBotonMultiplayer, Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_MULTIPLAYER, anchoMultiplayer, alturaMultiplayer);

        game.batch.draw(texturaBotonOpciones, Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_OPCIONES, anchoOpciones, alturaOpciones);

        game.batch.draw(texturaBotonSalir, Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_SALIR, anchoSalir, alturaSalir);

        game.batch.end();
    }

    @Override
    public void show() {
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
     * Libera los recursos de las texturas para evitar fugas de memoria.
     */
    @Override
    public void dispose() {
        fondoMenu.dispose();
        partidaIndividual.dispose();
        partidaIndividualC.dispose();
        salirMenu.dispose();
        salirMenuC.dispose();
        multiplayer.dispose();
        multiplayerC.dispose();
        opcionesMenu.dispose();
        opcionesMenuC.dispose();
    }
}
