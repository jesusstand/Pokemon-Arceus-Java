package com.Proyecto.Pokemon;

import com.Proyecto.Pokemon.pokemon.Pokemon;
import com.Proyecto.Pokemon.pokemon.PokeFuego;
import com.Proyecto.Pokemon.pokemon.PokeAgua;
import com.Proyecto.Pokemon.pokemon.PokePlanta;
import com.Proyecto.Pokemon.gui.Mapa;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Input.Keys;

/**
 * Pantalla de eleccion del Pokemon inicial.
 * Permite seleccionar entre Ponyta, Seel y Bulbasaur.
 */
public class PantallaDeEleccion implements Screen {

    private Main game;
    private Texture fondo;
    private Texture titulo;
    private Texture ponyta, seel, bulbasaur;

    private int opcionSeleccionada = 0; // 0: Ponyta, 1: Seel, 2: Bulbasaur
    private static final int CANTIDAD_POKEMON = 3;

    public PantallaDeEleccion(Main game) {
        this.game = game;

        // Carga de assets
        fondo = new Texture(Gdx.files.internal("FondoEleccion.png"));
        titulo = new Texture(Gdx.files.internal("PokemonEleccion.png"));

        ponyta = new Texture(Gdx.files.internal("Ponyta.png"));
        seel = new Texture(Gdx.files.internal("Seel.png"));
        bulbasaur = new Texture(Gdx.files.internal("Bulbasaur.png"));
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        game.batch.begin();

        // 1. Fondo
        game.batch.setColor(1, 1, 1, 1);
        game.batch.draw(fondo, 0, 0, sw, sh);

        // 2. Titulo (Centrado arriba)
        float tituloW = sw * 0.15f;
        float tituloH = sh * 0.15f;
        float tituloX = (sw - tituloW) / 2f;
        float tituloY = sh * 0.75f;
        game.batch.draw(titulo, tituloX, tituloY, tituloW, tituloH);

        // 3. Pokemones (Uno al lado del otro abajo)
        float espacioDisponible = sw * 0.95f;
        float margenX = sw * 0.025f;
        float pokeW = espacioDisponible / 3f;
        // Ajustamos la altura proporcionalmente o fijo, dependiendo de la imagen
        // original.
        // Como no sabemos la proporcion exacta, usaremos un tamaño cuadrado relativo al
        // ancho
        // pero ajustado un poco para que no quede enorme.
        float sizeBase = Math.min(pokeW, sh * 0.4f);

        float startY = sh * 0.2f;

        // --- PONYTA (Indice 0) ---
        dibujarPokemon(ponyta, 0, margenX, startY, sizeBase);

        // --- SEEL (Indice 1) ---
        dibujarPokemon(seel, 1, margenX + pokeW, startY, sizeBase);

        // --- BULBASAUR (Indice 2) ---
        dibujarPokemon(bulbasaur, 2, margenX + pokeW * 2, startY, sizeBase);

        game.batch.end();

        // --- INPUT ---
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            opcionSeleccionada = (opcionSeleccionada - 1 + CANTIDAD_POKEMON) % CANTIDAD_POKEMON;
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            opcionSeleccionada = (opcionSeleccionada + 1) % CANTIDAD_POKEMON;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            // Seleccionar Pokemon y empezar juego
            seleccionarPokemon();
        }
    }

    private void dibujarPokemon(Texture tex, int indice, float xBase, float yBase, float size) {
        float scale = 1.0f;
        boolean seleccionado = (indice == opcionSeleccionada);

        if (seleccionado) {
            scale = 1.1f; // Aumentar un poco el tamaño
            game.batch.setColor(1f, 1f, 1f, 1f); // Color normal
        } else {
            scale = 1.0f;
            game.batch.setColor(0f, 0f, 0f, 1f); // Totalmente negro
        }

        // Centrar la imagen en su espacio asignado
        float drawSize = size * scale;

        // Vamos a centrar la textura en el rectangulo [xBase, yBase, columnWidth?,
        // sizeBase]
        // Usaremos size como width de columna para simplificar la llamada anterior.
        float drawX = xBase + (size - drawSize) / 2f;
        float drawY = yBase + (size - drawSize) / 2f;

        game.batch.draw(tex, drawX, drawY, drawSize, drawSize);

        // Reset color por si acaso
        game.batch.setColor(1, 1, 1, 1);
    }

    private void seleccionarPokemon() {
        // Guardar el pokemon inicial en Main
        Pokemon seleccionado;
        switch (opcionSeleccionada) {
            case 0:
                seleccionado = new PokeFuego.Ignirrojo("Macho"); // Representa a Ponyta por ahora
                break;
            case 1:
                seleccionado = new PokeAgua.Aqualisca("Macho"); // Representa a Seel por ahora
                break;
            case 2:
                seleccionado = new PokePlanta.Brotalamo("Macho"); // Representa a Bulbasaur por ahora
                break;
            default:
                seleccionado = new PokeFuego.Ignirrojo("Macho");
        }
        game.setPokemonInicial(seleccionado);

        // Cargar el mapa
        game.setScreen(new Mapa(game, "MapaVerdePokemon.tmx"));
        dispose();
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
        fondo.dispose();
        titulo.dispose();
        ponyta.dispose();
        seel.dispose();
        bulbasaur.dispose();
    }
}
