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
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;

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
    public Mapa(Main game, String nombreArchivo) {
        this.game = game;

        // Ahora cargamos el mapa usando la variable nombreArchivo
        mapaTiled = new TmxMapLoader().load(nombreArchivo);
        renderer = new OrthogonalTiledMapRenderer(mapaTiled, UNIT_SCALE, game.batch);

        anchoMapa = mapaTiled.getProperties().get("width", Integer.class);
        altoMapa = mapaTiled.getProperties().get("height", Integer.class);

        camera = new OrthographicCamera();
        jugador = new Player(10f, 10f); // Puedes ajustar esto para que aparezca en otro sitio
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
        MapLayer layerVerde = mapaTiled.getLayers().get("pokeballVerde");
        if (layerVerde instanceof TiledMapTileLayer) {
            TiledMapTileLayer tileLayer = (TiledMapTileLayer) layerVerde;
            TiledMapTileLayer.Cell cell = tileLayer.getCell(cellX, cellY);
            if (cell != null) {
                // Elimina la pokeball
                tileLayer.setCell(cellX, cellY, null);
                System.out.println("Pokeball verde recogida!");
                return;
            }
        }

        // Comprobamos la capa 'pokeballVerdeDos'
        MapLayer layerVerdeDos = mapaTiled.getLayers().get("pokeballVerdeDos");
        if (layerVerdeDos instanceof TiledMapTileLayer) {
            TiledMapTileLayer tileLayer = (TiledMapTileLayer) layerVerdeDos;
            TiledMapTileLayer.Cell cell = tileLayer.getCell(cellX, cellY);
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
        // Primero verificamos que haya suelo y no nos salgamos del mapa
        // Buscamos la primera capa de tiles para usarla como referencia de bordes
        TiledMapTileLayer backgroundLayer = null;
        for (MapLayer layer : mapaTiled.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                backgroundLayer = (TiledMapTileLayer) layer;
                break;
            }
        }

        if (backgroundLayer == null) {
            return true; // Si no hay capas de tiles, asumimos solido por seguridad
        }

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

    public void revisarPortales(float x, float y) {
        // Buscamos la capa de objetos (asegúrate que el nombre coincida con Tiled)
        MapLayer capaObjetos = mapaTiled.getLayers().get("Portal"); // O "Portales"

        if (capaObjetos != null) {
            for (MapObject objeto : capaObjetos.getObjects()) {
                if (objeto instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) objeto).getRectangle();

                    // IMPORTANTE: Como usamos UNIT_SCALE (1/16), debemos ajustar
                    // el rectángulo si Tiled lo da en píxeles.
                    float rectX = rect.x * UNIT_SCALE;
                    float rectY = rect.y * UNIT_SCALE;
                    float rectW = rect.width * UNIT_SCALE;
                    float rectH = rect.height * UNIT_SCALE;

                    if (x >= rectX && x <= rectX + rectW && y >= rectY && y <= rectY + rectH) {
                        String siguienteMapa = objeto.getProperties().get("Destino", String.class);
                        if (siguienteMapa != null) {
                            if (!siguienteMapa.endsWith(".tmx")) {
                                siguienteMapa += ".tmx";
                            }
                            game.setScreen(new Mapa(game, siguienteMapa));
                            dispose();
                        }
                    }
                }
            }
        }
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
