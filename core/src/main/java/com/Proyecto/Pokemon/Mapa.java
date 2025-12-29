package com.Proyecto.Pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
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
import java.util.HashMap;
import java.util.Iterator;

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

    // --- ESTADO DE PAUSA ---
    private boolean pausado = false;
    private boolean inventarioAbierto = false;
    private int opcionPausa = 0; // 0: Volver, 1: Opciones, 2: Salir
    // --- ESTADO CRAFTEO ---
    private boolean menuCrafteoAbierto = false;
    private int opcionCrafteo = 1; // 0, 1, 2
    // --- ESTADO POKEMON CAPTURADOS ---
    private boolean menuPokemonAbierto = false;
    private int pokemonSeleccionado = 0; // Índice del Pokemon seleccionado
    private Texture marcoCrafteoSeleccionado;
    private Texture marcoCrafteoNoSeleccionado;
    private Texture pausaSalir, pausaSalirC, pausaVolver, pausaVolverC, pausaOpciones, pausaOpcionesC, pausaPokepausa;
    private Texture marcoInventario, pixel;
    private Texture texCraftear, texCraftearC;
    private Texture marcoPlastico, marcoGoma, marcoMadera, marcoSlot, marcoSlotC;
    private Texture texPokeCura, texPokeExp, texPokeball;
    private BitmapFont font;

    // --- ESTADO ERROR UI ---
    private boolean mostrandoError = false;
    private String mensajeError = "";
    private float tiempoMensajeError = 0;

    // --- ESTADO ENCUENTRO POKEMON ---
    private SpawnPokemon spawnPokemon;
    private Pokemon pokemonSalvaje;
    private boolean enEncuentro = false;
    private CapturaPokemon sistemaCaptura;

    private static final int OPCION_REANUDAR = 0;
    private static final int OPCION_SALIR_MENU = 1;
    private static final int CANTIDAD_OPCIONES = 2;

    private static final int INV_CRAFTEAR = 0;

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
        // Inicializamos al jugador desde Main para persistencia.
        this.jugador = game.getJugador();

        // Inicializar sistema de spawn y captura de Pokemon
        this.spawnPokemon = new SpawnPokemon();
        this.sistemaCaptura = new CapturaPokemon(jugador.getInventario());

        // Carga de texturas para el menu de pausa.
        pausaSalir = new Texture(Gdx.files.internal("Salir.png"));
        pausaSalirC = new Texture(Gdx.files.internal("SalirC.png"));
        pausaVolver = new Texture(Gdx.files.internal("Boton de Continuar base.png"));
        pausaVolverC = new Texture(Gdx.files.internal("Boton de Continuar.png"));
        pausaOpciones = new Texture(Gdx.files.internal("Opciones.png"));
        pausaOpcionesC = new Texture(Gdx.files.internal("OpcionesC.png"));
        pausaPokepausa = new Texture(Gdx.files.internal("Pokepausa.png"));
        marcoInventario = new Texture(Gdx.files.internal("MarcoInventario.png"));

        // Inicializar fuente y textura de pixel blanco para dibujo manual.
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();

        // Carga de nuevas texturas de inventario.
        texCraftear = new Texture(Gdx.files.internal("Boton de Craftear base.png"));
        texCraftearC = new Texture(Gdx.files.internal("Boton de Craftear.jpeg"));
        marcoPlastico = new Texture(Gdx.files.internal("Marco 8bit Plastico.png"));
        marcoGoma = new Texture(Gdx.files.internal("Marco 8bit Goma.png"));
        marcoMadera = new Texture(Gdx.files.internal("Marco 8bit Madera.png"));
        marcoSlot = new Texture(Gdx.files.internal("Marco 8bit.png"));
        marcoSlotC = new Texture(Gdx.files.internal("Marco 8bit a color.png"));

        // Carga de texturas de items individuales
        texPokeCura = new Texture(Gdx.files.internal("PokeCura.png"));
        texPokeExp = new Texture(Gdx.files.internal("PokeExp.png"));
        texPokeball = new Texture(Gdx.files.internal("Pokeball.png"));

        // Carga del marco amarillo para crafteo
        marcoCrafteoSeleccionado = new Texture(Gdx.files.internal("MarcoInventariobase.png"));
        // Carga del marco azul para crafteo no seleccionado
        marcoCrafteoNoSeleccionado = new Texture(Gdx.files.internal("MarcoInventario2.png"));

        // Configurar filtro de fuente para estilo retro/pixelado
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
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
                    // Condicion: Es de tipo "recogible", es una Pokébola, O la CAPA es recogible.
                    if (esRecogible(cell.getTile()) || esCapaRecogible(layer)) {
                        String nombreObjeto = "Objeto";
                        String categoria = "item";

                        // Intentamos obtener el nombre de las nuevas propiedades.
                        if (getPropiedad(cell.getTile(), "Tipo") != null) {
                            nombreObjeto = getPropiedad(cell.getTile(), "Tipo");
                            categoria = "pokeball";
                        } else if (getPropiedad(cell.getTile(), "Item") != null) {
                            nombreObjeto = getPropiedad(cell.getTile(), "Item");
                            categoria = "item";
                        }

                        try {
                            jugador.getInventario().agregarObjeto(nombreObjeto, categoria);
                            borrarAreaRecogible(cellX, cellY);
                        } catch (ExcepcionInventarioLleno e) {
                            mostrarError(e.getMessage());
                        }
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

        // Si tiene la propiedad explícita de "recogible" o tiene Tipo/Item.
        if ("recogible".equalsIgnoreCase(tipo) || getPropiedad(tile, "Tipo") != null
                || getPropiedad(tile, "Item") != null)
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
        for (Iterator<String> it = tile.getProperties().getKeys(); it.hasNext();) {
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
     * Verifica si el jugador está en un tile de hierba.
     * 
     * @param x Coordenada X del jugador.
     * @param y Coordenada Y del jugador.
     * @return true si está en hierba, false si no.
     */
    public boolean estaEnHierba(float x, float y) {
        int cellX = (int) x;
        int cellY = (int) y;

        // Recorremos todas las capas del mapa
        for (MapLayer layer : mapaTiled.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                TiledMapTileLayer.Cell cell = tileLayer.getCell(cellX, cellY);

                if (cell != null && cell.getTile() != null) {
                    // Verificar si el tile tiene la propiedad "hierba" o "grass"
                    String tipo = getPropiedad(cell.getTile(), "tipo");
                    if ("hierba".equalsIgnoreCase(tipo) || "grass".equalsIgnoreCase(tipo)) {
                        return true;
                    }

                    // También verificar si la capa tiene la propiedad de hierba
                    String tipoCapa = null;
                    if (layer.getProperties().containsKey("tipo")) {
                        tipoCapa = layer.getProperties().get("tipo", String.class);
                    }
                    if ("hierba".equalsIgnoreCase(tipoCapa) || "grass".equalsIgnoreCase(tipoCapa)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Verifica si debe aparecer un Pokemon salvaje al caminar sobre hierba.
     * 
     * @param x Coordenada X del jugador.
     * @param y Coordenada Y del jugador.
     */
    public void verificarEncuentroPokemon(float x, float y) {
        // Solo verificar si no estamos ya en un encuentro
        if (enEncuentro) {
            return;
        }

        // Verificar si está en hierba
        if (estaEnHierba(x, y)) {
            // Intentar spawn de Pokemon
            Pokemon pokemonEncontrado = spawnPokemon.verificarEncuentro();
            if (pokemonEncontrado != null) {
                pokemonSalvaje = pokemonEncontrado;
                enEncuentro = true;
                System.out.println("¡Un " + pokemonSalvaje.getNombre() + " salvaje apareció!");
            }
        }
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

                            // IMPORTANTE: Reseteamos la posición del jugador para el nuevo mapa.
                            // Podríamos guardarla en propiedades del portal si fuera necesario.
                            jugador.getPosicion().set(10, 10);
                            jugador.getDestino().set(10, 10);

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
        // Al pulsar ESCAPE alternamos la pausa.
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (!inventarioAbierto && !menuCrafteoAbierto) { // Evitar abrir pausa si estamos crafteando
                pausado = !pausado;
            } else if (menuCrafteoAbierto) {
                // Opcional: ESCAPE en crafteo cierra el menu (redundante con logica interna,
                // pero seguridad)
                menuCrafteoAbierto = false;
            }
        }

        // Al pulsar E alternamos el inventario.
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.E)) {
            if (!pausado && !menuPokemonAbierto) {
                if (menuCrafteoAbierto) {
                    menuCrafteoAbierto = false;
                    inventarioAbierto = true; // Volver al inv
                } else {
                    inventarioAbierto = !inventarioAbierto;
                }
            }
        }

        // Al pulsar P alternamos el menú de Pokemon capturados.
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.P)) {
            if (!pausado && !inventarioAbierto && !menuCrafteoAbierto && !enEncuentro) {
                menuPokemonAbierto = !menuPokemonAbierto;
                if (menuPokemonAbierto) {
                    pokemonSeleccionado = 0; // Resetear selección al abrir
                }
            }
        }

        if (pausado) {
            actualizarEntradaPausa();
        } else if (enEncuentro) {
            actualizarEntradaEncuentro();
        } else if (menuPokemonAbierto) {
            actualizarEntradaPokemon();
        } else if (menuCrafteoAbierto) {
            actualizarEntradaCrafteo();
        } else if (inventarioAbierto) {
            actualizarEntradaInventario();
        } else {
            // Solo actualizamos al jugador si no esta pausado ni en inventario.
            jugador.update(delta, this);
        }

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

        // DIBUJAR OVERLAY DE PAUSA
        if (pausado) {
            dibujarMenuPausa();
        } else if (enEncuentro) {
            dibujarEncuentroPokemon();
            if (mostrandoError)
                dibujarCuadroError(delta);
        } else if (menuPokemonAbierto) {
            dibujarMenuPokemon();
            if (mostrandoError)
                dibujarCuadroError(delta);
        } else if (menuPokemonAbierto) {
            dibujarMenuPokemon();
            if (mostrandoError)
                dibujarCuadroError(delta);
        } else if (menuCrafteoAbierto) {
            dibujarMenuCrafteo();
            if (mostrandoError)
                dibujarCuadroError(delta);
        } else if (inventarioAbierto) {
            dibujarInventario();
            if (mostrandoError)
                dibujarCuadroError(delta);
        } else {
            if (mostrandoError)
                dibujarCuadroError(delta); // Mostrar error en juego normal (ej: pickup)
        }
    }

    /**
     * Gestiona la entrada del teclado cuando el juego esta en pausa.
     */
    private void actualizarEntradaPausa() {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {
            opcionPausa = (opcionPausa - 1 + CANTIDAD_OPCIONES) % CANTIDAD_OPCIONES;
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
            opcionPausa = (opcionPausa + 1) % CANTIDAD_OPCIONES;
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            if (opcionPausa == OPCION_REANUDAR) {
                // Reanudar el juego
                pausado = false;
            } else if (opcionPausa == OPCION_SALIR_MENU) {
                // Salir al menú principal
                game.setScreen(new MenuPrincipal(game));
                dispose();
            }
        }
    }

    /**
     * Dibuja los elementos visuales del menu de pausa.
     */
    private void dibujarMenuPausa() {
        // Usamos una proyeccion estatica para el menu (coordenadas de pantalla).
        // Sin embargo, para mantenerlo simple y centrado, usaremos una proporcion
        // local.
        float pantallaAncho = Gdx.graphics.getWidth();
        float pantallaAlto = Gdx.graphics.getHeight();

        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, pantallaAncho, pantallaAlto);
        game.batch.begin();

        // Filtro negro semi-transparente.
        game.batch.setColor(0, 0, 0, 0.5f);
        // Dibujamos un fondo oscuro (opcional, si no hay textura de fondo)
        // Pero el usuario pidió Pokepausa grande en el medio.
        game.batch.setColor(1, 1, 1, 1); // Reset de color.

        // Imagen Pokepausa grande en el medio
        float pokeW = pantallaAncho * 0.4f; // Reducido de 0.6f a 0.4f
        float pokeH = (pokeW / pausaPokepausa.getWidth()) * pausaPokepausa.getHeight();
        game.batch.draw(pausaPokepausa, pantallaAncho * 0.55f, (pantallaAlto - pokeH) / 2f, pokeW, pokeH);

        float btnW = pantallaAncho * 0.3f;
        float btnH = pantallaAlto * 0.1f;
        float x = pantallaAncho * 0.05f; // Margen del 5% desde la izquierda
        float centroY = pantallaAlto / 2f;

        // Boton Reanudar (arriba).
        Texture texReanudar = (opcionPausa == OPCION_REANUDAR) ? pausaVolverC : pausaVolver;
        game.batch.draw(texReanudar, x, centroY + btnH / 2f + 15, btnW, btnH);

        // Boton Salir al Menú Principal (abajo).
        Texture texSalir = (opcionPausa == OPCION_SALIR_MENU) ? pausaSalirC : pausaSalir;
        game.batch.draw(texSalir, x, centroY - btnH / 2f - 15, btnW, btnH);

        game.batch.end();

        // Restauramos la proyeccion de la camara para el siguiente frame.
        game.batch.setProjectionMatrix(camera.combined);
    }

    /**
     * Dibuja los elementos visuales del inventario siguiendo el mockup
     * proporcionado.
     */
    private void dibujarInventario() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
        game.batch.begin();

        // 1. Fondo Oscuro semi-transparente
        game.batch.setColor(0, 0, 0, 0.5f);
        game.batch.draw(pixel, 0, 0, sw, sh);
        game.batch.setColor(1, 1, 1, 1); // Reset para que los botones no sean transparentes

        // 2. BOTONES SUPERIORES (Craftear a la izquierda)
        float topBtnW = sw * 0.28f;
        float topBtnH = sh * 0.1f;
        float topBtnY = sh * 0.85f;
        float startX = 50; // Izquierda

        dibujarBotonTop(INV_CRAFTEAR, startX, topBtnY, topBtnW, topBtnH, true);

        // 3. CUADRO PRINCIPAL (Usando marcoInventario)
        float marcoW = sw * 0.85f;
        float marcoH = sh * 0.78f; // Expandido para evitar choque
        float marcoX = (sw - marcoW) / 2f;
        float marcoY = sh * 0.05f; // Bajado un poco

        game.batch.setColor(1, 1, 1, 1);
        game.batch.draw(marcoInventario, marcoX, marcoY, marcoW, marcoH);

        // 4. LISTA DE ITEMS (Fijos: Plastico, Goma, Madera)
        float itemW = marcoW * 0.45f;
        float itemH = marcoH * 0.18f;
        float itemX = marcoX + 70;
        float itemStartY = marcoY + marcoH - itemH - 80;

        String[] itemsFijos = { "Plastico", "Goma", "Madera" };
        HashMap<String, Integer> inv = jugador.getInventario().getMapa();

        for (int i = 0; i < itemsFijos.length; i++) {
            String itemNombre = itemsFijos[i];
            int cantidad = inv.getOrDefault(itemNombre, 0);
            float currentY = itemStartY - i * (itemH + 30);
            dibujarItemBox(itemNombre, cantidad, itemX, currentY, itemW, itemH);
        }

        // 5. SLOTS DE LA DERECHA (PokeballCura, PokeballEXP, Pokeball)
        float slotW = marcoW * 0.15f;
        float slotH = marcoH * 0.2f;
        float slotX = marcoX + marcoW - slotW - 100;

        String[] pokeballsFijas = { "PokeballCura", "PokeballEXP", "Pokeball" };
        float pbOffsetY = -15; // Bajar un poco las casillas
        for (int j = 0; j < pokeballsFijas.length; j++) {
            String pbNombre = pokeballsFijas[j];
            int cantidadpb = inv.getOrDefault(pbNombre, 0);
            float currentY = itemStartY - j * (slotH + 15) + pbOffsetY;

            // Dibujar el slot (resaltado si se tiene el item)
            dibujarSlot(slotX, currentY, slotW, slotH, cantidadpb > 0);

            // Dibujar el icono de la pokebola si se tiene
            if (cantidadpb > 0) {
                Texture pbTex = null;
                if (j == 0)
                    pbTex = texPokeCura;
                else if (j == 1)
                    pbTex = texPokeExp;
                else if (j == 2)
                    pbTex = texPokeball;

                if (pbTex != null) {
                    float iconSize = slotW * 0.6f;
                    game.batch.setColor(1, 1, 1, 1);
                    game.batch.draw(pbTex, slotX + (slotW - iconSize) / 2f, currentY + (slotH - iconSize) / 2f + 5,
                            iconSize, iconSize);

                    font.setColor(Color.WHITE);
                    font.getData().setScale(1.2f);
                    font.draw(game.batch, "x" + cantidadpb, slotX + slotW - 40, currentY + 30);
                    font.getData().setScale(1.5f);
                }
            }
        }

        game.batch.end();
        game.batch.setProjectionMatrix(camera.combined);
    }

    private void dibujarBotonTop(int tipo, float x, float y, float w, float h, boolean activo) {
        Texture tex = null;
        if (tipo == INV_CRAFTEAR)
            tex = activo ? texCraftearC : texCraftear;

        if (tex != null) {
            game.batch.draw(tex, x, y, w, h);
        }
    }

    private void dibujarItemBox(String nombre, int cantidad, float x, float y, float w, float h) {
        Texture tex = null;
        if (cantidad > 0) {
            if ("Plastico".equalsIgnoreCase(nombre))
                tex = marcoPlastico;
            else if ("Goma".equalsIgnoreCase(nombre))
                tex = marcoGoma;
            else if ("Madera".equalsIgnoreCase(nombre))
                tex = marcoMadera;
        } else {
            tex = marcoSlot; // Marco vacio por defecto
        }

        if (tex != null) {
            game.batch.setColor(1, 1, 1, 1);
            game.batch.draw(tex, x, y, w, h);
        } else {
            // Vacío
            game.batch.setColor(0, 0, 0, 0.2f);
            game.batch.draw(pixel, x, y, w, h);
            game.batch.setColor(1, 1, 1, 1);
        }

        // Cantidad (si es > 0)
        if (cantidad > 0) {
            font.setColor(Color.BLACK); // Usamos negro para que destaque sobre el verde/amarillo
            if (tex != null) {
                // Ajustamos posición del texto si usamos la imagen de 8 bits
                font.draw(game.batch, "x" + cantidad, x + w * 0.75f, y + h * 0.65f);
            } else {
                font.draw(game.batch, "x" + cantidad, x + w - 60, y + h / 2f + 10);
            }
        }
    }

    private void dibujarSlot(float x, float y, float w, float h, boolean resaltado) {
        Texture tex = resaltado ? marcoSlotC : marcoSlot;
        game.batch.draw(tex, x, y, w, h);
    }

    /**
     * Gestiona la entrada del teclado cuando el inventario esta abierto.
     */
    private void actualizarEntradaInventario() {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            // Ir a crafteo (siempre activo)
            menuCrafteoAbierto = true;
            opcionCrafteo = 1;
        }
    }

    /**
     * Gestiona la entrada del teclado cuando el menú de Pokemon está abierto.
     */
    private void actualizarEntradaPokemon() {
        java.util.List<Pokemon> pokemons = sistemaCaptura.getPokemonsCapturados();
        
        if (pokemons.isEmpty()) {
            // Si no hay Pokemon, cerrar el menú con cualquier tecla
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE) || 
                Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.P)) {
                menuPokemonAbierto = false;
            }
            return;
        }

        // Navegar con flechas arriba/abajo
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {
            pokemonSeleccionado = (pokemonSeleccionado - 1 + pokemons.size()) % pokemons.size();
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
            pokemonSeleccionado = (pokemonSeleccionado + 1) % pokemons.size();
        }

        // Cerrar con ESCAPE o P
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE) || 
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.P)) {
            menuPokemonAbierto = false;
        }
    }

    /**
     * Dibuja el menú de Pokemon capturados con todas sus especificaciones.
     */
    private void dibujarMenuPokemon() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
        game.batch.begin();

        // Fondo oscuro semi-transparente
        game.batch.setColor(0, 0, 0, 0.8f);
        game.batch.draw(pixel, 0, 0, sw, sh);
        game.batch.setColor(1, 1, 1, 1);

        java.util.List<Pokemon> pokemons = sistemaCaptura.getPokemonsCapturados();

        // Título
        font.setColor(Color.YELLOW);
        font.getData().setScale(2.5f);
        String titulo = "POKEMON CAPTURADOS";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layoutTitulo = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, titulo);
        font.draw(game.batch, titulo, (sw - layoutTitulo.width) / 2f, sh - 50);

        if (pokemons.isEmpty()) {
            // Mensaje si no hay Pokemon
            font.setColor(Color.WHITE);
            font.getData().setScale(2.0f);
            String mensaje = "No has capturado ningún Pokemon aún";
            com.badlogic.gdx.graphics.g2d.GlyphLayout layoutMensaje = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, mensaje);
            font.draw(game.batch, mensaje, (sw - layoutMensaje.width) / 2f, sh / 2f);

            font.getData().setScale(1.5f);
            String instruccion = "Presiona P o ESCAPE para cerrar";
            com.badlogic.gdx.graphics.g2d.GlyphLayout layoutInst = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, instruccion);
            font.draw(game.batch, instruccion, (sw - layoutInst.width) / 2f, sh / 2f - 60);
        } else {
            // Área de lista de Pokemon (izquierda)
            float listaX = 50;
            float listaY = sh - 150;
            float listaW = sw * 0.35f;
            float listaH = sh - 200;
            float itemHeight = 60f;
            int itemsVisibles = (int) (listaH / itemHeight);
            
            // Fondo de la lista
            game.batch.setColor(0.15f, 0.15f, 0.15f, 0.9f);
            game.batch.draw(pixel, listaX, listaY - listaH, listaW, listaH);
            game.batch.setColor(1, 1, 1, 1);

            // Borde de la lista
            game.batch.setColor(0.6f, 0.6f, 0.6f, 1f);
            float bordeGrosor = 3f;
            game.batch.draw(pixel, listaX, listaY - listaH, listaW, bordeGrosor); // Arriba
            game.batch.draw(pixel, listaX, listaY - listaH, bordeGrosor, listaH); // Izquierda
            game.batch.draw(pixel, listaX + listaW - bordeGrosor, listaY - listaH, bordeGrosor, listaH); // Derecha
            game.batch.draw(pixel, listaX, listaY - listaH - bordeGrosor, listaW, bordeGrosor); // Abajo
            game.batch.setColor(1, 1, 1, 1);

            // Dibujar lista de Pokemon
            font.getData().setScale(1.3f);
            int startIndex = Math.max(0, Math.min(pokemonSeleccionado - itemsVisibles / 2, 
                Math.max(0, pokemons.size() - itemsVisibles)));
            
            for (int i = 0; i < itemsVisibles && (startIndex + i) < pokemons.size(); i++) {
                int index = startIndex + i;
                Pokemon p = pokemons.get(index);
                float itemY = listaY - (i * itemHeight) - 30;
                
                // Resaltar el seleccionado
                if (index == pokemonSeleccionado) {
                    game.batch.setColor(0.3f, 0.5f, 0.8f, 0.7f);
                    game.batch.draw(pixel, listaX + 5, itemY - itemHeight + 5, listaW - 10, itemHeight - 5);
                    game.batch.setColor(1, 1, 1, 1);
                }

                // Nombre del Pokemon
                font.setColor(index == pokemonSeleccionado ? Color.YELLOW : Color.WHITE);
                String nombreTexto = (index + 1) + ". " + p.getNombre();
                font.draw(game.batch, nombreTexto, listaX + 15, itemY);
            }

            // Área de detalles del Pokemon seleccionado (derecha)
            if (pokemonSeleccionado >= 0 && pokemonSeleccionado < pokemons.size()) {
                Pokemon p = pokemons.get(pokemonSeleccionado);
                float detallesX = listaX + listaW + 30;
                float detallesY = listaY;
                float detallesW = sw - detallesX - 50;
                float detallesH = listaH;

                // Fondo de detalles
                game.batch.setColor(0.2f, 0.2f, 0.2f, 0.9f);
                game.batch.draw(pixel, detallesX, detallesY - detallesH, detallesW, detallesH);
                game.batch.setColor(1, 1, 1, 1);

                // Borde de detalles
                game.batch.setColor(0.8f, 0.8f, 0.8f, 1f);
                game.batch.draw(pixel, detallesX, detallesY, detallesW, bordeGrosor); // Arriba
                game.batch.draw(pixel, detallesX, detallesY - detallesH, bordeGrosor, detallesH); // Izquierda
                game.batch.draw(pixel, detallesX + detallesW - bordeGrosor, detallesY - detallesH, bordeGrosor, detallesH); // Derecha
                game.batch.draw(pixel, detallesX, detallesY - detallesH - bordeGrosor, detallesW, bordeGrosor); // Abajo
                game.batch.setColor(1, 1, 1, 1);

                // Título de detalles
                font.setColor(Color.CYAN);
                font.getData().setScale(2.0f);
                String detalleTitulo = "ESPECIFICACIONES";
                com.badlogic.gdx.graphics.g2d.GlyphLayout layoutDetalle = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, detalleTitulo);
                font.draw(game.batch, detalleTitulo, detallesX + (detallesW - layoutDetalle.width) / 2f, detallesY - 30);

                // Información del Pokemon
                float yPos = detallesY - 80;
                font.setColor(Color.WHITE);
                font.getData().setScale(1.8f);
                
                // Nombre
                font.setColor(Color.YELLOW);
                font.draw(game.batch, "Nombre:", detallesX + 30, yPos);
                font.setColor(Color.WHITE);
                font.draw(game.batch, p.getNombre(), detallesX + 200, yPos);
                yPos -= 50;

                // Tipo
                font.setColor(Color.YELLOW);
                font.draw(game.batch, "Tipo:", detallesX + 30, yPos);
                font.setColor(Color.WHITE);
                String tipoTexto = p.getTipoString(); // getTipoString() devuelve String
                font.draw(game.batch, tipoTexto, detallesX + 200, yPos);
                yPos -= 50;

                // Sexo
                font.setColor(Color.YELLOW);
                font.draw(game.batch, "Sexo:", detallesX + 30, yPos);
                font.setColor(Color.WHITE);
                font.draw(game.batch, p.getSexo(), detallesX + 200, yPos);
                yPos -= 50;

                // Peso
                font.setColor(Color.YELLOW);
                font.draw(game.batch, "Peso:", detallesX + 30, yPos);
                font.setColor(Color.WHITE);
                font.draw(game.batch, String.format("%.2f kg", p.getPeso()), detallesX + 200, yPos);
                yPos -= 50;

                // Vida
                font.setColor(Color.YELLOW);
                font.draw(game.batch, "Vida (PS):", detallesX + 30, yPos);
                font.setColor(Color.WHITE);
                String vidaTexto = p.getVida() + " / " + p.getVidaMaxima();
                font.draw(game.batch, vidaTexto, detallesX + 200, yPos);
                yPos -= 50;

                // Barra de vida visual
                float barraX = detallesX + 200;
                float barraY = yPos - 20;
                float barraW = 300;
                float barraH = 25;
                
                // Fondo de la barra
                game.batch.setColor(0.3f, 0.3f, 0.3f, 1f);
                game.batch.draw(pixel, barraX, barraY, barraW, barraH);
                
                // Barra de vida (verde/amarillo/rojo según porcentaje)
                float porcentajeVida = (float) p.getVida() / p.getVidaMaxima();
                if (porcentajeVida > 0.5f) {
                    game.batch.setColor(0, 1, 0, 1f); // Verde
                } else if (porcentajeVida > 0.25f) {
                    game.batch.setColor(1, 1, 0, 1f); // Amarillo
                } else {
                    game.batch.setColor(1, 0, 0, 1f); // Rojo
                }
                game.batch.draw(pixel, barraX, barraY, barraW * porcentajeVida, barraH);
                game.batch.setColor(1, 1, 1, 1);

                // Borde de la barra
                game.batch.setColor(0.8f, 0.8f, 0.8f, 1f);
                game.batch.draw(pixel, barraX, barraY, barraW, 2); // Arriba
                game.batch.draw(pixel, barraX, barraY, 2, barraH); // Izquierda
                game.batch.draw(pixel, barraX + barraW - 2, barraY, 2, barraH); // Derecha
                game.batch.draw(pixel, barraX, barraY + barraH - 2, barraW, 2); // Abajo
                game.batch.setColor(1, 1, 1, 1);

                yPos -= 60;

                // Estado
                font.setColor(Color.YELLOW);
                font.getData().setScale(1.5f);
                font.draw(game.batch, "Estado:", detallesX + 30, yPos);
                font.setColor(p.estaVivo() ? Color.GREEN : Color.RED);
                font.draw(game.batch, p.estaVivo() ? "Vivo" : "Derrotado", detallesX + 200, yPos);
            }

            // Instrucciones en la parte inferior
            font.setColor(Color.LIGHT_GRAY);
            font.getData().setScale(1.2f);
            String instrucciones = "↑ ↓: Navegar | P o ESCAPE: Cerrar";
            com.badlogic.gdx.graphics.g2d.GlyphLayout layoutInst = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, instrucciones);
            font.draw(game.batch, instrucciones, (sw - layoutInst.width) / 2f, 40);

            // Contador de Pokemon
            font.setColor(Color.CYAN);
            String contador = "Total: " + pokemons.size() + " Pokemon";
            com.badlogic.gdx.graphics.g2d.GlyphLayout layoutCont = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, contador);
            font.draw(game.batch, contador, sw - layoutCont.width - 30, 40);
        }

        game.batch.end();
        game.batch.setProjectionMatrix(camera.combined);
    }

    /**
     * Gestiona la entrada del teclado cuando hay un encuentro con Pokemon.
     */
    private void actualizarEntradaEncuentro() {
        if (pokemonSalvaje == null || !pokemonSalvaje.estaVivo()) {
            // Si el Pokemon fue derrotado o no existe, cerrar encuentro
            enEncuentro = false;
            pokemonSalvaje = null;
            return;
        }

        // Presionar ENTER para intentar capturar con Pokeball
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            try {
                boolean capturado = sistemaCaptura.intentarCapturar(pokemonSalvaje, "Pokeball");
                if (capturado) {
                    enEncuentro = false;
                    pokemonSalvaje = null;
                }
            } catch (ExcepcionPokebolaInsuficiente e) {
                mostrarError(e.getMessage());
            }
        }

        // Presionar ESPACIO para huir del encuentro
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            System.out.println("Huyes del encuentro...");
            enEncuentro = false;
            pokemonSalvaje = null;
        }
    }

    /**
     * Dibuja la pantalla de encuentro con Pokemon salvaje.
     */
    private void dibujarEncuentroPokemon() {
        if (pokemonSalvaje == null) {
            return;
        }

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
        game.batch.begin();

        // Fondo oscuro semi-transparente
        game.batch.setColor(0, 0, 0, 0.7f);
        game.batch.draw(pixel, 0, 0, sw, sh);
        game.batch.setColor(1, 1, 1, 1);

        // Cuadro de información del Pokemon
        float cuadroW = sw * 0.7f;
        float cuadroH = sh * 0.5f;
        float cuadroX = (sw - cuadroW) / 2f;
        float cuadroY = (sh - cuadroH) / 2f;

        // Fondo del cuadro
        game.batch.setColor(0.2f, 0.2f, 0.2f, 0.9f);
        game.batch.draw(pixel, cuadroX, cuadroY, cuadroW, cuadroH);
        game.batch.setColor(1, 1, 1, 1);

        // Borde del cuadro
        game.batch.setColor(0.8f, 0.8f, 0.8f, 1f);
        float bordeGrosor = 5f;
        game.batch.draw(pixel, cuadroX, cuadroY, cuadroW, bordeGrosor); // Arriba
        game.batch.draw(pixel, cuadroX, cuadroY + cuadroH - bordeGrosor, cuadroW, bordeGrosor); // Abajo
        game.batch.draw(pixel, cuadroX, cuadroY, bordeGrosor, cuadroH); // Izquierda
        game.batch.draw(pixel, cuadroX + cuadroW - bordeGrosor, cuadroY, bordeGrosor, cuadroH); // Derecha
        game.batch.setColor(1, 1, 1, 1);

        // Información del Pokemon
        font.setColor(Color.WHITE);
        font.getData().setScale(2.0f);
        String mensaje = "¡Un " + pokemonSalvaje.getNombre() + " salvaje apareció!";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, mensaje);
        font.draw(game.batch, mensaje, cuadroX + (cuadroW - layout.width) / 2f, cuadroY + cuadroH - 50);

        font.getData().setScale(1.5f);
        String info = pokemonSalvaje.toString();
        font.draw(game.batch, info, cuadroX + 30, cuadroY + cuadroH - 120);

        // Instrucciones
        font.getData().setScale(1.2f);
        font.setColor(Color.YELLOW);
        String instrucciones = "ENTER: Capturar con Pokeball | ESPACIO: Huir";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layoutInst = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, instrucciones);
        font.draw(game.batch, instrucciones, cuadroX + (cuadroW - layoutInst.width) / 2f, cuadroY + 50);

        // Mostrar probabilidad de captura
        if (sistemaCaptura != null) {
            try {
                double probabilidad = sistemaCaptura.calcularProbabilidadCaptura(pokemonSalvaje, "Pokeball");
                String probTexto = "Probabilidad de captura: " + String.format("%.1f%%", probabilidad * 100);
                font.setColor(Color.CYAN);
                font.getData().setScale(1.0f);
                com.badlogic.gdx.graphics.g2d.GlyphLayout layoutProb = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, probTexto);
                font.draw(game.batch, probTexto, cuadroX + (cuadroW - layoutProb.width) / 2f, cuadroY + 30);
            } catch (Exception e) {
                // Ignorar errores al calcular probabilidad
            }
        }

        game.batch.end();
        game.batch.setProjectionMatrix(camera.combined);
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
        // NO disponemos al jugador aqui porque es persistente en Main.
        pausaSalir.dispose();
        pausaSalirC.dispose();
        pausaVolver.dispose();
        pausaVolverC.dispose();
        pausaOpciones.dispose();
        pausaOpcionesC.dispose();
        pausaPokepausa.dispose();
        marcoInventario.dispose();
        pixel.dispose();
        font.dispose();
        texCraftear.dispose();
        texCraftearC.dispose();
        marcoPlastico.dispose();
        marcoGoma.dispose();
        marcoMadera.dispose();
        marcoSlot.dispose();
        marcoSlotC.dispose();
        texPokeCura.dispose();
        texPokeExp.dispose();
        texPokeball.dispose();
        marcoCrafteoSeleccionado.dispose();
        if (marcoCrafteoNoSeleccionado != null)
            marcoCrafteoNoSeleccionado.dispose();
    }

    /**
     * Dibuja el menu de crafteo con 3 opciones.
     */
    private void dibujarMenuCrafteo() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
        game.batch.begin();

        // Fondo oscuro
        game.batch.setColor(0, 0, 0, 0.7f);
        game.batch.draw(pixel, 0, 0, sw, sh);
        game.batch.setColor(1, 1, 1, 1);

        // Dimensiones de los marcos
        float frameW = sw * 0.25f;
        float frameH = sh * 0.5f;
        float spacing = sw * 0.05f;

        // Calculamos X inicial para que esten centrados
        float totalW = (frameW * 3) + (spacing * 2);
        float startX = (sw - totalW) / 2f;
        float y = (sh - frameH) / 2f;

        // Datos para las recetas
        String[] nombresItems = { "Pokeball", "PokeballEXP", "PokeballCura" };
        Texture[] iconos = { texPokeball, texPokeExp, texPokeCura };
        String[] recetasTexto = {
                "Plastico\nGoma",
                "Madera\nGoma",
                "Plastico\nMadera"
        };

        for (int i = 0; i < 3; i++) {
            float x = startX + i * (frameW + spacing);

            // Elegir marco segun seleccion
            // NOTA: Si opcionCrafteo == 3 (Salir), ninguno de estos debe estar
            // seleccionado.
            boolean isSelected = (i == opcionCrafteo);
            Texture marco = isSelected ? marcoCrafteoSeleccionado : marcoCrafteoNoSeleccionado;
            if (marco == null)
                marco = marcoCrafteoSeleccionado; // Fallback

            // Dibujar marco
            game.batch.setColor(1, 1, 1, 1);
            game.batch.draw(marco, x, y, frameW, frameH);

            // Dibujar Icono
            float iconSize = frameW * 0.4f;
            float iconX = x + (frameW - iconSize) / 2f;
            float iconY = y + frameH * 0.55f;
            game.batch.draw(iconos[i], iconX, iconY, iconSize, iconSize);

            // Dibujar Nombre debajo del Icono
            font.setColor(Color.BLACK);
            font.getData().setScale(1.3f);
            String nombre = nombresItems[i];
            com.badlogic.gdx.graphics.g2d.GlyphLayout layoutN = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font,
                    nombre);
            font.draw(game.batch, nombre, x + (frameW - layoutN.width) / 2f, iconY - 10);

            // Dibujar Texto (Ingredientes)
            font.getData().setScale(1.2f);

            String texto = recetasTexto[i];
            String[] lineas = texto.split("\n");
            float textY = y + frameH * 0.35f; // Mas abajo
            for (String linea : lineas) {
                // Centrado aproximado
                com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font,
                        linea);
                float textX = x + (frameW - layout.width) / 2f;
                font.draw(game.batch, linea, textX, textY);
                textY -= 40;
            }
        }

        game.batch.setColor(1, 1, 1, 1);
        game.batch.end();
        game.batch.setProjectionMatrix(camera.combined);
    }

    /**
     * Gestiona la entrada en el menu de crafteo.
     */
    private void actualizarEntradaCrafteo() {
        // Navegacion horizontal entre slots
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            opcionCrafteo = (opcionCrafteo - 1 + 3) % 3;
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            opcionCrafteo = (opcionCrafteo + 1) % 3;
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            menuCrafteoAbierto = false; // Volver al inventario
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            try {
                intentarCrafteo();
            } catch (ExcepcionMaterialesInsuficientes | ExcepcionInventarioLleno e) {
                mostrarError(e.getMessage());
            }
        }
    }

    private void intentarCrafteo() throws ExcepcionMaterialesInsuficientes, ExcepcionInventarioLleno {
        HashMap<String, Integer> inv = jugador.getInventario().getMapa();
        String i1 = "", i2 = "";
        String producto = "";
        String tipoProducto = "pokeball";

        // Definir recetas
        if (opcionCrafteo == 0) { // Pokeball
            i1 = "Plastico";
            i2 = "Goma";
            producto = "Pokeball";
        } else if (opcionCrafteo == 1) { // PokeballEXP
            i1 = "Madera";
            i2 = "Goma";
            producto = "PokeballEXP";
        } else if (opcionCrafteo == 2) { // PokeballCura
            i1 = "Plastico";
            i2 = "Madera";
            producto = "PokeballCura";
        }

        int c1 = inv.getOrDefault(i1, 0);
        int c2 = inv.getOrDefault(i2, 0);

        if (c1 >= 1 && c2 >= 1) {
            // Verificar espacio antes de consumir
            // NOTA: AgregarObjeto lanzará excepcion si está lleno, pero es mejor saber
            // antes.
            // Para ser atomicos, simularemos validacion.

            // Si el objeto ya existe y tiene >= 10, agregarObjeto tirara excepcion.
            // Asi que consumimos y luego agregamos? No, si falla el agregar perdemos items.
            // Primero intentamos agregar (dry run) o confiamos en la excepcion.
            // Almacenamiento no tiene metodo canAdd pero agregarObjeto verifica.
            // Sin embargo agregarObjeto modifica el estado.
            // Verificamos manualmente:
            int cantProd = inv.getOrDefault(producto, 0);
            if (cantProd >= 10) { // 10 es MAX_ITEMS, debería ser publico en Almacenamiento o hardcoded igual
                throw new ExcepcionInventarioLleno("Inventario lleno para " + producto);
            }

            // Consumir
            inv.put(i1, c1 - 1);
            inv.put(i2, c2 - 1);

            try {
                // Producir
                jugador.getInventario().agregarObjeto(producto, tipoProducto);
                System.out.println("Crafteado: " + producto);
            } catch (ExcepcionInventarioLleno e) {
                // Si falla (race condition rara), devolver materiales
                inv.put(i1, c1);
                inv.put(i2, c2);
                throw e;
            }

        } else {
            throw new ExcepcionMaterialesInsuficientes("Faltan materiales para " + producto);
        }
    }

    private void mostrarError(String mensaje) {
        this.mostrandoError = true;
        this.mensajeError = mensaje;
        this.tiempoMensajeError = 3.0f; // 3 segundos
    }

    private void dibujarCuadroError(float delta) {
        if (!mostrandoError)
            return;

        tiempoMensajeError -= delta;
        if (tiempoMensajeError <= 0) {
            mostrandoError = false;
        }

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
        game.batch.begin();

        // Cuadro Rojo de Error
        float w = sw * 0.6f;
        float h = sh * 0.2f;
        float x = (sw - w) / 2f;
        float y = (sh - h) / 2f;

        game.batch.setColor(0.8f, 0, 0, 0.9f);
        game.batch.draw(pixel, x, y, w, h);

        // Borde
        game.batch.setColor(1, 1, 1, 1);
        // Marco simple (opcional)
        // ...

        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font,
                mensajeError);
        font.draw(game.batch, mensajeError, x + (w - layout.width) / 2f, y + (h + layout.height) / 2f);

        game.batch.end();
        game.batch.setProjectionMatrix(camera.combined);
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
