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
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;

/**
 * Pantalla principal del juego donde ocurre la accion.
 * Gestiona el mapa de Tiled, el renderizado de los graficos y la interaccion
 * del jugador.
 */
public class Mapa implements Screen {
    private Main game;
    private TiledMap mapaTiled;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Player jugador;

    private float anchoMapa, altoMapa;
    // Escala unitaria: 1 unidad de mundo = 16 pixeles (tamaño de un tile).
    private static final float UNIT_SCALE = 1 / 16f;

    /**
     * Constructor del Mapa.
     *
     * @param game          Instancia principal del juego.
     * @param nombreArchivo El nombre del archivo .tmx que se cargara.
     */
    public Mapa(Main game, String nombreArchivo) {
        this.game = game;

        // Cargamos el mapa de Tiled usando la ruta del archivo.
        mapaTiled = new TmxMapLoader().load(nombreArchivo);
        renderer = new OrthogonalTiledMapRenderer(mapaTiled, UNIT_SCALE, game.batch);

        // Obtenemos las dimensiones del mapa en numero de baldosas.
        anchoMapa = mapaTiled.getProperties().get("width", Integer.class);
        altoMapa = mapaTiled.getProperties().get("height", Integer.class);

        camera = new OrthographicCamera();
        // Inicializamos al jugador en la posicion inicial.
        jugador = new Player(10f, 10f);
    }

