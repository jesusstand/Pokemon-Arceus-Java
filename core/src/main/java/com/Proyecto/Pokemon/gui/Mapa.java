package com.Proyecto.Pokemon.gui;

import com.Proyecto.Pokemon.Main;
import com.Proyecto.Pokemon.NPC;
import com.Proyecto.Pokemon.PantallaBatalla;
import com.Proyecto.Pokemon.jugador.Player;
import com.Proyecto.Pokemon.pokemon.Pokemon;
import com.Proyecto.Pokemon.pokemon.PokeFuego;
import com.Proyecto.Pokemon.sistema.SpawnPokemon;
import com.Proyecto.Pokemon.sistema.CapturaPokemon;
import com.Proyecto.Pokemon.sistema.GestorGuardado;
import com.Proyecto.Pokemon.sistema.GestorMusica;
import com.Proyecto.Pokemon.excepciones.ExcepcionInventarioLleno;
import com.Proyecto.Pokemon.excepciones.ExcepcionMaterialesInsuficientes;
import com.Proyecto.Pokemon.excepciones.ExcepcionPokebolaInsuficiente;
import com.Proyecto.Pokemon.excepciones.ExcepcionEquipoLleno;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import java.util.List;
import com.badlogic.gdx.utils.Array;

/**
 * Pantalla principal del juego donde ocurre la accion.
 * Gestiona el mapa de Tiled, el renderizado de los graficos y la interaccion
 * del jugador.
 */
public class Mapa implements Screen {
    // --- CAMPOS DEL MAPA ---
    private Main game;
    private TiledMap mapaTiled;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Player jugador;
    private String nombreMapa; // Added missing field
    private float grassTimer = 0;
    private Array<Rectangle> zonasHierba = new Array<>();
    private java.util.List<NPC> npcs;

    // --- ESTADO DE PAUSA ---
    private boolean pausado = false;
    private boolean inventarioAbierto = false;
    private boolean menuEquipoAbierto = false; // Estado para menï¿½ equipo lateral (R)
    private int opcionPausa = 0; // 0: Volver, 1: Opciones, 2: Salir
    private boolean saliendoAlMenu = false; // Bandera para indicar que estamos saliendo
    private boolean modoSoltar = false;
    private boolean botonSoltarSeleccionado = false; // Foco en el botón soltar (arriba de la lista)
    private Texture texSoltarLetra, texBotonSoltar, texBotonSoltarColor;
    // --- ESTADO CRAFTEO ---
    private boolean menuCrafteoAbierto = false;
    private int opcionCrafteo = 1; // 0, 1, 2
    // --- ESTADO POKEMON CAPTURADOS ---
    // Nota: Ahora usamos PantallaPokemonCapturados en lugar de un menú interno
    private Texture marcoCrafteoSeleccionado;
    private Texture marcoCrafteoNoSeleccionado;
    private Texture pausaSalir, pausaSalirC, pausaVolver, pausaVolverC, pausaOpciones, pausaOpcionesC, pausaPokepausa;
    private Texture marcoInventario, pixel;
    private Texture texCraftear, texCraftearC;
    private Texture marcoPlastico, marcoGoma, marcoMadera, marcoSlot, marcoSlotC;
    private Texture texPokeCura, texPokeExp, texPokeball;
    private BitmapFont font, fontPequeña;
    private HashMap<String, Texture> marcosPokemon = new HashMap<>();
    private HashMap<String, Texture> marcosPokemonVacio = new HashMap<>();
    private int equipoSeleccionado = 0; // Para cerrar el menu o navegar si fuera necesario

    // --- ESTADO ERROR UI ---
    private boolean mostrandoError = false;
    private String mensajeError = "";
    private float tiempoMensajeError = 0;

    // --- ESTADO DIALOGO ---
    private boolean mostrandoDialogo = false;
    private String textoActual = "";
    private boolean mostrandoPokedex = false;

    // --- ESTADO ENCUENTRO POKEMON ---
    private SpawnPokemon spawnPokemon;
    private Pokemon pokemonSalvaje;
    private boolean enEncuentro = false;
    private CapturaPokemon sistemaCaptura;
    private GestorSpritesPokemon gestorSprites = new GestorSpritesPokemon();

    // --- ESTADO CURACIÓN CENTRO POKÉMON ---
    private boolean menuCuracionAbierto = false;
    private int opcionCuracion = 0; // 0: Aceptar, 1: Cancelar
    private Texture texCurarEquipo, texBotonAceptar, texBotonAceptarBase, texBotonCancelar, texBotonCancelarBase;
    private Texture marcoInfo;

    private static final int OPCION_REANUDAR = 0;
    private static final int OPCION_SALIR_MENU = 1;
    private static final int CANTIDAD_OPCIONES = 2;

    private static final int INV_CRAFTEAR = 0;

    private float anchoMapa, altoMapa;
    // Escala unitaria: 1 unidad de mundo = 16 pixeles (tama├▒o de un tile).
    private static final float UNIT_SCALE = 1 / 16f;

    // --- ESCALA VIRTUAL PARA UI ---
    private static final float UI_WIDTH = 1280f;
    private static final float UI_HEIGHT = 720f;

    // --- REFERENCIA AL NPC CON EL QUE SE HABLA ---
    private NPC npcActual = null;

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

