package com.Proyecto.Pokemon.gui;

import com.Proyecto.Pokemon.Main;
import com.Proyecto.Pokemon.NPC;
import com.Proyecto.Pokemon.PantallaBatalla;
import com.Proyecto.Pokemon.PantallaPokemonCapturados;
import com.Proyecto.Pokemon.jugador.Player;
import com.Proyecto.Pokemon.pokemon.Pokemon;
import com.Proyecto.Pokemon.pokemon.PokePlanta;
import com.Proyecto.Pokemon.pokemon.PokeFuego;
import com.Proyecto.Pokemon.sistema.SpawnPokemon;
import com.Proyecto.Pokemon.sistema.CapturaPokemon;
import com.Proyecto.Pokemon.sistema.GestorGuardado;
import com.Proyecto.Pokemon.sistema.GestorMusica;
import com.Proyecto.Pokemon.excepciones.ExcepcionInventarioLleno;
import com.Proyecto.Pokemon.excepciones.ExcepcionMaterialesInsuficientes;
import com.Proyecto.Pokemon.excepciones.ExcepcionPokebolaInsuficiente;
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
    private int opcionPausa = 0; // 0: Volver, 1: Opciones, 2: Salir
    private boolean saliendoAlMenu = false; // Bandera para indicar que estamos saliendo
    // --- ESTADO CRAFTEO ---
    private boolean menuCrafteoAbierto = false;
    private int opcionCrafteo = 1; // 0, 1, 2
    // --- ESTADO POKEMON CAPTURADOS ---
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
    private GestorSpritesPokemon gestorSprites;

    private static final int OPCION_REANUDAR = 0;
    private static final int OPCION_SALIR_MENU = 1;
    private static final int CANTIDAD_OPCIONES = 2;

    private static final int INV_CRAFTEAR = 0;

    private float anchoMapa, altoMapa;
    // Escala unitaria: 1 unidad de mundo = 16 pixeles (tama├▒o de un tile).
    private static final float UNIT_SCALE = 1 / 16f;

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

                    // Tambi├⌐n verificar si la capa tiene la propiedad de hierba
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

        // Verificar si est├í en hierba
        if (estaEnHierba(x, y)) {
            // Intentar spawn de Pokemon
            Pokemon pokemonEncontrado = spawnPokemon.verificarEncuentro();
            if (pokemonEncontrado != null) {
                pokemonSalvaje = pokemonEncontrado;
                enEncuentro = true;
                System.out.println("┬íUn " + pokemonSalvaje.getNombre() + " salvaje apareci├│!");
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
        // Buscamos la capa de portales de forma m├ís flexible (insensible a may├║sculas).
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
                            // Agregar prefijo Tiled/ si no está presente
                            if (!siguienteMapa.startsWith("Tiled/")) {
                                siguienteMapa = "Tiled/" + siguienteMapa;
                            }

                            // Al cambiar de mapa, le pasamos el nombre del mapa actual para que el nuevo
                            // sepa donde colocarnos (en el portal que apunta hacia AQUI)
                            String mapaActual = nombreMapa;
                            // Quitamos extension si la tiene para comparaciones mas limpias
                            if (mapaActual.endsWith(".tmx")) {
                                mapaActual = mapaActual.substring(0, mapaActual.length() - 4);
                            }

                            game.setScreen(new Mapa(game, siguienteMapa, mapaActual));
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
        // Si estamos saliendo al menú, no renderizar nada más
        if (saliendoAlMenu) {
            return;
        }

        boolean enHierba = false;
        for (Rectangle zona : zonasHierba) {
            if (zona.contains(jugador.getX(), jugador.getY())) {
                enHierba = true;
                break;
            }
        }

        // LÓGICA DE HIERBA
        for (Rectangle zona : zonasHierba) {
            if (zona.contains(jugador.getX(), jugador.getY())) {
                enHierba = true;
                break;
            }
        }

        if (enHierba && jugador.isMoviendose()) {
            grassTimer += delta;
            if ((int) grassTimer > (int) (grassTimer - delta)) {
                System.out.println("Segundos en hierba: " + (int) grassTimer);
            }

            if (grassTimer >= 5f) {
                System.out.println("Inicia pelea pokemon!");
                grassTimer = 0;

                // Iniciar Batalla
                // Crear un pokemon rival aleatorio (usando SpawnPokemon que ya existe en esta
                // clase)
                Pokemon rival = spawnPokemon.verificarEncuentro();
                if (rival == null) {
                    // Fallback si spawn devuelve null (aunque verificamos encuentro forzado por
                    // tiempo)
                    rival = new PokePlanta.Brotalamo("Salvaje");
                }

                // Obtener pokemon del jugador
                Pokemon miPokemon = game.getPokemonInicial();
                if (miPokemon == null) {
                    miPokemon = new PokeFuego.Ignirrojo("Macho"); // Fallback
                }

                // Cambiar a pantalla de batalla
                // Importante: Pasamos 'this' (Mapa) para poder volver
                game.setScreen(new PantallaBatalla(game, this, miPokemon, rival));
            }
        } else if (!enHierba) {
            grassTimer = 0;
        }

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
            if (!pausado) {
                if (menuCrafteoAbierto) {
                    menuCrafteoAbierto = false;
                    inventarioAbierto = true; // Volver al inv
                } else {
                    inventarioAbierto = !inventarioAbierto;
                }
            }
        }

        // Al pulsar P abrir pantalla de Pokemon capturados.
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.P)) {
            if (!pausado && !inventarioAbierto && !menuCrafteoAbierto && !enEncuentro) {
                // Abrir directamente la pantalla de Pokemon capturados
                game.setScreen(new PantallaPokemonCapturados(game, this));
            }
        }

        if (pausado) {
            actualizarEntradaPausa();
        } else if (enEncuentro) {
            actualizarEntradaEncuentro();
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

        // Renderizar NPCs
        if (npcs != null) {
            for (NPC npc : npcs) {
                npc.render(game.batch);
            }
        }

        game.batch.end();

        // DIBUJAR OVERLAY DE PAUSA
        if (pausado) {
            dibujarMenuPausa();
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
        } else {
            if (mostrandoError)
                dibujarCuadroError(delta); // Mostrar error en juego normal (ej: pickup)
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
            // En lugar de salir inmediatamente, lanzar la excepción para que se maneje correctamente
            throw new RuntimeException("No se pudo cargar el mapa: " + nombreArchivo, e);
        }

        renderer = new OrthogonalTiledMapRenderer(mapaTiled, UNIT_SCALE, game.batch);

        // 2. BUSCAMOS LA HIERBA
        MapLayer capaLogica = mapaTiled.getLayers().get("LogicaHierba");
        if (capaLogica != null) {
            for (MapObject objeto : capaLogica.getObjects()) {
                if (objeto instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) objeto).getRectangle();
                    Rectangle rectEscalado = new Rectangle(
                            rect.x * UNIT_SCALE,
                            rect.y * UNIT_SCALE,
                            rect.width * UNIT_SCALE,
                            rect.height * UNIT_SCALE);
                    zonasHierba.add(rectEscalado);
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

        // --- SPAWN LOGIC ---
        float spawnX = 10;
        float spawnY = 10;

        if (nombreMapaAnterior != null) {
            // Buscamos un portal que tenga como Destino el mapa anterior
            MapLayer capaPortales = null;
            for (MapLayer layer : mapaTiled.getLayers()) {
                if (layer.getName().equalsIgnoreCase("Portal") || layer.getName().equalsIgnoreCase("Portales")) {
                    capaPortales = layer;
                    break;
                }
            }

            if (capaPortales != null) {
                for (MapObject obj : capaPortales.getObjects()) {
                    if (obj instanceof RectangleMapObject) {
                        String destino = obj.getProperties().get("Destino", String.class);
                        if (destino != null) {
                            // Normalizamos quitando .tmx para comparar
                            String destClean = destino.replace(".tmx", "");
                            String prevClean = nombreMapaAnterior.replace(".tmx", "");

                            if (destClean.equalsIgnoreCase(prevClean)) {
                                Rectangle rect = ((RectangleMapObject) obj).getRectangle();

                                float pX = rect.x * UNIT_SCALE;
                                float pY = rect.y * UNIT_SCALE;

                                spawnX = pX;
                                spawnY = pY;

                                // Heuristica para no spawnear ENCIMA del portal y volver a teletransportarse
                                // Si estamos en un borde, nos movemos hacia adentro.
                                // Si no, por defecto nos movemos hacia abajo (asumiendo puerta vertical).
                                if (pY < 2) {
                                    spawnY += 1; // Borde inferior -> Mover Arriba
                                } else if (pY > altoMapa - 3) {
                                    spawnY -= 1; // Borde superior -> Mover Abajo
                                } else if (pX < 2) {
                                    spawnX += 1; // Borde izquierdo -> Mover Derecha
                                } else if (pX > anchoMapa - 3) {
                                    spawnX -= 1; // Borde derecho -> Mover Izquierda
                                } else {
                                    spawnY -= 1; // Default: Mover Abajo (Salir de puerta)
                                }

                                System.out.println("Spawn encontrado hacia: " + destino + " | Offset aplicado");
                                break; // Encontramos el portal, paramos
                            }
                        }
                    }
                }
            }
        }

        // Jugador
        this.jugador = game.getJugador();
        if (this.jugador != null) {
            // Si hay un mapa anterior, estamos cambiando de mapa (usar posición del portal)
            // Si no hay mapa anterior, estamos iniciando/cargando en este mapa
            // En ambos casos, actualizamos la posición del jugador
            // La única excepción es si el jugador fue cargado desde guardado y está en una posición válida
            // (pero esto se maneja en PantallaSeleccionPartida que establece la posición antes de crear el mapa)
            
            // Si hay mapa anterior, es un cambio de mapa (usar portal)
            // Si no hay mapa anterior pero la posición es diferente de (10,10), mantenerla (cargado)
            // Si no hay mapa anterior y está en (10,10), usar spawn calculado
            float jugadorX = this.jugador.getX();
            float jugadorY = this.jugador.getY();
            boolean esPosicionPorDefecto = Math.abs(jugadorX - 10f) < 0.1f && Math.abs(jugadorY - 10f) < 0.1f;
            
            if (nombreMapaAnterior != null || (nombreMapaAnterior == null && esPosicionPorDefecto)) {
                // Cambio de mapa O nueva partida: usar spawn calculado
                this.jugador.getPosicion().set(spawnX, spawnY);
                this.jugador.getDestino().set(spawnX, spawnY);
            }
            // Si no hay mapa anterior y no es posición por defecto, mantener la posición (partida cargada)
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

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();

        texCraftear = new Texture(Gdx.files.internal("Boton de Craftear base.png"));
        texCraftearC = new Texture(Gdx.files.internal("Boton de Craftear.jpeg"));
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

        // NPC Init
        npcs = new java.util.ArrayList<>();

        // Texture region default for NPC
        Texture playerTex = new Texture(Gdx.files.internal("player_sprite.png"));
        TextureRegion npcRegion = new TextureRegion(playerTex, 0, 0, playerTex.getWidth() / 4,
                playerTex.getHeight() / 4);

        for (MapLayer layer : mapaTiled.getLayers()) {
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

        // --- CASO 0: INTERACCION CON NPC ---
        if (npcs != null) {
            for (NPC npc : npcs) {
                // Chequear si la posicion de interaccion (x, y) esta cerca del NPC
                // Usamos una tolerancia pequeña (0.5f) ya que x,y son enteras a veces
                if (npc.getPosicion().dst(x, y) < 1.0f) {
                    npc.interactuar();
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
        if (tile == null || tile.getProperties() == null)
            return null;
        if (tile.getProperties().containsKey(key))
            return tile.getProperties().get(key).toString(); // Direct check

        for (Iterator<String> it = tile.getProperties().getKeys(); it.hasNext();) {
            String k = it.next();
            if (k.equalsIgnoreCase(key)) {
                return tile.getProperties().get(k).toString();
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
                // Guardar partida antes de salir
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
            boolean guardado = GestorGuardado.guardarPartida(jugador, pokemonInicial, nombreMapaLimpio);
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

        // Boton Reanudar (arriba).
        Texture texReanudar = (opcionPausa == OPCION_REANUDAR) ? pausaVolverC : pausaVolver;
        game.batch.draw(texReanudar, x, centroY + btnH / 2, btnW, btnH);

        // Boton Salir al Men├║ Principal (abajo).
        Texture texSalir = (opcionPausa == OPCION_SALIR_MENU) ? pausaSalirC : pausaSalir;
        game.batch.draw(texSalir, x, centroY - btnH / 2, btnW, btnH);

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
                    float iconSize = slotW * 0.5f;
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
}