    /**
     * Gestiona la interaccion del jugador con el mundo.
     *
     * @param x Coordenada X del objetivo de interaccion.
     * @param y Coordenada Y del objetivo de interaccion.
     * @return true si se realizo una interaccion con exito.
     */
    public boolean interactuar(float x, float y) {
        int cellX = (int) x;
        int cellY = (int) y;

        // Recorremos todas las capas del mapa.
        for (MapLayer layer : mapaTiled.getLayers()) {

            // --- CASO 1: Capas de Baldosas (Tile Layers) ---
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                TiledMapTileLayer.Cell cell = tileLayer.getCell(cellX, cellY);

                if (cell != null && cell.getTile() != null) {
                    String tipo = getPropiedad(cell.getTile(), "tipo");

                    // PRIORIDAD 1: Eventos especiales (ej: ir al inicio).
                    if ("inicio".equalsIgnoreCase(tipo)) {
                        game.setScreen(new PantallaDeInicio(game));
                        dispose();
                        return true;
                    }

                    // PRIORIDAD 2: Recoger objetos del suelo.
                    // Condicion: Es de tipo "recogible" (en el tile), es una Pokébola, O la CAPA es
                    // recogible.
                    if (esRecogible(cell.getTile()) || esCapaRecogible(layer)) {
                        borrarAreaRecogible(cellX, cellY);
                        return true;
                    }
                }
            }

            // --- CASO 2: Capas de Objetos (Object Layers) ---
            // Revisamos tanto objetos de tipo baldosa como formas.
            for (MapObject obj : layer.getObjects()) {
                if (obj instanceof TiledMapTileMapObject) {
                    TiledMapTileMapObject tileObj = (TiledMapTileMapObject) obj;
                    if (tileObj.getTile() == null)
                        continue;

                    // Calculamos el área que ocupa el objeto en el mundo.
                    float objX = tileObj.getX() * UNIT_SCALE;
                    float objY = tileObj.getY() * UNIT_SCALE;
                    float objW = tileObj.getTile().getTextureRegion().getRegionWidth() * UNIT_SCALE;
                    float objH = tileObj.getTile().getTextureRegion().getRegionHeight() * UNIT_SCALE;

                    // Si el clic del jugador está dentro del objeto...
                    if (x >= objX && x < objX + objW && y >= objY && y < objY + objH) {
                        String tipo = getPropiedad(tileObj.getTile(), "tipo");

                        if ("inicio".equalsIgnoreCase(tipo)) {
                            game.setScreen(new PantallaDeInicio(game));
                            dispose();
                            return true;
                        }

                        if (esRecogible(tileObj.getTile())) {
                            layer.getObjects().remove(obj);
                            System.out.println("Objeto recogido de la capa de objetos: " + layer.getName());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Metodo interno para borrar todas las piezas de una pokebola multi-tile.
     *
     * @param x Coordenada X central.
     * @param y Coordenada Y central.
     */
    private void borrarAreaRecogible(int x, int y) {
        // Escaneamos un area de 3x3 (radio de 1 baldosa).
        // Esto es suficiente para objetos de 2x2 y evita borrar objetos vecinos por
        // error.
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int tx = x + dx;
                int ty = y + dy;
                for (MapLayer layer : mapaTiled.getLayers()) {
                    if (layer instanceof TiledMapTileLayer) {
                        TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                        TiledMapTileLayer.Cell cell = tileLayer.getCell(tx, ty);
                        if (cell != null && cell.getTile() != null) {
                            // Borramos si el tile es recogible O si la capa entera lo es.
                            if (esRecogible(cell.getTile()) || esCapaRecogible(layer)) {
                                tileLayer.setCell(tx, ty, null);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Objeto recogido y mapa actualizado en " + x + "," + y);
    }

    /**
     * Determina si un tile dado es una pokebola segun su origen o sus propiedades.
     * 
     * @param tile El tile a verificar.
     * @return true si es una pokebola.
     */
    private boolean esRecogible(com.badlogic.gdx.maps.tiled.TiledMapTile tile) {
        if (tile == null)
            return false;

        String tipo = getPropiedad(tile, "tipo");

        // Un objeto de "inicio" no es recogible (es un evento).
        if ("inicio".equalsIgnoreCase(tipo))
            return false;

        // Si tiene la propiedad explícita de "recogible".
        if ("recogible".equalsIgnoreCase(tipo))
            return true;

        // Comprobación por nombre del conjunto de patrones (tileset).
        // Si el tileset tiene "pokebola" o "pokeball" en el nombre, lo tratamos como
        // recogible.
        for (com.badlogic.gdx.maps.tiled.TiledMapTileSet tileset : mapaTiled.getTileSets()) {
            String name = tileset.getName();
            if (name != null && (name.toLowerCase().contains("pokebola") || name.toLowerCase().contains("pokeball"))) {
                for (com.badlogic.gdx.maps.tiled.TiledMapTile t : tileset) {
                    if (t == tile)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper para obtener una propiedad de un tile sin importar
     * mayúsculas/minúsculas.
     */
    private String getPropiedad(com.badlogic.gdx.maps.tiled.TiledMapTile tile, String key) {
        if (tile == null || tile.getProperties() == null)
            return null;
        for (java.util.Iterator<String> it = tile.getProperties().getKeys(); it.hasNext();) {
            String k = it.next();
            if (k.equalsIgnoreCase(key)) {
                return tile.getProperties().get(k).toString();
            }
        }
        return null;
    }

    /**
     * Determina si una capa entera contiene objetos que se pueden recoger.
     */
    private boolean esCapaRecogible(MapLayer layer) {
        if (layer == null)
            return false;

        // Buscamos propiedad "tipo" en la capa.
        String tipoCapa = null;
        if (layer.getProperties().containsKey("tipo")) {
            tipoCapa = layer.getProperties().get("tipo", String.class);
        }

        if ("recogible".equalsIgnoreCase(tipoCapa))
            return true;

        // Fallback para mantener funcionando lo anterior sin cambiar el Tiled.
        String nombre = layer.getName();
        return nombre != null && nombre.equalsIgnoreCase("Capa de patrones 2");
    }

    /**
     * Verifica si una posicion del mapa es solida para bloquear el paso del
     * jugador.
     *
     * @param x Coordenada X en baldosas.
     * @param y Coordenada Y en baldosas.
     * @return true si hay colision, false si es libre.
     */
    public boolean esSolido(int x, int y) {
        // En lugar de una sola capa, permitimos caminar si hay CUALQUIER baldosa de
        // fondo (suelo)
        // y no hay colisiones en las capas superiores.
        boolean tieneSuelo = false;

        for (MapLayer layer : mapaTiled.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);

                if (cell != null && cell.getTile() != null) {
                    tieneSuelo = true; // Si hay algo, asumimos que puede haber suelo.

                    // Los objetos interactuables NO bloquean el paso.
                    // Verificamos por propiedad de capa, propiedad de tile o si es evento.
                    if (esCapaRecogible(layer)
                            || esRecogible(cell.getTile())
                            || "inicio".equalsIgnoreCase(getPropiedad(cell.getTile(), "tipo"))) {
                        continue;
                    }

                    // Si el tile tiene formas de colisión, bloquea.
                    if (cell.getTile().getObjects().getCount() > 0) {
                        return true;
                    }
                }
            }
        }

        // Si no hay ninguna baldosa en ninguna capa en esa posición, es el vacío
        // (sólido).
        return !tieneSuelo;
    }

    /**
     * Detecta si el jugador esta en un portal y cambia de mapa.
     * 
     * @param x Coordenada X del jugador.
     * @param y Coordenada Y del jugador.
     */
    public void revisarPortales(float x, float y) {
        // Buscamos la capa de portales de forma más flexible (insensible a mayúsculas).
        MapLayer capaObjetos = null;
        for (MapLayer layer : mapaTiled.getLayers()) {
            if (layer.getName().equalsIgnoreCase("Portal") || layer.getName().equalsIgnoreCase("Portales")) {
                capaObjetos = layer;
                break;
            }
        }

        if (capaObjetos != null) {
            for (MapObject objeto : capaObjetos.getObjects()) {
                if (objeto instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) objeto).getRectangle();

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

    /**
     * Bucle de renderizado principal de la pantalla.
     *
     * @param delta Tiempo transcurrido entre el frame actual y el anterior.
     */
    @Override
    public void render(float delta) {
        // Actualizamos al jugador y la camara.
        jugador.update(delta, this);

        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        // Mantener la camara centrada en el jugador pero dentro de los limites del
        // mapa.
        float camX = MathUtils.clamp(jugador.getX() + 0.5f, halfWidth, anchoMapa - halfWidth);
        float camY = MathUtils.clamp(jugador.getY() + 0.5f, halfHeight, altoMapa - halfHeight);

        camera.position.set(camX, camY, 0);
        camera.update();

        // Limpiar la pantalla.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Renderizar mapa.
        renderer.setView(camera);
        renderer.render();

        // Renderizar jugador.
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        jugador.draw(game.batch);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Ajustamos la proporcion de la camara segun el tamaño de la ventana.
        float tilesVisibles = 18f;
        camera.viewportWidth = tilesVisibles;
        camera.viewportHeight = tilesVisibles * ((float) height / (float) width);
        camera.update();
    }

    @Override
    public void dispose() {
        // Liberar recursos de LibGDX.
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