                    // Si el tile tiene formas de colisi├│n, bloquea.
                    if (cell.getTile().getObjects().getCount() > 0) {
                        return true;
                    }
                }
            }
        }

        // Si no hay ninguna baldosa en ninguna capa en esa posición, es el vacío
        // (sólido).
        if (!tieneSuelo)
            return true;

        // Verificar colision con NPCs
        if (npcs != null) {
            for (NPC npc : npcs) {
                if (npc.isSolido()) {
                    // Si el NPC esta en la coordenada x, y
                    if ((int) npc.getPosicion().x == x && (int) npc.getPosicion().y == y) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Verifica si el jugador está en la zona de curación del Centro Pokémon.
     * 
     * @return true si está en la zona de curación, false en caso contrario.
     */
    private boolean estaEnZonaCuracion() {
        MapLayer capaZonas = mapaTiled.getLayers().get("ZonasEspeciales");
        if (capaZonas == null) {
            return false;
        }

        // Usamos el centro del jugador para una detección más precisa
        float centroX = jugador.getX() + 0.5f;
        float centroY = jugador.getY() + 0.5f;

        for (MapObject objeto : capaZonas.getObjects()) {
            if (objeto instanceof RectangleMapObject) {
                String nombreObjeto = objeto.getName();
                // Buscamos objetos con nombre "Zona curacion" o "ZonaCuracion" (insensible a
                // mayúsculas)
                if (nombreObjeto != null &&
                        (nombreObjeto.equalsIgnoreCase("Zona curacion") ||
                                nombreObjeto.equalsIgnoreCase("ZonaCuracion"))) {
                    Rectangle rect = ((RectangleMapObject) objeto).getRectangle();
                    // Escalamos el área al tamaño del mundo
                    float rectX = rect.x * UNIT_SCALE;
                    float rectY = rect.y * UNIT_SCALE;
                    float rectW = rect.width * UNIT_SCALE;
                    float rectH = rect.height * UNIT_SCALE;

                    // Verificamos si el centro del jugador está dentro de la zona
                    if (centroX >= rectX && centroX <= rectX + rectW &&
                            centroY >= rectY && centroY <= rectY + rectH) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Cura todos los Pokémon del jugador al máximo.
     * Incluye el Pokémon inicial y todos los Pokémon capturados.
     */
    private void curarEquipoCompleto() {
        int pokemonsCurados = 0;

        // 2. Curar todos los Pokémon del equipo
        List<Pokemon> pokemonsEquipo = jugador.getEquipo().getPokemons();
        for (Pokemon pokemon : pokemonsEquipo) {
            if (pokemon != null) {
                pokemon.curar();
                pokemonsCurados++;
                System.out.println("✓ " + pokemon.getNombre() + " curado exitosamente");
            }
        }

        // Mostrar mensaje de confirmación
        if (pokemonsCurados > 0) {
            mostrarDialogo("¡Todos tus Pokémon han sido curados exitosamente!");
        } else {
            mostrarDialogo("No tienes Pokémon para curar.");
        }
    }

    public boolean estaEnHierba(float x, float y) {
        // 1. Verificar por rectángulos de zona (LogicaHierba)
        for (Rectangle zona : zonasHierba) {
            if (zona.contains(x, y)) {
                return true;
            }
        }

        // 2. Verificar por propiedades de tiles
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
        // Se ha movido la lógica al render() con temporizador de 5 segundos
    }

    /**
     * Detecta si el jugador esta en un portal y cambia de mapa.
     * Usa el centro del jugador para una detección más precisa.
     *
     * @param x Coordenada X del jugador.
     * @param y Coordenada Y del jugador.
     */
    public void revisarPortales(float x, float y) {
        // 1. Revisar Portales Normales
        for (MapLayer layer : mapaTiled.getLayers()) {
            String layerName = layer.getName().toLowerCase();
            if (layerName.contains("portal")) {
                for (MapObject objeto : layer.getObjects()) {
                    if (objeto instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) objeto).getRectangle();
                        if (checkCollision(x, y, rect)) {
                            String siguienteMapa = objeto.getProperties().get("Destino", String.class);
                            if (siguienteMapa != null) {

                                String mapaActual = nombreMapa;
                                if (mapaActual.endsWith(".tmx")) {
                                    mapaActual = mapaActual.substring(0, mapaActual.length() - 4);
                                }
                                cambiarMapa(siguienteMapa, mapaActual);
                                return;
                            }
                        }
                    }
                }
            }
        }

        // 2. Revisar Jefes (Capa PeleaBoss)
        MapLayer bossLayer = mapaTiled.getLayers().get("PeleaBoss");
        if (bossLayer != null) {
            for (MapObject objeto : bossLayer.getObjects()) {
                if (objeto instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) objeto).getRectangle();
                    if (checkCollision(x, y, rect)) {
                        String tipoPelea = objeto.getProperties().get("Pelea", String.class);
                        if (tipoPelea == null)
                            return;

                        Pokemon boss = null;
                        if (tipoPelea.equalsIgnoreCase("boss1")) {
                            if (!jugador.getPokedex().esPokedexCompleta()) {
                                mostrarError(
                                        "Pokedex Incompleta: Completa los puntos de investigacion para poder avanzar");
                                return;
                            }
                            boss = new com.Proyecto.Pokemon.pokemon.PokeDragon.Aethergon("Macho", 10);
                        } else if (tipoPelea.equalsIgnoreCase("boss2")) {
                            boss = new com.Proyecto.Pokemon.pokemon.PokeDragon.Dracornea("Hembra", 8);
                        }

                        if (boss != null) {
                            // Iniciar batalla
                            // Usamos miPokemon del jugador (el primero vivo)
                            Pokemon miPokemon = null;
                            for (Pokemon p : jugador.getEquipo().getPokemons()) {
                                if (p.estaVivo()) {
                                    miPokemon = p;
                                    break;
                                }
                            }

                            if (miPokemon != null) {
                                System.out.println("Iniciando batalla contra Boss: " + boss.getNombre());
                                game.setScreen(new PantallaBatalla(game, this, miPokemon, boss, false)); // false =
                                                                                                         // salvaje/boss
                                enEncuentro = false; // Asegurar estado
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Verifica si el jugador colisiona con un rectángulo dado.
     * 
     * @param playerX Posición X del jugador.
     * @param playerY Posición Y del jugador.
     * @param rect    Rectángulo con el que se verifica la colisión.
     * @return true si hay colisión, false en caso contrario.
     */
    private boolean checkCollision(float playerX, float playerY, Rectangle rect) {
        float rectX = rect.x * UNIT_SCALE;
        float rectY = rect.y * UNIT_SCALE;
        float rectW = rect.width * UNIT_SCALE;
        float rectH = rect.height * UNIT_SCALE;
        return playerX < rectX + rectW && playerX + 1 > rectX && playerY < rectY + rectH && playerY + 1 > rectY;
    }

    /**
     * @deprecated Usar revisarPortales() que ahora es genérico.
     */
    public void revisarPortalCentro(float playerX, float playerY) {
        revisarPortales(playerX, playerY);
    }

    /**
     * Cambia el mapa actual por uno nuevo.
     * 
     * @param nombreArchivo Nombre del archivo .tmx del nuevo mapa.
     * @param spawnPoint    Nombre del punto de spawn o mapa anterior para
     *                      determinar la posición inicial.
     */
    private void cambiarMapa(String nombreArchivo, String spawnPoint) {
        if (!nombreArchivo.endsWith(".tmx"))
            nombreArchivo += ".tmx";
        if (!nombreArchivo.startsWith("Tiled/"))
            nombreArchivo = "Tiled/" + nombreArchivo;

        System.out.println("Cambiando a: " + nombreArchivo + " en spawn: " + spawnPoint);

        // Aquí usamos los dos parámetros para el constructor
        game.setScreen(new Mapa(game, nombreArchivo, spawnPoint));
        this.dispose();
    }

    /**
     * Bucle de renderizado principal de la pantalla.
     *
     * @param delta Tiempo transcurrido entre el frame actual y el anterior.
     */
    @Override
    public void render(float delta) {
        // Si estamos saliendo al menú, no renderizar nada más
        if (saliendoAlMenu) {
            return;
        }

        // Manejo de teclas globales (fuera de los menús)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (!inventarioAbierto && !menuCrafteoAbierto && !menuEquipoAbierto) {
                pausado = !pausado;
            } else if (inventarioAbierto || menuCrafteoAbierto) {
                inventarioAbierto = false;
                menuCrafteoAbierto = false;
            }
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.E)) {
            if (!pausado) {
                if (menuCrafteoAbierto) {
                    menuCrafteoAbierto = false;
                    inventarioAbierto = true;
                } else {
                    inventarioAbierto = !inventarioAbierto;
                }
            }
        }

        // Lógica de pausa y otros menús
        if (pausado) {
            actualizarEntradaPausa();
        } else if (mostrandoDialogo) {
            actualizarEntradaDialogo();
        } else if (mostrandoPokedex) {
            actualizarEntradaPokedex();
        } else if (enEncuentro) {
            actualizarEntradaEncuentro();
        } else if (menuCrafteoAbierto) {
            actualizarEntradaCrafteo();
        } else if (inventarioAbierto) {
            actualizarEntradaInventario();
        } else if (menuEquipoAbierto) {
            actualizarEntradaEquipo();
        } else if (menuCuracionAbierto) {
            actualizarEntradaCuracion();
        } else {
            // Lógica normal de juego
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
                menuEquipoAbierto = true;
                equipoSeleccionado = 0;
                preCargarMarcosEquipo();
            }

            jugador.update(delta, this);

            // 1. Lógica de curación
            if (estaEnZonaCuracion() && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.C)) {
                curarEquipoCompleto();
            }

            // 2. Lógica de encuentros en hierba
            if (!enEncuentro && estaEnHierba(jugador.getX(), jugador.getY()) && jugador.isMoviendose()) {
                grassTimer += delta;
                if (grassTimer >= 5.0f) {
                    grassTimer = 0;
                    Pokemon rival = spawnPokemon.verificarEncuentro();
                    if (rival != null) {
                        pokemonSalvaje = rival;
                        enEncuentro = true;
                    }
                }
            }

            // Iniciar Batalla si hubo encuentro
            if (enEncuentro && !saliendoAlMenu) {
                Pokemon rival = pokemonSalvaje;
                if (rival == null)
                    rival = new com.Proyecto.Pokemon.pokemon.PokeFuego.Ignirrojo("Macho");
                Pokemon miPokemon = null;
                // Buscar el primer pokemon vivo del equipo
                for (int i = 0; i < jugador.getEquipo().getCantidad(); i++) {
                    Pokemon p = jugador.getEquipo().getPokemon(i);
                    if (p.estaVivo()) {
                        miPokemon = p;
                        break;
                    }
                }

                // Fallback si todos debilitados
                if (miPokemon == null) {
                    if (jugador.getEquipo().getCantidad() > 0) {
                        miPokemon = jugador.getEquipo().getPokemon(0);
                    } else {
                        miPokemon = game.getPokemonInicial();
                        if (miPokemon == null)
                            miPokemon = new com.Proyecto.Pokemon.pokemon.PokeFuego.Ignirrojo("Macho");
                    }
                }

                game.setScreen(new PantallaBatalla(game, this, miPokemon, rival));
                enEncuentro = false;
                return;
            }
        }

        // Actualización de la cámara
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;
        if (this.nombreMapa != null && this.nombreMapa.contains("MapaCentro")) {
            camera.position.set(anchoMapa / 2f, altoMapa / 2f, 0);
            camera.zoom = 1.0f;
        } else {
            float camX = MathUtils.clamp(jugador.getX() + 0.5f, halfWidth, anchoMapa - halfWidth);
            float camY = MathUtils.clamp(jugador.getY() + 0.5f, halfHeight, altoMapa - halfHeight);
            camera.position.set(camX, camY, 0);
            camera.zoom = 1.0f;
        }
        camera.update();

        // Limpiar la pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Renderizar mapa
        renderer.setView(camera);
        renderer.render();

        // Renderizar jugador y NPCs
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        jugador.draw(game.batch);
        if (npcs != null) {
            for (NPC npc : npcs) {
                npc.render(game.batch);
            }
        }
        game.batch.end();

        // DIBUJAR OVERLAYS
        if (pausado) {
            dibujarMenuPausa();
        } else if (mostrandoDialogo) {
            dibujarDialogo();
        } else if (mostrandoPokedex) {
            dibujarPokedex();
        } else if (enEncuentro) {
            dibujarEncuentroPokemon();
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
        } else if (menuEquipoAbierto) {
            dibujarMenuEquipoLateral();
            if (mostrandoError) {
                dibujarCuadroError(delta);
            }
        } else if (menuCuracionAbierto) {
            dibujarMenuCuracion();
        } else {
            if (mostrandoError)
                dibujarCuadroError(delta);
        }
    }

    /**
     * Constructor del Mapa.
     *
     * @param game          Instancia principal del juego.
     * @param nombreArchivo El nombre del archivo .tmx que se cargara.
     */
    public Mapa(Main game, String nombreArchivo) {
        this(game, nombreArchivo, null);
    }

    /**
     * Constructor que intenta spawnear al jugador en el portal que conecta con el
     * mapa anterior.
     */
    public Mapa(Main game, String nombreArchivo, String nombreMapaAnterior) {
        this(game, nombreArchivo, nombreMapaAnterior, null);
    }

    /**
     * Constructor extendido con mensaje inicial.
     */
    public Mapa(Main game, String nombreArchivo, String nombreMapaAnterior, String mensajeInicial) {
        this.game = game;
        this.nombreMapa = nombreArchivo;

        // 1. CARGAMOS EL MAPA
        try {
            // Crear loader con resolver explícito para rutas internas
            TmxMapLoader loader = new TmxMapLoader();
            mapaTiled = loader.load(nombreArchivo);
        } catch (Exception e) {
            System.err.println("Error cargando mapa: " + nombreArchivo);
            System.err.println("Excepción: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            // En lugar de salir inmediatamente, lanzar la excepción para que se maneje
            // correctamente
            throw new RuntimeException("No se pudo cargar el mapa: " + nombreArchivo, e);
        }

        renderer = new OrthogonalTiledMapRenderer(mapaTiled, UNIT_SCALE, game.batch);

        // 2. BUSCAMOS LA HIERBA (Mantener igual)
        MapLayer capaLogica = mapaTiled.getLayers().get("LogicaHierba");
        if (capaLogica != null) {
            for (MapObject objeto : capaLogica.getObjects()) {
                if (objeto instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) objeto).getRectangle();
                    zonasHierba.add(new Rectangle(rect.x * UNIT_SCALE, rect.y * UNIT_SCALE, rect.width * UNIT_SCALE,
                            rect.height * UNIT_SCALE));
                }
            }
        }

        // 3. INICIALIZACION
        if (mapaTiled.getProperties().containsKey("width")) {
            anchoMapa = mapaTiled.getProperties().get("width", Integer.class);
            altoMapa = mapaTiled.getProperties().get("height", Integer.class);
        } else {
            // Valores por defecto si el mapa no tiene propiedades
            anchoMapa = 50;
            altoMapa = 40;
        }

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 30, 20);

        // --- LÓGICA DE SPAWN BASADA EN PUNTOS NOMBRADOS EN TILED ---
        float spawnX = 10; // Posición por defecto
        float spawnY = 10;

        if (nombreMapaAnterior != null) {
            System.out.println("Mapa anterior: " + nombreMapaAnterior);
            System.out.println("Mapa actual: " + nombreMapa);

            // Normalizar nombres para comparación (quitar "Tiled/" y ".tmx")
            String anteriorLimpio = nombreMapaAnterior.replace("Tiled/", "").replace(".tmx", "");
            String actualLimpio = nombreMapa.replace("Tiled/", "").replace(".tmx", "");

            // Determinar qué punto de spawn buscar según el mapa de origen
            String nombreSpawn = null;

            // Caso especial: Respawn tras derrota
            if (nombreMapaAnterior.equals("RespawnCentro")) {
                nombreSpawn = "arriba"; // Punto de spawn correcto dentro del Centro Pokemon
            }
            // Caso especial: Bosses (spawn directo)
            else if (nombreMapaAnterior.equals("abajo3") || nombreMapaAnterior.equals("abajo")) {
                nombreSpawn = nombreMapaAnterior;
            }
            // MapaVerde tiene múltiples puntos de spawn
            else if (actualLimpio.contains("MapaVerdePokemon")) {
                if (anteriorLimpio.contains("MapaAzul")) {
                    nombreSpawn = "abajo1";
                } else if (anteriorLimpio.contains("MapaCentro")) {
                    nombreSpawn = "abajo2";
                }
            }
            // MapaAzul
            else if (actualLimpio.contains("MapaAzul")) {
                if (anteriorLimpio.contains("MapaCentro")) {
                    nombreSpawn = "abajo2";
                } else {
                    nombreSpawn = "arriba"; // Default for other transitions (e.g. from Verde)
                }
            }
            // MapaCentro
            else if (actualLimpio.contains("MapaCentro")) {
                nombreSpawn = "arriba"; // Default entry point
            }

            // Buscar el punto de spawn en la capa "Sentido"
            if (nombreSpawn != null) {
                MapLayer capaSentido = mapaTiled.getLayers().get("Sentido");
                if (capaSentido != null) {
                    boolean encontrado = false;
                    // Recorrer todos los objetos de la capa buscando el que tenga el atributo
                    // correcto
                    for (MapObject objeto : capaSentido.getObjects()) {
                        // Intentamos obtener la propiedad "Sentido" (con mayúscula, como en el
                        // screenshot del usuario)
                        String sentidoObjeto = objeto.getProperties().get("Sentido", String.class);
                        // Si no existe, intentamos "sentido" (minúscula)
                        if (sentidoObjeto == null) {
                            sentidoObjeto = objeto.getProperties().get("sentido", String.class);
                        }

                        // También verificamos si el NOMBRE del objeto coincide, por si acaso
                        if (sentidoObjeto == null && objeto.getName() != null) {
                            // Esto es opcional, pero robusto si el usuario olvidó la propiedad pero puso el
                            // nombre
                            // sentidoObjeto = objeto.getName();
                            // Dejémoslo solo por propiedades por ahora para ser estrictos con lo que pidió
                            // el usuario ("atributos")
                        }

                        if (sentidoObjeto != null && sentidoObjeto.equalsIgnoreCase(nombreSpawn)) {
                            spawnX = (Float) objeto.getProperties().get("x") * UNIT_SCALE;
                            spawnY = (Float) objeto.getProperties().get("y") * UNIT_SCALE;
                            System.out.println(
                                    "Spawn encontrado con sentido '" + nombreSpawn + "': " + spawnX + ", " + spawnY);
                            encontrado = true;
                            break;
                        }
                    }
                    if (!encontrado) {
                        System.out.println("ADVERTENCIA: No se encontró objeto con sentido '" + nombreSpawn + "'");
                    }
                } else {
                    System.out.println("ADVERTENCIA: No se encontró la capa 'Sentido'");
                }
            }
        }

        // Fallback: Si no hay mapa anterior, buscar objeto con sentido "Entrada"
        if (nombreMapaAnterior == null) {
            MapLayer capaSentido = mapaTiled.getLayers().get("Sentido");
            if (capaSentido != null) {
                boolean encontradoInput = false;
                // Primero buscamos explícitamente "Entrada"
                for (MapObject objeto : capaSentido.getObjects()) {
                    String sentidoObjeto = objeto.getProperties().get("Sentido", String.class);
                    if (sentidoObjeto == null) {
                        sentidoObjeto = objeto.getProperties().get("sentido", String.class);
                    }

                    if (sentidoObjeto != null && sentidoObjeto.equalsIgnoreCase("Entrada")) {
                        spawnX = (Float) objeto.getProperties().get("x") * UNIT_SCALE;
                        spawnY = (Float) objeto.getProperties().get("y") * UNIT_SCALE;
                        System.out.println("Spawn encontrado en 'Entrada': " + spawnX + ", " + spawnY);
                        encontradoInput = true;
                        break;
                    }
                }

                // Si no encontramos "Entrada", usamos el primer punto de spawn disponible
                if (!encontradoInput && capaSentido.getObjects().getCount() > 0) {
                    MapObject primerObjeto = capaSentido.getObjects().get(0);
                    spawnX = (Float) primerObjeto.getProperties().get("x") * UNIT_SCALE;
                    spawnY = (Float) primerObjeto.getProperties().get("y") * UNIT_SCALE;
                    System.out.println("Spawn default encontrado (fallback): " + spawnX + ", " + spawnY);
                }
            }
        }

        // 4. JUGADOR (Actualizar posición)
        this.jugador = game.getJugador();
        if (this.jugador != null) {
            // Si hay un mapa anterior, estamos cambiando de mapa (usar posición del portal)
            // Si no hay mapa anterior, estamos iniciando/cargando en este mapa
            // En ambos casos, actualizamos la posición del jugador
            // La única excepción es si el jugador fue cargado desde guardado y está en una
            // posición válida
            // (pero esto se maneja en PantallaSeleccionPartida que establece la posición
            // antes de crear el mapa)

            // Si hay mapa anterior, es un cambio de mapa (usar portal)
            // Si no hay mapa anterior pero la posición es diferente de (10,10), mantenerla
            // (cargado)
            // Si no hay mapa anterior y está en (10,10), usar spawn calculado
            float jugadorX = this.jugador.getX();
            float jugadorY = this.jugador.getY();
            boolean esPosicionPorDefecto = Math.abs(jugadorX - 10f) < 0.1f && Math.abs(jugadorY - 10f) < 0.1f;

            if (nombreMapaAnterior != null || (nombreMapaAnterior == null && esPosicionPorDefecto)) {
                // Cambio de mapa O nueva partida: usar spawn calculado
                this.jugador.getPosicion().set(spawnX, spawnY);
                this.jugador.getDestino().set(spawnX, spawnY);
            }
            // Si no hay mapa anterior y no es posición por defecto, mantener la posición
            // (partida cargada)
        } else {
            this.jugador = new Player(spawnX, spawnY);
            game.setJugador(this.jugador);
        }

        this.spawnPokemon = new SpawnPokemon();
        // Usar el sistema de captura del jugador (persistente)
        this.sistemaCaptura = this.jugador.getSistemaCaptura();

        // Reproducir música según el mapa actual
        reproducirMusicaMapa();

        // Carga de texturas de UI
        pausaSalir = new Texture(Gdx.files.internal("Salir.png"));
        pausaSalirC = new Texture(Gdx.files.internal("SalirC.png"));
        pausaVolver = new Texture(Gdx.files.internal("Boton de Continuar base.png"));
        pausaVolverC = new Texture(Gdx.files.internal("Boton de Continuar.png"));
        pausaOpciones = new Texture(Gdx.files.internal("Opciones.png"));
        pausaOpcionesC = new Texture(Gdx.files.internal("OpcionesC.png"));
        pausaPokepausa = new Texture(Gdx.files.internal("Pokepausa.png"));
        marcoInventario = new Texture(Gdx.files.internal("MarcoInventario.png"));

        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        Pixmap pixmap = new Pixmap(1, 1,
                Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();

        texCraftear = new Texture(Gdx.files.internal("Boton de Craftear.png")); // Reemplazado por el C
        texCraftearC = texCraftear; // Mantenemos referencia por compatibilidad
        marcoPlastico = new Texture(Gdx.files.internal("Marco 8bit Plastico.png"));
        marcoGoma = new Texture(Gdx.files.internal("Marco 8bit Goma.png"));
        marcoMadera = new Texture(Gdx.files.internal("Marco 8bit Madera.png"));
        marcoSlot = new Texture(Gdx.files.internal("Marco 8bit.png"));
        marcoSlotC = new Texture(Gdx.files.internal("Marco 8bit a color.png"));
        texPokeCura = new Texture(Gdx.files.internal("PokeCura.png"));
        texPokeExp = new Texture(Gdx.files.internal("PokeExp.png"));
        texPokeball = new Texture(Gdx.files.internal("Pokeball.png"));
        marcoCrafteoSeleccionado = new Texture(Gdx.files.internal("MarcoInventariobase.png"));
        marcoCrafteoNoSeleccionado = new Texture(Gdx.files.internal("MarcoInventario2.png"));

        // Texturas Curación
        texCurarEquipo = new Texture(Gdx.files.internal("CurarEquipo.png"));
        texBotonAceptar = new Texture(Gdx.files.internal("Boton de Aceptar.png"));
        texBotonAceptarBase = new Texture(Gdx.files.internal("Boton de Aceptar base.png"));
        texBotonCancelar = new Texture(Gdx.files.internal("Boton de Cancelar.png"));
        texBotonCancelarBase = new Texture(Gdx.files.internal("Boton de Cancelar base.png"));

        fontPequeña = new BitmapFont();
        fontPequeña.getData().setScale(0.8f);
        marcoInfo = new Texture(Gdx.files.internal("Marco Info1.png"));

        // NPC Init
        npcs = new java.util.ArrayList<>();

        // Texture region default for NPC
        Texture playerTex = new Texture(Gdx.files.internal("player_sprite.png"));
        TextureRegion npcRegion = new TextureRegion(playerTex, 0, 0, playerTex.getWidth() / 4,
                playerTex.getHeight() / 4);

        for (MapLayer layer : mapaTiled.getLayers()) {
            // Carga de NPCs desde capa de Objetos
            if (layer.getObjects().getCount() > 0) {
                Iterator<MapObject> iter = layer.getObjects().iterator();
                while (iter.hasNext()) {
                    MapObject obj = iter.next();

                    boolean esCapaNPC = layer.getName().toUpperCase().contains("NPC");
                    boolean tienePropiedadNPC = obj.getProperties().containsKey("NPC");

                    if (!esCapaNPC && !tienePropiedadNPC) {
                        continue;
                    }

                    float x = 0;
                    float y = 0;
                    TextureRegion regionToUse = npcRegion;
                    boolean esValido = false;

                    if (obj instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                        x = rect.x * UNIT_SCALE;
                        y = rect.y * UNIT_SCALE;
                        esValido = true;
                    } else if (obj instanceof TiledMapTileMapObject) {
                        TiledMapTileMapObject tileObj = (TiledMapTileMapObject) obj;
                        x = tileObj.getX() * UNIT_SCALE;
                        y = tileObj.getY() * UNIT_SCALE;

                        if (tileObj.getTile() != null) {
                            regionToUse = tileObj.getTile().getTextureRegion();
                        }
                        esValido = true;
                    }

                    if (esValido) {
                        String msg = "Hola viajero!";
                        if (obj.getProperties().containsKey("mensaje")) {
                            msg = obj.getProperties().get("mensaje", String.class);
                        }

                        String tipoNPC = "";
                        if (obj.getProperties().containsKey("NPC")) {
                            tipoNPC = obj.getProperties().get("NPC", String.class);
                        }
                        if (tipoNPC.isEmpty() && esCapaNPC)
                            tipoNPC = "Civil";

                        Color objColor = null;
                        if (obj.getColor() != null) {
                            objColor = obj.getColor();
                        }

                        float snapX = Math.round(x);
                        float snapY = Math.round(y);

                        NPC nuevoNpc = new NPC(snapX, snapY, regionToUse, msg, objColor, tipoNPC);
                        npcs.add(nuevoNpc);
                        System.out.println("NPC Cargado: " + tipoNPC + " en " + snapX + "," + snapY);
                    }
                }
            }

            // Carga de NPCs desde capa de Tiles
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                for (int x = 0; x < tileLayer.getWidth(); x++) {
                    for (int y = 0; y < tileLayer.getHeight(); y++) {
                        TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                        if (cell != null && cell.getTile() != null) {
                            String propNPC = getPropiedad(cell.getTile(), "NPC");
                            boolean isNPC = propNPC != null;

                            if (isNPC) {
                                TextureRegion reg = cell.getTile().getTextureRegion();
                                String msg = "Hola!";
                                if (getPropiedad(cell.getTile(), "mensaje") != null)
                                    msg = getPropiedad(cell.getTile(), "mensaje");

                                String tipo = propNPC;

                                NPC n = new NPC(x, y, reg, msg, null, tipo);
                                npcs.add(n);
                                System.out.println("NPC (Tile) Cargado: " + tipo + " en " + x + "," + y);
                                tileLayer.setCell(x, y, null);
                            }
                        }
                    }
                }
            }
        }

        // Mostrar mensaje inicial si existe (AL FINAL DEL CONSTRUCTOR, fuera del bucle
        // de capas)
        if (mensajeInicial != null && !mensajeInicial.isEmpty()) {
            mostrarDialogo(mensajeInicial);
        }
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
        System.out.println(
                "DEBUG: Iniciando interaccion en (" + x + "," + y + ") -> celda (" + cellX + "," + cellY + ")");

        // --- CASO 0: INTERACCION CON NPC ---
        if (npcs != null) {
            for (NPC npc : npcs) {
                // Chequear si la posicion de interaccion (x, y) esta cerca del NPC
                // Usamos una tolerancia pequeña (0.5f) ya que x,y son enteras a veces
                if (npc.getPosicion().dst(x, y) < 1.0f) {
                    this.npcActual = npc; // Guardamos el NPC con el que estamos hablando
                    String tipo = npc.getTipo();
                    String tipoNormalizado = (tipo != null) ? tipo.toLowerCase().replace(" ", "") : "";

                    if (tipoNormalizado.contains("enemigo1")) {
                        mostrarDialogo("Deja de mirarme!, Quieres morir?, A PELEAR");
                    } else if (tipoNormalizado.contains("enemigo2")) {
                        mostrarDialogo(
                                "En estos tiemos tienes que aceptar la oscuridad para sobrevivir.... dejame llevarte hacia ella.");
                    } else if (tipoNormalizado.contains("enemigo3")) {
                        mostrarDialogo("Mis padres me regalaron un pokemon, veamos como se luce en batalla!");
                    } else if (tipoNormalizado.contains("enemigo4")) {
                        mostrarDialogo("1 2 3 4, 1 2 3 4 6, Aghh me hiciste perder la cuenta, preparate para sufrir!");
                    } else if ("Enemigo".equalsIgnoreCase(tipo)) {
                        mostrarDialogo("Hola como estas");
                    } else {
                        mostrarDialogo(npc.getMensaje());
                    }
                    return true;
                }
            }
        }

        // Recorremos todas las capas del mapa.
        for (MapLayer layer : mapaTiled.getLayers()) {

            // --- CASO 1: Capas de Baldosas (Tile Layers) ---
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                TiledMapTileLayer.Cell cell = tileLayer.getCell(cellX, cellY);

                if (cell != null && cell.getTile() != null) {
                    String tipo = getPropiedad(cell.getTile(), "tipo");
                    String item = getPropiedad(cell.getTile(), "item");
                    System.out.println("DEBUG: Capa Tile: " + layer.getName() + " Celda: " + cellX + "," + cellY
                            + " Tipo: " + tipo + " Item: " + item);

                    // PRIORIDAD 0: Letreros / Carteles (En Baldosas)
                    boolean esLetrero = "Cartel".equalsIgnoreCase(tipo) || "Letrero".equalsIgnoreCase(tipo) ||
                            "Cartel".equalsIgnoreCase(item) || "Letrero".equalsIgnoreCase(item) ||
                            getPropiedad(cell.getTile(), "Letrero") != null;

                    if (esLetrero) {
                        System.out.println("DEBUG: Interactuando con Letrero/Cartel (Baldosa)");
                        mostrarDialogo("Ten cuidado con la hierba, nunca se sabe lo que puede aparecer");
                        return true;
                    }

                    // PRIORIDAD 1: Eventos especiales (ej: ir al inicio).
                    if ("inicio".equalsIgnoreCase(tipo)) {
                        game.setScreen(new PantallaDeInicio(game));
                        dispose();
                        return true;
                    }

                    // PRIORIDAD 1.5: Emergencias (Curación Centro Pokémon)
                    String emergencias = getPropiedad(cell.getTile(), "Emergencias");
                    if ("Curar".equalsIgnoreCase(emergencias)) {
                        menuCuracionAbierto = true;
                        opcionCuracion = 0; // Por defecto en Aceptar
                        return true;
                    }

                    // PRIORIDAD 1.6: Pokedex
                    String pokedexProp = getPropiedad(cell.getTile(), "Pokedex");
                    if ("True".equalsIgnoreCase(pokedexProp)) {
                        mostrandoPokedex = true;
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
                    if (tileObj.getTile() == null) {
                        System.out.println("DEBUG: Objeto " + obj.getName() + " sin tile!");
                        continue;
                    }

                    // Calculamos el área que ocupa el objeto en el mundo.
                    float objX = tileObj.getX() * UNIT_SCALE;
                    float objY = tileObj.getY() * UNIT_SCALE;
                    float objW = tileObj.getTile().getTextureRegion().getRegionWidth() * UNIT_SCALE;
                    float objH = tileObj.getTile().getTextureRegion().getRegionHeight() * UNIT_SCALE;

                    // Si el clic del jugador está dentro del objeto...
                    if (x >= objX && x < objX + objW && y >= objY && y < objY + objH) {
                        // Buscamos propiedad en el OBJETO primero, luego en el TILE
                        String tipo = getPropiedad(tileObj, "tipo");
                        if (tipo == null)
                            tipo = getPropiedad(tileObj.getTile(), "tipo");

                        String item = getPropiedad(tileObj, "item");
                        if (item == null)
                            item = getPropiedad(tileObj.getTile(), "item");

                        // DEBUG para ver que detecta
                        System.out.println(
                                "DEBUG: Objeto detectado: " + tileObj.getName() + " Tipo: " + tipo + " Item: " + item);

                        boolean esLetreroObj = "Cartel".equalsIgnoreCase(tipo) || "Letrero".equalsIgnoreCase(tipo) ||
                                "Cartel".equalsIgnoreCase(item) || "Letrero".equalsIgnoreCase(item) ||
                                getPropiedad(tileObj, "Letrero") != null ||
                                getPropiedad(tileObj.getTile(), "Letrero") != null;

                        if (esLetreroObj) {
                            System.out.println("DEBUG: Interactuando con Letrero/Cartel (Objeto)");
                            mostrarDialogo("Ten cuidado con la hierba, nunca se sabe lo que puede aparecer");
                            return true;
                        }

                        if ("inicio".equalsIgnoreCase(tipo)) {
                            game.setScreen(new PantallaDeInicio(game));
                            dispose();
                            return true;
                        }

                        // CRITICO: Si es un NPC (tiene propiedad NPC o tipo NPC), NO LO RECOJAS.
                        if (tileObj.getProperties().containsKey("NPC") || "NPC".equalsIgnoreCase(tipo)) {
                            // Es un NPC, lo ignoramos aqui porque ya se debio manejar en el bucle de "CASO
                            // 0" al inicio del metodo.
                            // Retornamos true para decir "aqui hay algo", pero no lo recogemos.
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

        // SEGURIDAD: SI ES NPC, JAMAS ES RECOGIBLE
        if (getPropiedad(tile, "NPC") != null || "NPC".equalsIgnoreCase(tipo))
            return false;

        // Un objeto de "inicio" no es recogible (es un evento).
        if ("inicio".equalsIgnoreCase(tipo))
            return false;

        // Comprobación por nombre del conjunto de patrones (tileset).
        // Si el tileset tiene "pokebola" o "pokeball" en el nombre, lo tratamos como
        // recogible.
        for (com.badlogic.gdx.maps.tiled.TiledMapTileSet tileset : mapaTiled.getTileSets()) {
            String name = tileset.getName();
            if (name != null && (name.toLowerCase().contains("pokebola") || name.toLowerCase().contains("pokeball"))) {
                for (com.badlogic.gdx.maps.tiled.TiledMapTile t : tileset) {
                    // EXCEPCION: SI TIENE PROPIEDAD NPC, NO ES RECOGIBLE AUNQUE ESTE EN TILESET
                    // POKEBOLA
                    if (t == tile && getPropiedad(t, "NPC") == null)
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
        if (tile == null)
            return null;
        String val = getPropiedad(tile.getProperties(), key);
        if (val != null)
            return val;

        // Si no se encuentra en las propiedades directas del tile,
        // buscamos en los objetos contenidos en el tile (útil para "Letrero").
        if (tile.getObjects() != null) {
            for (com.badlogic.gdx.maps.MapObject obj : tile.getObjects()) {
                val = getPropiedad(obj, key);
                if (val != null)
                    return val;
            }
        }
        return null;
    }

    private String getPropiedad(com.badlogic.gdx.maps.MapObject obj, String key) {
        if (obj == null)
            return null;

        // Comprobamos si el nombre del objeto o su tipo coincide con el valor buscado
        // cuando buscamos "tipo" o "item".
        if ("tipo".equalsIgnoreCase(key) || "item".equalsIgnoreCase(key)) {
            if (obj.getName() != null)
                return obj.getName();

            // En LibGDX, el campo "Type" de Tiled suele estar en las propiedades con la
            // clave "type"
            // pero algunas versiones lo exponen de otras formas. Intentamos accederlo.
            Object type = obj.getProperties().get("type");
            if (type != null)
                return type.toString();
        }

        return getPropiedad(obj.getProperties(), key);
    }

    private String getPropiedad(com.badlogic.gdx.maps.MapProperties props, String key) {
        if (props == null)
            return null;
        if (props.containsKey(key))
            return props.get(key).toString();

        for (Iterator<String> it = props.getKeys(); it.hasNext();) {
            String k = it.next();
            if (k.equalsIgnoreCase(key)) {
                return props.get(k).toString();
            }
        }
        return null;
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
                // Guardar partida antes de salir al menú principal
                System.out.println("Seleccionado: Salir al menú principal");
                guardarPartidaYSalir();
            }
        }
    }

    /**
     * Guarda la partida actual y vuelve al menú principal.
     */
    private void guardarPartidaYSalir() {
        try {
            // Obtener el Pokémon inicial del jugador
            Pokemon pokemonInicial = game.getPokemonInicial();
            if (pokemonInicial == null) {
                // Fallback si no hay Pokémon inicial
                pokemonInicial = new PokeFuego.Ignirrojo("Macho");
            }

            // Limpiar el nombre del mapa: quitar "Tiled/" y extensión ".tmx"
            String nombreMapaLimpio = nombreMapa;
            if (nombreMapaLimpio != null) {
                // Quitar prefijo "Tiled/" si existe
                if (nombreMapaLimpio.startsWith("Tiled/")) {
                    nombreMapaLimpio = nombreMapaLimpio.substring(6);
                }
                // Quitar extensión ".tmx" si existe
                if (nombreMapaLimpio.endsWith(".tmx")) {
                    nombreMapaLimpio = nombreMapaLimpio.substring(0, nombreMapaLimpio.length() - 4);
                }
            } else {
                nombreMapaLimpio = "MapaVerdePokemon"; // Fallback
            }

            // Guardar la partida
            boolean guardado = GestorGuardado.guardarPartida(jugador, nombreMapaLimpio);
            if (guardado) {
                System.out.println("Partida guardada correctamente. Volviendo al menú principal...");
            } else {
                System.out.println("Advertencia: No se pudo guardar la partida, pero se continúa con la salida.");
            }

            // Salir al menú principal
            System.out.println("Cambiando al menú principal...");
            // Marcar que estamos saliendo para evitar renderizado adicional
            saliendoAlMenu = true;
            // Cambiar la pantalla primero - LibGDX manejará el dispose() automáticamente
            MenuPrincipal menuPrincipal = new MenuPrincipal(game);
            game.setScreen(menuPrincipal);
            System.out.println("Pantalla cambiada al menú principal.");
            // Llamar hide() manualmente para asegurar que la pantalla actual se oculte
            hide();
            // No llamar dispose() aquí - LibGDX lo manejará automáticamente
        } catch (Exception e) {
            System.err.println("Error al guardar partida: " + e.getMessage());
            e.printStackTrace();
            // Aun así, salir al menú principal
            System.out.println("Cambiando al menú principal (después de error)...");
            saliendoAlMenu = true;
            game.setScreen(new MenuPrincipal(game));
            hide();
            // No llamar dispose() aquí - LibGDX lo manejará automáticamente
        }
    }

    /**
     * Gestiona la entrada del teclado cuando se muestra la Pokedex.
     * Permite salir de la Pokedex presionando ENTER.
     */
    private void actualizarEntradaPokedex() {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            mostrandoPokedex = false;
        }
    }

    /**
     * Dibuja la interfaz visual de la Pokedex.
     * Muestra una cuadrícula con los Pokemon descubiertos y sus puntos de
     * investigación.
     */
    private void dibujarPokedex() {
        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.begin();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float anchoMarco = w * 0.8f;
        float altoMarco = h * 0.8f;
        float x = (w - anchoMarco) / 2;
        float y = (h - altoMarco) / 2;

        game.batch.setColor(1, 1, 1, 1);
        if (marcoInventario != null)
            game.batch.draw(marcoInventario, x, y, anchoMarco, altoMarco);
        else
            game.batch.draw(pixel, x, y, anchoMarco, altoMarco);

        String[] nombresPokedex = { "Ignirrojo", "Brotálamo", "Aqualisca", "Volcárex", "Floravelo", "Mareónix" };

        // Ajustamos el área de contenido para "unir más las imágenes" (reducir
        // dispersión)
        float contentW = anchoMarco * 0.85f;
        float contentH = altoMarco * 0.85f;
        float contentX = x + (anchoMarco - contentW) / 2;
        float contentY = y + (altoMarco - contentH) / 2;

        float slotW = contentW / 3;
        float slotH = contentH / 2;

        for (int i = 0; i < 6; i++) {
            int row = (i < 3) ? 0 : 1;
            int col = i % 3;

            // Ajustar Y segun la fila: la superior (0) usa el centro, la inferior (1) usa
            // la base.
            float drawY = (row == 0) ? (contentY + slotH) : (contentY + 50);
            float drawX = contentX + col * slotW;

            String nombreDisplay = nombresPokedex[i];

            // Usamos el nombre tal cual para buscar el sprite, ya que los archivos tienen
            // acentos
            int puntos = jugador.getPuntosInvestigacion(nombreDisplay);

            Texture sprite = gestorSprites.obtenerSpriteFrente(nombreDisplay, 0);

            float spriteSize = 90; // Más pequeña
            // Centrar sprite en el slot y bajarlo un poco (ajuste relativo al slot)
            float spriteX = drawX + (slotW - spriteSize) / 2;
            float spriteY = drawY + (slotH - spriteSize) / 2 + 10;

            if (sprite != null) {
                if (puntos < 10) {
                    game.batch.setColor(0, 0, 0, 1);
                } else {
                    game.batch.setColor(1, 1, 1, 1);
                }
                game.batch.draw(sprite, spriteX, spriteY, spriteSize, spriteSize);
                game.batch.setColor(1, 1, 1, 1);
            }

            fontPequeña.setColor(Color.WHITE);
            // Centrar nombre (SIN NUMERO) abajo del centro del pokemon
            float textY = spriteY - 15;

            com.badlogic.gdx.graphics.g2d.GlyphLayout layoutName = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                    fontPequeña, nombreDisplay);
            fontPequeña.draw(game.batch, nombreDisplay, spriteX + (spriteSize - layoutName.width) / 2, textY);

            String puntosInfo = puntos + "/10";
            com.badlogic.gdx.graphics.g2d.GlyphLayout layoutPoints = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                    fontPequeña, puntosInfo);
            fontPequeña.draw(game.batch, puntosInfo, spriteX + (spriteSize - layoutPoints.width) / 2, textY - 25);
        }

        game.batch.end();
    }

    /**
     * Dibuja los elementos visuales del menu de pausa.
     */
    private void dibujarMenuPausa() {
        // Sin embargo, para mantenerlo simple y centrado, usaremos una proporcion
        // local.
        float pantallaAncho = Gdx.graphics.getWidth();
        float pantallaAlto = Gdx.graphics.getHeight();

        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, pantallaAncho, pantallaAlto);
        game.batch.begin();

        // Filtro negro semi-transparente.
        game.batch.setColor(0, 0, 0, 0.5f);
        // Dibujamos un fondo oscuro (opcional, si no hay textura de fondo)
        // Pero el usuario pidi├│ Pokepausa grande en el medio.
        game.batch.setColor(1, 1, 1, 1); // Reset de color.

        // Imagen Pokepausa grande en el medio
        float pokeW = pantallaAncho * 0.4f; // Reducido de 0.6f a 0.4f
        float pokeH = (pokeW / pausaPokepausa.getWidth()) * pausaPokepausa.getHeight();
        game.batch.draw(pausaPokepausa, pantallaAncho * 0.55f, (pantallaAlto - pokeH) / 2f, pokeW, pokeH);

        float btnW = pantallaAncho * 0.3f;
        float btnH = pantallaAlto * 0.1f;
        float x = pantallaAncho * 0.05f; // Margen del 5% desde la izquierda
        float centroY = pantallaAlto / 2f;

        float separacion = 30f;
        // Boton Reanudar (arriba).
        Texture texReanudar = (opcionPausa == OPCION_REANUDAR) ? pausaVolverC : pausaVolver;
        game.batch.draw(texReanudar, x, centroY + separacion, btnW, btnH);

        // Boton Salir al Menú Principal (abajo).
        Texture texSalir = (opcionPausa == OPCION_SALIR_MENU) ? pausaSalirC : pausaSalir;
        game.batch.draw(texSalir, x, centroY - btnH - separacion, btnW, btnH);

        game.batch.end();

        // Restauramos la proyeccion de la camara para el siguiente frame.
        game.batch.setProjectionMatrix(camera.combined);
    }

    /**
     * Gestiona la entrada del teclado cuando se abre el menú de curación.
     */
    private void actualizarEntradaCuracion() {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            opcionCuracion = 0; // Aceptar
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            opcionCuracion = 1; // Cancelar
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.Z)) {
            if (opcionCuracion == 0) {
                // Aceptar curación
                curarEquipoCompleto();
                menuCuracionAbierto = false;
            } else {
                // Cancelar
                menuCuracionAbierto = false;
            }
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.X)) {
            menuCuracionAbierto = false;
        }
    }

    /**
     * Dibuja los elementos visuales del menú de curación.
     */
    private void dibujarMenuCuracion() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
        game.batch.begin();

        // 1. Fondo Oscuro semi-transparente para resaltar el menu
        game.batch.setColor(0, 0, 0, 0.4f);
        game.batch.draw(pixel, 0, 0, sw, sh);
        game.batch.setColor(Color.WHITE);

        // 2. Imagen de fondo CurarEquipo (centrada)
        float curarW = sw * 0.5f;
        float curarH = (curarW / texCurarEquipo.getWidth()) * texCurarEquipo.getHeight();
        float curarX = (sw - curarW) / 2f;
        float curarY = (sh - curarH) / 2f + 50; // Un poco arriba del centro

        game.batch.draw(texCurarEquipo, curarX, curarY, curarW, curarH);

        // 3. Botones uno al lado del otro
        float btnW = sw * 0.2f;
        float btnH = sh * 0.08f;
        float btnY = curarY - btnH - 30; // Un poco debajo de la imagen
        float spacing = 20;
        float startX = (sw - (btnW * 2 + spacing)) / 2f;

        // Botón Aceptar
        Texture texAceptar = (opcionCuracion == 0) ? texBotonAceptarBase : texBotonAceptar;
        game.batch.draw(texAceptar, startX, btnY, btnW, btnH);

        // Botón Cancelar
        Texture texCancelar = (opcionCuracion == 1) ? texBotonCancelarBase : texBotonCancelar;
        game.batch.draw(texCancelar, startX + btnW + spacing, btnY, btnW, btnH);

        game.batch.end();
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
                    float iconSize = slotW * 0.5f; // Reducido de 0.7f a 0.5f
                    game.batch.draw(pbTex, slotX + (slotW - iconSize) / 2f, currentY + (slotH - iconSize) / 2f,
                            iconSize,
                            iconSize);
                }
            } else {
                // Dibujar interrogacion si no se tiene
                // (Opcional)
            }

            // Dibujar Cantidad
            if (cantidadpb > 0) {
                font.setColor(Color.BLACK);
                font.getData().setScale(1.2f);
                font.draw(game.batch, "x" + cantidadpb, slotX + 10, currentY + 30);
            }
        }

        game.batch.setColor(1, 1, 1, 1);
        game.batch.end();
        game.batch.setProjectionMatrix(camera.combined);
    }

    /**
     * Gestiona la entrada del teclado en el menú de equipo lateral.
     * Permite navegar por la lista de Pokemon, soltarlos o cerrar el menú.
     */
    private void actualizarEntradaEquipo() {
        int numPokemons = jugador.getEquipo().getPokemons().size();

        // 1. MODO SOLTAR: El jugador selecciona a quien echar
        if (modoSoltar) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.W)
                    || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {
                equipoSeleccionado = (equipoSeleccionado - 1 + numPokemons) % numPokemons;
            }
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.S)
                    || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
                equipoSeleccionado = (equipoSeleccionado + 1) % numPokemons;
            }
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                // Eliminar Pokemon
                if (numPokemons <= 1) {
                    // No debería pasar si validamos al entrar, pero por seguridad
                    mostrarError("No puedes soltar pokemones teniendo uno solo");
                    modoSoltar = false;
                    return;
                }
                Pokemon eliminado = jugador.getEquipo().getPokemon(equipoSeleccionado);
                jugador.getEquipo().eliminarPokemon(equipoSeleccionado);
                System.out.println("Has soltado a " + (eliminado != null ? eliminado.getNombre() : "el pokémon") + ".");

                // Ajustar indice si es necesario
                if (equipoSeleccionado >= jugador.getEquipo().getCantidad()) {
                    equipoSeleccionado = Math.max(0, jugador.getEquipo().getCantidad() - 1);
                }

                // Verificar si debemos salir del modo (menos de 2 pokemons)
                if (jugador.getEquipo().getCantidad() < 2) {
                    modoSoltar = false;
                }
            }
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)
                    || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
                modoSoltar = false;
            }
            return;
        }

        // 2. MODO NORMAL SELECCIONANDO BOTON SOLTAR
        if (botonSoltarSeleccionado) {
            // Volver a la lista de pokemon
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.A)
                    || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
                botonSoltarSeleccionado = false;
                equipoSeleccionado = 0; // Volver al primero
            }

            // Entrar al modo soltar
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                if (numPokemons < 2) {
                    mostrarError("No puedes soltar pokemones teniendo uno solo");
                } else {
                    modoSoltar = true;
                    botonSoltarSeleccionado = false;
                    equipoSeleccionado = 0;
                }
            }
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
                menuEquipoAbierto = false;
            }
            return;
        }

        // 3. MODO NORMAL LISTA DE POKEMONS
        // Navegacion vertical en la lista
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.W)
                || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {
            equipoSeleccionado = (equipoSeleccionado - 1 + numPokemons) % numPokemons;
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.S)
                || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
            equipoSeleccionado = (equipoSeleccionado + 1) % numPokemons;
        }

        // Ir al boton soltar (derecha)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.D)
                || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            botonSoltarSeleccionado = true;
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
            menuEquipoAbierto = false;
        }
    }

    /**
     * Obtiene una lista de movimientos representativos para un Pokemon dado.
     * 
     * @param p El Pokemon del cual obtener los movimientos.
     * @return Una cadena de texto con los nombres de los movimientos.
     */
    private String getMovimientosString(com.Proyecto.Pokemon.pokemon.Pokemon p) {
        if (p instanceof com.Proyecto.Pokemon.pokemon.PokeFuego)
            return "Lanzallamas, Llamarada";
        if (p instanceof com.Proyecto.Pokemon.pokemon.PokeAgua)
            return "Hidrochorro, Burbuja";
        if (p instanceof com.Proyecto.Pokemon.pokemon.PokePlanta)
            return "Hoja Afilada, Absorber";
        if (p instanceof com.Proyecto.Pokemon.pokemon.PokeDragon)
            return "Rayo Draconico, Cola Dragon";
        return "Placaje";
    }

    /**
     * Carga en memoria las texturas de los marcos para el equipo Pokemon actual.
     * Evita cargar texturas repetidas.
     */
    private void preCargarMarcosEquipo() {
        java.util.List<Pokemon> team = jugador.getEquipo().getPokemons();
        for (Pokemon p : team) {
            String nombre = p.getNombre().toLowerCase()
                    .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                    .replace("ñ", "n");

            if (!marcosPokemon.containsKey(nombre)) {
                try {
                    // Marcos organizados por Pokémon:
                    // assets/pokemon/<NombrePokemon>/Marco 8bit <NombreCap>.png
                    String folderPokemon = "pokemon/" + p.getNombre() + "/";
                    String nombreCap = nombre.substring(0, 1).toUpperCase() + nombre.substring(1); // sin acentos

                    String marcoPath = folderPokemon + "Marco 8bit " + nombreCap + ".png";
                    String marcoVacioPath = folderPokemon + "Marco 8bit " + nombreCap + " vacio.png";

                    if (Gdx.files.internal(marcoPath).exists()) {
                        marcosPokemon.put(nombre, new Texture(Gdx.files.internal(marcoPath)));
                    }
                    if (Gdx.files.internal(marcoVacioPath).exists()) {
                        marcosPokemonVacio.put(nombre, new Texture(Gdx.files.internal(marcoVacioPath)));
                    }
                } catch (Exception e) {
                    System.err.println("No se pudo cargar marco para: " + p.getNombre());
                }
            }
        }
    }

    /**
     * Dibuja el equipo pokemon en el lado IZQUIERDO (Estilo Batalla).
     * Muestra la lista de Pokemon actuales, sus vidas y detalles del seleccionado.
     */
    private void dibujarMenuEquipoLateral() {
        if (texBotonSoltar == null) {
            try {
                texBotonSoltar = new Texture(Gdx.files.internal("Marco 8bit.png"));
                texBotonSoltarColor = new Texture(Gdx.files.internal("Marco 8bit a color.png"));
                texSoltarLetra = new Texture(Gdx.files.internal("SoltarPokemoLetra.png"));
            } catch (Exception e) {
                System.err.println("Error cargando texturas de soltar: " + e.getMessage());
            }
        }

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
        game.batch.begin();

        // Fondo oscuro (Estilo Batalla)
        game.batch.setColor(0, 0, 0, 0.8f);
        game.batch.draw(pixel, 0, 0, sw, sh);
        game.batch.setColor(Color.WHITE);

        java.util.List<com.Proyecto.Pokemon.pokemon.Pokemon> equipo = jugador.getEquipo().getPokemons();
        int numPokemons = equipo.size();

        // Parametros layout IZQUIERDA
        float slotW = 240, slotH = 85;
        float startX = 30; // A la izquierda
        float startY = sh - 110;
        float spacingY = 10;

        for (int i = 0; i < 6; i++) {
            float x = startX;
            float y = startY - i * (slotH + spacingY);

            if (i < numPokemons) {
                com.Proyecto.Pokemon.pokemon.Pokemon p = equipo.get(i);
                String nombreNorm = p.getNombre().toLowerCase()
                        .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                        .replace("ñ", "n");

                // Elección de marco segun seleccion
                Texture marco;
                if (i == equipoSeleccionado && !botonSoltarSeleccionado) {
                    // Seleccionado: Marco lleno (y no estamos en el boton soltar, o estamos en modo
                    // soltar seleccionando)
                    marco = marcosPokemon.get(nombreNorm);
                } else {
                    // No seleccionado: Marco vacío
                    marco = marcosPokemonVacio.get(nombreNorm);
                }
                game.batch.setColor(Color.WHITE);

                if (marco == null)
                    marco = marcoSlot; // Fallback

                game.batch.draw(marco, x, y, slotW, slotH);

                // Vida en texto
                fontPequeña.setColor(p.estaVivo() ? Color.WHITE : Color.RED);
                fontPequeña.draw(game.batch, "HP: " + p.getVida() + "/" + p.getVidaMaxima(), x + slotW * 0.45f,
                        y + slotH * 0.4f);
            } else {
                // Slot genérico vacío
                game.batch.setColor(Color.WHITE);
                game.batch.draw(marcoSlot, x, y, slotW, slotH);
            }
        }
        game.batch.setColor(Color.WHITE);

        // PANEL DE INFORMACION (A LA DERECHA) - USA MARCO INFO
        if (numPokemons > 0) {
            float infoW = sw * 0.55f;
            float infoH = sh * 0.75f;
            float infoX = sw - infoW - 30;
            float infoY = (sh - infoH) / 2f - 40;

            if (modoSoltar) {
                // --- MODO SOLTAR ---
                // NO DIBUJAR: Button, MarcoInfo, Stats
                // SOLO DIBUJAR: Imagen warning y Texto

                if (texSoltarLetra != null) {
                    float imgW = infoW * 0.7f;
                    float imgH = (imgW / texSoltarLetra.getWidth()) * texSoltarLetra.getHeight();
                    game.batch.draw(texSoltarLetra, infoX + (infoW - imgW) / 2, infoY + (infoH - imgH) / 2, imgW, imgH);
                }

            } else {
                // --- MODO NORMAL ---
                // 1. Boton Soltar
                float btnW = 160;
                float btnH = 50;
                float btnX = infoX + infoW - btnW;
                float btnY = infoY + infoH;

                Texture btnTex = (botonSoltarSeleccionado) ? texBotonSoltarColor : texBotonSoltar;
                if (btnTex != null) {
                    game.batch.draw(btnTex, btnX, btnY, btnW, btnH);
                }

                // 2. Marco Info
                game.batch.draw(marcoInfo, infoX, infoY, infoW, infoH);

                // 3. Stats
                // Validar seleccion
                if (equipoSeleccionado < numPokemons) {
                    com.Proyecto.Pokemon.pokemon.Pokemon sel = equipo.get(equipoSeleccionado);

                    // Sprite del Pokemon seleccionado
                    Texture pTex = gestorSprites.obtenerSprite(sel.getNombre());
                    if (pTex != null) {
                        float spriteSize = infoH * 0.4f;
                        game.batch.draw(pTex, infoX + (infoW - spriteSize) / 2f, infoY + infoH - spriteSize - 50,
                                spriteSize,
                                spriteSize);
                    }

                    // Datos del Pokemon
                    font.getData().setScale(1.1f);
                    font.setColor(Color.WHITE);
                    float textX = infoX + 60;
                    float textY = infoY + infoH * 0.58f;
                    float spacingV = 35;

                    font.draw(game.batch, "Nombre: " + sel.getNombre(), textX, textY);
                    font.draw(game.batch, "Nivel: " + sel.getNivel(), textX, textY - spacingV);
                    font.draw(game.batch, "PS: " + sel.getVida() + "/" + sel.getVidaMaxima(), textX,
                            textY - spacingV * 2);
                    font.draw(game.batch, "Género: " + sel.getSexo(), textX, textY - spacingV * 3);
                    font.draw(game.batch, "Tipo: " + sel.getTipoString(), textX, textY - spacingV * 4);
                    font.draw(game.batch, "Peso: " + sel.getPeso() + " kg", textX, textY - spacingV * 5);
                    font.draw(game.batch, "Movimientos: " + getMovimientosString(sel), textX, textY - spacingV * 6);
                }
            }
        }

        game.batch.end();
        game.batch.setProjectionMatrix(camera.combined);
    }

    /**
     * Helper simplificado para dibujar botones superiores del inventario.
     */
    private void dibujarBotonTop(int id, float x, float y, float w, float h, boolean activo) {
        // Siempre usar la versión 'C' (color) ya que el usuario pidió eliminar la otra
        Texture tex = texCraftearC;
        game.batch.draw(tex, x, y, w, h);
    }

    /**
     * Dibuja un cuadro de item en el inventario.
     * 
     * @param nombre   Nombre del item.
     * @param cantidad Cantidad actual en el inventario.
     * @param x        Posición X.
     * @param y        Posición Y.
     * @param w        Ancho del cuadro.
     * @param h        Alto del cuadro.
     */
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
            // Vac├¡o
            game.batch.setColor(0, 0, 0, 0.2f);
            game.batch.draw(pixel, x, y, w, h);
            game.batch.setColor(1, 1, 1, 1);
        }

        // Cantidad (si es > 0)
        if (cantidad > 0) {
            font.setColor(Color.BLACK); // Usamos negro para que destaque sobre el verde/amarillo
            if (tex != null) {
                // Ajustamos posici├│n del texto si usamos la imagen de 8 bits
                font.draw(game.batch, "x" + cantidad, x + w * 0.75f, y + h * 0.65f);
            } else {
                font.draw(game.batch, "x" + cantidad, x + w - 60, y + h / 2f + 10);
            }
        }
    }

    /**
     * Dibuja un slot de inventario (para Pokeballs).
     * 
     * @param x         Posición X.
     * @param y         Posición Y.
     * @param w         Ancho.
     * @param h         Alto.
     * @param resaltado Si true, usa la textura de color (resaltado).
     */
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
            } catch (ExcepcionEquipoLleno e) {
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

        // Cuadro de informaci├│n del Pokemon
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

        // Sprite del Pokemon (izquierda del cuadro)
        Texture spritePokemon = gestorSprites.obtenerSprite(pokemonSalvaje.getNombre());
        if (spritePokemon != null) {
            float spriteX = cuadroX + 40;
            float spriteY = cuadroY + 100;
            float spriteSize = 200f; // Tamaño del sprite
            game.batch.draw(spritePokemon, spriteX, spriteY, spriteSize, spriteSize);
        }

        // Informaci├│n del Pokemon (derecha del cuadro)
        font.setColor(Color.WHITE);
        font.getData().setScale(2.0f);
        String mensaje = "¡Un " + pokemonSalvaje.getNombre() + " salvaje apareció!";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, mensaje);
        float textoX = cuadroX + 280; // A la derecha del sprite
        font.draw(game.batch, mensaje, textoX + (cuadroW - 280 - layout.width) / 2f, cuadroY + cuadroH - 50);

        font.getData().setScale(1.5f);
        String info = pokemonSalvaje.toString();
        font.draw(game.batch, info, textoX, cuadroY + cuadroH - 120);

        // Instrucciones
        font.getData().setScale(1.2f);
        font.setColor(Color.YELLOW);
        String instrucciones = "ENTER: Capturar con Pokeball | ESPACIO: Huir";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layoutInst = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font,
                instrucciones);
        font.draw(game.batch, instrucciones, cuadroX + (cuadroW - layoutInst.width) / 2f, cuadroY + 50);

        // Mostrar probabilidad de captura
        if (sistemaCaptura != null) {
            try {
                double probabilidad = sistemaCaptura.calcularProbabilidadCaptura(pokemonSalvaje, "Pokeball");
                String probTexto = "Probabilidad de captura: " + String.format("%.1f%%", probabilidad * 100);
                font.setColor(Color.CYAN);
                font.getData().setScale(1.0f);
                com.badlogic.gdx.graphics.g2d.GlyphLayout layoutProb = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                        font, probTexto);
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
        // Verificar que la cámara esté inicializada antes de acceder a ella
        if (camera == null) {
            return;
        }
        // Ajustamos la proporcion de la camara segun el tama├▒o de la ventana.
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
        if (gestorSprites != null)
            gestorSprites.dispose();
        if (texSoltarLetra != null)
            texSoltarLetra.dispose();
        if (texBotonSoltar != null)
            texBotonSoltar.dispose();
        if (texBotonSoltarColor != null)
            texBotonSoltarColor.dispose();
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

    /**
     * Intenta craftear el item seleccionado en el menú de crafteo.
     * Verifica si hay materiales suficientes y espacio en el inventario.
     * 
     * @throws ExcepcionMaterialesInsuficientes Si no hay suficientes materiales.
     * @throws ExcepcionInventarioLleno         Si el inventario está lleno.
     */
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
            // NOTA: AgregarObjeto lanzar├í excepcion si est├í lleno, pero es mejor saber
            // antes.
            // Para ser atomicos, simularemos validacion.

            // Si el objeto ya existe y tiene >= 10, agregarObjeto tirara excepcion.
            // Asi que consumimos y luego agregamos? No, si falla el agregar perdemos items.
            // Primero intentamos agregar (dry run) o confiamos en la excepcion.
            // Almacenamiento no tiene metodo canAdd pero agregarObjeto verifica.
            // Sin embargo agregarObjeto modifica el estado.
            // Verificamos manualmente:
            int cantProd = inv.getOrDefault(producto, 0);
            if (cantProd >= 10) { // 10 es MAX_ITEMS, deber├¡a ser publico en Almacenamiento o hardcoded igual
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

    /**
     * Muestra un mensaje de error en pantalla durante un tiempo limitado.
     * 
     * @param mensaje El mensaje de error a mostrar.
     */
    private void mostrarError(String mensaje) {
        this.mostrandoError = true;
        this.mensajeError = mensaje;
        this.tiempoMensajeError = 3.0f; // 3 segundos
    }

    /**
     * Dibuja el cuadro de error activo si hay uno.
     * 
     * @param delta Tiempo transcurrido para actualizar el temporizador.
     */
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
        float w = sw * 0.9f;
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
        // Reproducir música del mapa cuando se muestra la pantalla
        reproducirMusicaMapa();
    }

    /**
     * Reproduce la música correspondiente al mapa actual.
     */
    private void reproducirMusicaMapa() {
        if (nombreMapa == null) {
            return;
        }

        // Determinar qué música reproducir según el nombre del mapa
        String nombreMapaLower = nombreMapa.toLowerCase();

        if (nombreMapaLower.contains("verde") || nombreMapaLower.contains("mapaverdepokemon")) {
            GestorMusica.reproducirMusica(GestorMusica.TipoMusica.MAPA_VERDE);
        } else if (nombreMapaLower.contains("azul") || nombreMapaLower.contains("mapaazulpokemon")) {
            GestorMusica.reproducirMusica(GestorMusica.TipoMusica.MAPA_AZUL);
        } else {
            // Por defecto, usar música del mapa verde
            GestorMusica.reproducirMusica(GestorMusica.TipoMusica.MAPA_VERDE);
        }
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

    // --- METODOS DE DIALOGO ---
    private void mostrarDialogo(String texto) {
        if (texto == null || texto.isEmpty())
            return;
        this.textoActual = texto;
        this.mostrandoDialogo = true;
    }

    private void cerrarDialogo() {
        this.mostrandoDialogo = false;
        this.textoActual = "";

        // Si el NPC era un enemigo, iniciar pelea
        if (npcActual != null) {
            String tipo = npcActual.getTipo();
            if (tipo != null && (tipo.toLowerCase().contains("enemigo") || "Enemigo".equalsIgnoreCase(tipo))) {
                System.out.println("Iniciando batalla tras dialogo con enemigo...");

                // Generar Pokemon de NPC (Nivel 4-6)
                java.util.Random rnd = new java.util.Random();
                int nivelNPC = rnd.nextInt(3) + 4; // 4, 5, 6
                int pokeIndex = rnd.nextInt(6); // 0-5

                Pokemon rival = null;
                switch (pokeIndex) {
                    case 0:
                        rival = new com.Proyecto.Pokemon.pokemon.PokeFuego.Ignirrojo("Macho", nivelNPC);
                        break;
                    case 1:
                        rival = new com.Proyecto.Pokemon.pokemon.PokeFuego.Volcarex("Macho", nivelNPC);
                        break;
                    case 2:
                        rival = new com.Proyecto.Pokemon.pokemon.PokeAgua.Aqualisca("Macho", nivelNPC);
                        break;
                    case 3:
                        rival = new com.Proyecto.Pokemon.pokemon.PokeAgua.Mareonix("Macho", nivelNPC);
                        break;
                    case 4:
                        rival = new com.Proyecto.Pokemon.pokemon.PokePlanta.Brotalamo("Macho", nivelNPC);
                        break;
                    case 5:
                        rival = new com.Proyecto.Pokemon.pokemon.PokePlanta.Floravelo("Macho", nivelNPC);
                        break;
                    default:
                        rival = new com.Proyecto.Pokemon.pokemon.PokeFuego.Ignirrojo("Macho", nivelNPC);
                        break;
                }

                Pokemon miPokemon = null;
                // Buscar el primer pokemon vivo del equipo
                for (int i = 0; i < jugador.getEquipo().getCantidad(); i++) {
                    Pokemon p = jugador.getEquipo().getPokemon(i);
                    if (p.estaVivo()) {
                        miPokemon = p;
                        break;
                    }
                }

                // Fallback si todos debilitados (no debería pasar normalmente)
                if (miPokemon == null) {
                    if (jugador.getEquipo().getCantidad() > 0) {
                        miPokemon = jugador.getEquipo().getPokemon(0);
                    } else {
                        miPokemon = game.getPokemonInicial();
                        if (miPokemon == null) {
                            miPokemon = new com.Proyecto.Pokemon.pokemon.PokeFuego.Ignirrojo("Macho");
                        }
                    }
                }

                game.setScreen(new PantallaBatalla(game, this, miPokemon, rival, true));
            }
            npcActual = null; // Resetear tras cerrar
        }
    }

    private void actualizarEntradaDialogo() {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.Z) ||
                Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.X) ||
                Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            cerrarDialogo();
        }
    }

    private void dibujarDialogo() {
        // Overlay semitransparente oscuro
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // --- USAR ESCALA VIRTUAL FIJA (1280x720) ---
        game.batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, UI_WIDTH, UI_HEIGHT));
        game.batch.begin();

        // Dimensiones en coordenadas virtuales
        float width = UI_WIDTH * 0.9f;
        float height = UI_HEIGHT * 0.22f;

        // Posicionar en la parte inferior
        float x = (UI_WIDTH - width) / 2f;
        float y = UI_HEIGHT * 0.05f;

        // DIBUJAR FONDO
        float r = 10f;
        game.batch.setColor(0.05f, 0.05f, 0.05f, 0.9f);
        game.batch.draw(pixel, x + r, y, width - 2 * r, height);
        game.batch.draw(pixel, x, y + r, r, height - 2 * r);
        game.batch.draw(pixel, x + width - r, y + r, r, height - 2 * r);

        // DIBUJAR BORDE BLANCO
        game.batch.setColor(Color.WHITE);
        float b = 3f;
        game.batch.draw(pixel, x + r, y, width - 2 * r, b);
        game.batch.draw(pixel, x + r, y + height - b, width - 2 * r, b);
        game.batch.draw(pixel, x, y + r, b, height - 2 * r);
        game.batch.draw(pixel, x + width - b, y + r, b, height - 2 * r);

        game.batch.draw(pixel, x + b, y + b, r - b, b);
        game.batch.draw(pixel, x + b, y + b, b, r - b);
        game.batch.draw(pixel, x + width - r, y + b, r - b, b);
        game.batch.draw(pixel, x + width - b, y + b, b, r - b);
        game.batch.draw(pixel, x + b, y + height - r, b, r - b);
        game.batch.draw(pixel, x + b, y + height - b, r - b, b);
        game.batch.draw(pixel, x + width - b, y + height - r, b, r - b);
        game.batch.draw(pixel, x + width - r, y + height - b, r - b, b);

        // DIBUJAR TEXTO
        game.batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE); // Asegurar color blanco
        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;
        font.getData().setScale(2.5f); // Escala ajustada a 1280x720

        float marginX = 40f;
        float marginY = 30f;
        font.draw(game.batch, textoActual, x + marginX, y + height - marginY, width - 2 * marginX,
                com.badlogic.gdx.utils.Align.topLeft, true);

        font.getData().setScale(oldScaleX, oldScaleY);

        game.batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Restaurar matriz para el resto del juego
        game.batch.setProjectionMatrix(camera.combined);
    }

}
