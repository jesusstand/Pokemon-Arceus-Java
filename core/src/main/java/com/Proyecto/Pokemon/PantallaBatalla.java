package com.Proyecto.Pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Pantalla que gestiona la visualización de la batalla Pokemon.
 * Muestra los Pokemon, el fondo y permite huir (volver al mapa).
 */
public class PantallaBatalla implements Screen {

    private Main game;
    private Screen pantallaAnterior;
    private Pokemon pokemonJugador;
    private Pokemon pokemonRival;
    private Batalla batalla;
    private OrthographicCamera camera;

    // Assets
    private Texture fondoBatalla;
    private Texture botonPelea, botonPeleaActivo;
    private Texture botonMochila, botonMochilaActivo;
    private Texture botonEquipo, botonEquipoActivo;
    private Texture botonHuir, botonHuirActivo;

    // Sprites temporales
    private Texture spriteJugador;
    private Texture spriteRival;

    // Lógica UI
    private int opcionSeleccionada = 0;
    private BitmapFont font;

    // Log de batalla
    private String mensajeLog = "";

    public PantallaBatalla(Main game, Screen pantallaAnterior, Pokemon pokemonJugador, Pokemon pokemonRival) {
        this.game = game;
        this.pantallaAnterior = pantallaAnterior;
        this.pokemonJugador = pokemonJugador;
        this.pokemonRival = pokemonRival;

        // Inicializar cámara para UI (pixel-perfect)
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Inicializar lógica de batalla
        this.batalla = new Batalla(pokemonJugador, pokemonRival);

        cargarAssets();
    }

    private void cargarAssets() {
        fondoBatalla = new Texture(Gdx.files.internal("CampoBatalla.png"));

        botonPelea = new Texture(Gdx.files.internal("Boton Pelea.png"));
        botonPeleaActivo = new Texture(Gdx.files.internal("Boton Pelea activo.png"));

        botonMochila = new Texture(Gdx.files.internal("Boton Mochila.png"));
        botonMochilaActivo = new Texture(Gdx.files.internal("Boton Mochila activo.png"));

        botonEquipo = new Texture(Gdx.files.internal("Boton Equipo.png"));
        botonEquipoActivo = new Texture(Gdx.files.internal("Boton Equipo activo.png"));

        botonHuir = new Texture(Gdx.files.internal("Boton Huir.png"));
        botonHuirActivo = new Texture(Gdx.files.internal("Boton Huir activo.png"));

        spriteJugador = cargarSpritePara(pokemonJugador);
        spriteRival = cargarSpritePara(pokemonRival);

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
    }

    private Texture cargarSpritePara(Pokemon p) {
        // Mapeo simple basado en los assets disponibles
        if (p instanceof PokeFuego)
            return new Texture(Gdx.files.internal("Ponyta.png"));
        if (p instanceof PokeAgua)
            return new Texture(Gdx.files.internal("Seel.png"));
        if (p instanceof PokePlanta)
            return new Texture(Gdx.files.internal("Bulbasaur.png"));

        return new Texture(Gdx.files.internal("Bulbasaur.png"));
    }

    @Override
    public void render(float delta) {
        manejarInput();

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        // 1. Dibujar Fondo
        game.batch.draw(fondoBatalla, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 2. Dibujar Pokemon
        dibujarEscenaBatalla();

        // 3. Dibujar UI Botones
        dibujarBotones();

        // 4. Dibujar Mensajes
        font.draw(game.batch, mensajeLog, 50, Gdx.graphics.getHeight() - 50);

        game.batch.end();
    }

    private void dibujarEscenaBatalla() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        // Pokemon Jugador (Izquierda - Abajo)
        float xJugador = sw * 0.15f;
        float yJugador = sh * 0.25f;
        float sizeJugador = sh * 0.35f;

        game.batch.draw(spriteJugador, xJugador, yJugador, sizeJugador, sizeJugador);
        font.draw(game.batch, pokemonJugador.getNombre() + " PS: " + pokemonJugador.getVida(), xJugador, yJugador - 20);

        // Pokemon Rival (Derecha - Arriba)
        float xRival = sw * 0.65f;
        float yRival = sh * 0.55f;
        float sizeRival = sh * 0.25f;

        game.batch.draw(spriteRival, xRival, yRival, sizeRival, sizeRival);
        font.draw(game.batch, pokemonRival.getNombre() + " PS: " + pokemonRival.getVida(), xRival,
                yRival + sizeRival + 40);
    }

    private void dibujarBotones() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        float btnW = sw * 0.2f;
        float btnH = sh * 0.1f;
        float margen = 10;

        float startX = sw * 0.55f;
        float startY = sh * 0.05f;

        drawBtn(botonPelea, botonPeleaActivo, 0, startX, startY + btnH + margen, btnW, btnH);
        drawBtn(botonMochila, botonMochilaActivo, 1, startX + btnW + margen, startY + btnH + margen, btnW, btnH);
        drawBtn(botonEquipo, botonEquipoActivo, 2, startX, startY, btnW, btnH);
        drawBtn(botonHuir, botonHuirActivo, 3, startX + btnW + margen, startY, btnW, btnH);
    }

    private void drawBtn(Texture inactivo, Texture activo, int id, float x, float y, float w, float h) {
        if (opcionSeleccionada == id) {
            game.batch.draw(activo, x, y, w, h);
        } else {
            game.batch.draw(inactivo, x, y, w, h);
        }
    }

    private void manejarInput() {
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            if (opcionSeleccionada == 2)
                opcionSeleccionada = 0;
            if (opcionSeleccionada == 3)
                opcionSeleccionada = 1;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            if (opcionSeleccionada == 0)
                opcionSeleccionada = 2;
            if (opcionSeleccionada == 1)
                opcionSeleccionada = 3;
        }
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            if (opcionSeleccionada == 1)
                opcionSeleccionada = 0;
            if (opcionSeleccionada == 3)
                opcionSeleccionada = 2;
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            if (opcionSeleccionada == 0)
                opcionSeleccionada = 1;
            if (opcionSeleccionada == 2)
                opcionSeleccionada = 3;
        }

        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            ejecutarAccion();
        }
    }

    private void ejecutarAccion() {
        switch (opcionSeleccionada) {
            case 0: // PELEA
                if (!batalla.estaTerminada()) {
                    mensajeLog = batalla.realizarAtaque();
                } else {
                    mensajeLog = "La batalla ha terminado.\nPulsa Huir para salir.";
                    // Opcional: Auto-exit
                }
                break;
            case 3: // HUIR
                huirDeBatalla();
                break;
            default:
                mensajeLog = "Acción no implementada aún.";
                break;
        }
    }

    private void huirDeBatalla() {
        game.setScreen(pantallaAnterior);
        dispose();
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
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        fondoBatalla.dispose();
        botonPelea.dispose();
        botonPeleaActivo.dispose();
        botonHuir.dispose();
        botonHuirActivo.dispose();
        botonMochila.dispose();
        botonMochilaActivo.dispose();
        botonEquipo.dispose();
        botonEquipoActivo.dispose();
        spriteJugador.dispose();
        spriteRival.dispose();
        font.dispose();
    }
}
