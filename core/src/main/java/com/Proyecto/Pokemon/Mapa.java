package com.Proyecto.Pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Pantalla principal del juego donde ocurre la accion.
 * Carga el mapa Tiled, gestiona el jugador y la camara.
 */
public class Mapa implements Screen {
    private Main game;
    private TiledMap mapaTiled;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Player jugador;

    private float anchoMapa, altoMapa;
    // Escala unitaria: 1 unidad = 16 pixeles (tamaño del tile)
    private static final float UNIT_SCALE = 1 / 16f;

    /**
     * Constructor del Mapa.
     *
     * @param game Instancia principal del juego.
     */
    public Mapa(Main game) {
        this.game = game;
        // Carga el mapa desde la carpeta assets
        mapaTiled = new TmxMapLoader().load("MapaVerdePokemon.tmx");
        renderer = new OrthogonalTiledMapRenderer(mapaTiled, UNIT_SCALE, game.batch);

        // Obtiene las dimensiones del mapa desde las propiedades
        anchoMapa = mapaTiled.getProperties().get("width", Integer.class);
        altoMapa = mapaTiled.getProperties().get("height", Integer.class);

        camera = new OrthographicCamera();
        // Inicializa al jugador en la posicion (10, 10)
        jugador = new Player(10f, 10f);
    }

    /**
     * Interactua con el tile en la posicion dada.
     * 
     * @param x Coordenada X del tile objetivo.
     * @param y Coordenada Y del tile objetivo.
     */
    public void interactuar(float x, float y) {
        int cellX = (int) x;
        int cellY = (int) y;

        // Comprobamos la capa 'pokeballVerde'
        TiledMapTileLayer layerVerde = (TiledMapTileLayer) mapaTiled.getLayers().get("pokeballVerde");
        if (layerVerde != null) {
            TiledMapTileLayer.Cell cell = layerVerde.getCell(cellX, cellY);
            if (cell != null) {
                // Elimina la pokeball
                layerVerde.setCell(cellX, cellY, null);
                System.out.println("Pokeball verde recogida!");
                return;
            }
        }

        // Comprobamos la capa 'pokeballVerdeDos'
        TiledMapTileLayer layerVerdeDos = (TiledMapTileLayer) mapaTiled.getLayers().get("pokeballVerdeDos");
        if (layerVerdeDos != null) {
            TiledMapTileLayer.Cell cell = layerVerdeDos.getCell(cellX, cellY);
            if (cell != null) {
                // Vuelve a la pantalla de inicio
                game.setScreen(new PantallaDeInicio(game));
                return;
            }
        }
    }

    /**
     * Comprueba si una celda especifica del mapa es solida.
     *
     * @param x Coordenada X del tile.
     * @param y Coordenada Y del tile.
     * @return true si es solido (no transitable), false si es libre.
     */
    public boolean esSolido(int x, int y) {
        // Primero verificamos que haya suelo (capa 0) y no nos salgamos del mapa
        TiledMapTileLayer backgroundLayer = (TiledMapTileLayer) mapaTiled.getLayers().get(0);
        TiledMapTileLayer.Cell bgCell = backgroundLayer.getCell(x, y);

        if (bgCell == null) {
            return true; // Vacio/Fuera de limites es solido
        }

        // Iteramos sobre TODAS las capas para buscar objetos de colision
        for (MapLayer layer : mapaTiled.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null && cell.getTile().getObjects().getCount() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void render(float delta) {
        // Actualiza la logica del jugador
        jugador.update(delta, this);

        // La camara sigue al jugador suavemente
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        // Limita la camara para que no salga de los bordes del mapa (clamp)
        // Sumamos 0.5f para que la camara se centre en el personaje
        float camX = MathUtils.clamp(jugador.getX() + 0.5f, halfWidth, anchoMapa - halfWidth);
        float camY = MathUtils.clamp(jugador.getY() + 0.5f, halfHeight, altoMapa - halfHeight);

        camera.position.set(camX, camY, 0);
        camera.update();

        // Limpieza del buffer de pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Renderiza el mapa
        renderer.setView(camera);
        renderer.render();

        // Dibuja al jugador
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        jugador.draw(game.batch);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Configura el viewport.
        float tilesVisibles = 18f;
        camera.viewportWidth = tilesVisibles;
        camera.viewportHeight = tilesVisibles * ((float) height / (float) width);
        camera.update();
    }

    @Override
    public void dispose() {
        mapaTiled.dispose();
        renderer.dispose();
        jugador.dispose();
    }

    @Override
    public void show() {
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
}
