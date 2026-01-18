package com.Proyecto.Pokemon;

import com.Proyecto.Pokemon.gui.MenuPrincipal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;

/**
 * Pantalla intermedia para seleccionar entre Nueva Partida y Cargar Partida.
 */
public class PantallaSeleccionPartida implements Screen {

    private Main game;
    private Texture fondoMenu;
    private Texture nuevaPartida, nuevaPartidaC;
    private Texture cargarPartida, cargarPartidaC;
    private Texture salir, salirC;

    // --- CONSTANTES DEL ESTADO DEL MENU ---
    private static final int NUEVA_PARTIDA = 0;
    private static final int CARGAR_PARTIDA = 1;
    private static final int SALIR = 2;
    private static final int OPCIONES = 3; // Cantidad total de botones.

    private int opcionSeleccionada;

    // --- CONSTANTES DE DISENO RESPONSIVO ---
    private static final float BUTTON_X_POS = 0.08f;
    private static final float BUTTON_Y_NUEVA = 0.70f; // Misma posicion que Partida Individual
    private static final float BUTTON_Y_CARGAR = 0.55f; // Misma posicion que Multiplayer
    private static final float BUTTON_Y_SALIR = 0.40f; // Posicion ajustada para alinearse mejor

    private static final float BUTTON_WIDTH_FACTOR = 0.35f;
    private static final float BUTTON_HEIGHT_FACTOR = 0.10f;

    public PantallaSeleccionPartida(Main game) {
        this.game = game;

        fondoMenu = new Texture(Gdx.files.internal("arceus.jpg"));

        // Cargar texturas segun nombres proporcionados por el usuario
        nuevaPartida = new Texture(Gdx.files.internal("Boton de Nueva Partida base.png"));
        nuevaPartidaC = new Texture(Gdx.files.internal("Boton de Nueva Partida.png"));

        cargarPartida = new Texture(Gdx.files.internal("Boton de Cargar Partida base.png"));
        cargarPartidaC = new Texture(Gdx.files.internal("Boton de cargar Partida.png"));

        salir = new Texture(Gdx.files.internal("Salir.png"));
        salirC = new Texture(Gdx.files.internal("SalirC.png"));

        opcionSeleccionada = NUEVA_PARTIDA;
    }

    @Override
    public void render(float delta) {
        float baseWidth = Gdx.graphics.getWidth() * BUTTON_WIDTH_FACTOR;
        float baseHeight = Gdx.graphics.getHeight() * BUTTON_HEIGHT_FACTOR;
        float scaleFactor = 1.1f;

        Texture texNueva = nuevaPartida;
        Texture texCargar = cargarPartida;
        Texture texSalir = salir;

        float wNueva = baseWidth, hNueva = baseHeight;
        float wCargar = baseWidth, hCargar = baseHeight;
        float wSalir = baseWidth, hSalir = baseHeight;

        if (opcionSeleccionada == NUEVA_PARTIDA) {
            texNueva = nuevaPartidaC;
            wNueva *= scaleFactor;
            hNueva *= scaleFactor;
        } else if (opcionSeleccionada == CARGAR_PARTIDA) {
            texCargar = cargarPartidaC;
            wCargar *= scaleFactor;
            hCargar *= scaleFactor;
        } else if (opcionSeleccionada == SALIR) {
            texSalir = salirC;
            wSalir *= scaleFactor;
            hSalir *= scaleFactor;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.setColor(1f, 1f, 1f, 0.8f);
        game.batch.draw(fondoMenu, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.setColor(1f, 1f, 1f, 1f);

        game.batch.draw(texNueva, Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_NUEVA, wNueva, hNueva);

        game.batch.draw(texCargar, Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_CARGAR, wCargar, hCargar);

        game.batch.draw(texSalir, Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_SALIR, wSalir, hSalir);

        game.batch.end();

        // Input
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            opcionSeleccionada = (opcionSeleccionada - 1 + OPCIONES) % OPCIONES;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            opcionSeleccionada = (opcionSeleccionada + 1) % OPCIONES;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            switch (opcionSeleccionada) {
                case NUEVA_PARTIDA:
                    // Iniciar nueva partida (Mismo comportamiento que tenia Partida Individual)
                    game.setScreen(new PantallaDeEleccion(game));
                    dispose();
                    break;
                case CARGAR_PARTIDA:
                    // No hace nada por ahora
                    System.out.println("Cargar Partida no implementado aun.");
                    break;
                case SALIR:
                    // Volver al menu principal
                    game.setScreen(new MenuPrincipal(game));
                    dispose();
                    break;
            }
        }
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

    @Override
    public void dispose() {
        fondoMenu.dispose();
        nuevaPartida.dispose();
        nuevaPartidaC.dispose();
        cargarPartida.dispose();
        cargarPartidaC.dispose();
        salir.dispose();
        salirC.dispose();
    }
}
