package com.Proyecto.Pokemon;

import com.Proyecto.Pokemon.pokemon.Pokemon;
import com.Proyecto.Pokemon.pokemon.PokeFuego;
import com.Proyecto.Pokemon.pokemon.PokeAgua;
import com.Proyecto.Pokemon.pokemon.PokePlanta;
import com.Proyecto.Pokemon.pokemon.PokeDragon;
import com.Proyecto.Pokemon.sistema.Batalla;
import com.Proyecto.Pokemon.sistema.GestorMusica;
import com.Proyecto.Pokemon.sistema.CapturaPokemon;
import com.Proyecto.Pokemon.excepciones.ExcepcionPokebolaInsuficiente;
import com.Proyecto.Pokemon.excepciones.ExcepcionEquipoLleno;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Pantalla que gestiona la visualización de la batalla Pokemon.
 * Permite usar ataques, objetos y cambiar de pokemon.
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

    // Textura especial derrota
    private Texture imgGatoRisa;

    // Sprites temporales
    private Texture spriteJugador;
    private Texture spriteRival;

    // Textura blanca para el cuadro de mensajes
    private Texture texturaBlanca;

    // Lógica UI
    private int opcionSeleccionada = 0;
    private BitmapFont font;
    private BitmapFont fontPequeña;

    // Log de batalla
    private String mensajeLog = "";
    private com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

    // Estados del menú
    // Estados del menú
    private enum EstadoMenu {
        MENU_PRINCIPAL,
        MENU_ATAQUES,
        MENU_MOCHILA,
        MENU_EQUIPO,
        MENSAJE_DERROTA, // Nuevo estado para control estricto de mensaje derrota
        MENSAJE_VICTORIA, // Nuevo estado para control estricto de mensaje victoria
        MENSAJE_CAPTURA // Nuevo estado para control estricto de mensaje captura
    }

    private EstadoMenu estadoActual = EstadoMenu.MENU_PRINCIPAL;

    // Menú de ataques
    private List<String> ataquesDisponibles;
    private int ataqueSeleccionado = 0;

    // Menú de mochila
    private List<String> pokebolasDisponibles;
    private int pokebolaSeleccionada = 0;

    // Menú de equipo (Marcos 8-bit)
    private Texture marcoGenericoVacio;
    private Texture texSalir, texSalirC;
    private HashMap<String, Texture> marcosPokemon;
    private HashMap<String, Texture> marcosPokemonVacio;
    private int equipoSeleccionado = 0; // 0-5 para slots, 6 para Salir

    // Flag para saber si es batalla contra entrenador
    private boolean esBatallaEntrenador = false;
    private boolean cambioForzado = false;

    public PantallaBatalla(Main game, Screen pantallaAnterior, Pokemon pokemonJugador, Pokemon pokemonRival) {
        this(game, pantallaAnterior, pokemonJugador, pokemonRival, false);
    }

    public PantallaBatalla(Main game, Screen pantallaAnterior, Pokemon pokemonJugador, Pokemon pokemonRival,
            boolean esEntrenador) {
        this.game = game;
        this.pantallaAnterior = pantallaAnterior;
        this.esBatallaEntrenador = esEntrenador;

        // NOTA: Eliminamos la sincronización por atributos para respetar la identidad
        // única de cada objeto Pokemon.
        // Si el usuario tiene 2 Ignirrojos, son dos objetos distintos.
        this.pokemonJugador = pokemonJugador;
        this.pokemonRival = pokemonRival;

        // Inicializar cámara para UI (pixel-perfect)
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Inicializar lógica de batalla
        this.batalla = new Batalla(pokemonJugador, pokemonRival);

        cargarAssets();
        inicializarAtaquesDisponibles();
        inicializarPokebolasDisponibles();

        // Limpiar mensajes de batallas anteriores
        mensajeLog = "";

        // Reproducir música de batalla
        GestorMusica.reproducirMusica(GestorMusica.TipoMusica.BATALLA);
    }

    /**
     * Carga todos los recursos gráficos (texturas) necesarios para la batalla.
     * Incluye fondos, botones y sprites de los Pokemon.
     */
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

        // Cargar imagen de derrota
        try {
            imgGatoRisa = new Texture(Gdx.files.internal("GATORISA.jpg"));
        } catch (Exception e) {
            System.err.println("No se encontró GATORISA.jpg, usando fallback");
            imgGatoRisa = null;
        }

        // Usar GestorSpritesPokemon para cargar los sprites correctos según el nombre
        gestorSprites = new com.Proyecto.Pokemon.gui.GestorSpritesPokemon();
        spriteJugador = gestorSprites.obtenerSpriteAtras(pokemonJugador.getNombre(), 0);
        spriteRival = gestorSprites.obtenerSpriteFrente(pokemonRival.getNombre(), 0);

        // Fallbacks por si el gestor falla (aunque el gestor imprime errores)
        if (spriteJugador == null) {
            // Textura por defecto o error (rosa/negra)
            com.badlogic.gdx.graphics.Pixmap p = new com.badlogic.gdx.graphics.Pixmap(32, 32,
                    com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            p.setColor(Color.MAGENTA);
            p.fill();
            spriteJugador = new Texture(p);
        }
        if (spriteRival == null) {
            com.badlogic.gdx.graphics.Pixmap p = new com.badlogic.gdx.graphics.Pixmap(32, 32,
                    com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            p.setColor(Color.RED);
            p.fill();
            spriteRival = new Texture(p);
        }

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        fontPequeña = new BitmapFont();
        fontPequeña.setColor(Color.WHITE);
        fontPequeña.getData().setScale(1.2f);

        // Crear textura blanca para el cuadro de mensajes
        com.badlogic.gdx.graphics.Pixmap pixmapBlanco = new com.badlogic.gdx.graphics.Pixmap(1, 1,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmapBlanco.setColor(Color.WHITE);
        pixmapBlanco.fill();
        texturaBlanca = new Texture(pixmapBlanco);
        pixmapBlanco.dispose();

        // Cargar assets para el menú de equipo
        marcoGenericoVacio = new Texture(Gdx.files.internal("Marco 8bit.png"));
        texSalir = new Texture(Gdx.files.internal("Salir.png"));
        texSalirC = new Texture(Gdx.files.internal("SalirC.png"));
        marcosPokemon = new HashMap<>();
        marcosPokemonVacio = new HashMap<>();
        preCargarMarcosEquipo();
    }

    private void preCargarMarcosEquipo() {
        List<Pokemon> team = game.getJugador().getEquipo().getPokemons();
        for (Pokemon p : team) {
            String nombre = p.getNombre().toLowerCase()
                    .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                    .replace("ñ", "n");

            // Especial para Volcarex y Mareonix que tienen tildes en el nombre pero tal vez
            // no en el archivo
            if (!marcosPokemon.containsKey(nombre)) {
                try {
                    // Marcos organizados por Pokémon:
                    // assets/pokemon/<NombrePokemon>/Marco 8bit <NombreCap>.png
                    String folderPokemon = "pokemon/" + p.getNombre() + "/";
                    String nombreCap = nombre.substring(0, 1).toUpperCase() + nombre.substring(1); // sin acentos

                    String marcoPath = folderPokemon + "Marco 8bit " + nombreCap + ".png";
                    String marcoVacioPath = folderPokemon + "Marco 8bit " + nombreCap + " vacio.png";

                    // Si no existen, dejamos fallback a null y luego el dibujado usa
                    // marcoGenericoVacio.
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

    // Gestor de sprites (mejora del remoto)
    private com.Proyecto.Pokemon.gui.GestorSpritesPokemon gestorSprites;

    /**
     * Inicializa la lista de ataques disponibles según el tipo de Pokemon del
     * jugador.
     */
    private void inicializarAtaquesDisponibles() {
        ataquesDisponibles = new ArrayList<>();
        if (pokemonJugador instanceof PokeFuego) {
            ataquesDisponibles.add("Lanzallamas");
            ataquesDisponibles.add("Llamarada");
        } else if (pokemonJugador instanceof PokeAgua) {
            ataquesDisponibles.add("Hidrochorro");
            ataquesDisponibles.add("Burbuja");
        } else if (pokemonJugador instanceof PokePlanta) {
            ataquesDisponibles.add("Hoja Afilada");
            ataquesDisponibles.add("Absorber");
        } else if (pokemonJugador instanceof PokeDragon) {
            ataquesDisponibles.add("Rayo Draconico");
            ataquesDisponibles.add("Cola Dragon");
        }

        // Siempre añadir Salir al final del menú de ataques
        ataquesDisponibles.add("Salir");

        ataqueSeleccionado = 0;
    }

    /**
     * Inicializa la lista de pokebolas disponibles según el inventario del jugador.
     */
    private void inicializarPokebolasDisponibles() {
        pokebolasDisponibles = new ArrayList<>();
        HashMap<String, Integer> inventario = game.getJugador().getInventario().getMapa();

        // Solo agregar pokebolas que el jugador tenga
        if (inventario.getOrDefault("Pokeball", 0) > 0) {
            pokebolasDisponibles.add("Pokeball");
        }
        if (inventario.getOrDefault("PokeballEXP", 0) > 0) {
            pokebolasDisponibles.add("PokeballEXP");
        }
        if (inventario.getOrDefault("PokeballCura", 0) > 0) {
            pokebolasDisponibles.add("PokeballCura");
        }

        // Siempre añadir Salir al final
        pokebolasDisponibles.add("Salir");

        pokebolaSeleccionada = 0;
    }

    // Variable para controlar si el rival debe atacar automáticamente
    private boolean esperandoAtaqueRival = false;
    private float tiempoEsperaRival = 0f;
    private static final float DELAY_ATAQUE_RIVAL = 0.5f; // Esperar 0.5 segundos antes del ataque

    // Estado Derrota Jugador
    private boolean procesandoDerrotaJugador = false;
    private float tiempoPantallaNegra = 0f;
    private static final float DURACION_PANTALLA_NEGRA = 3.0f;
    // private boolean pantallaNegraCompleta = false; // Eliminado por no usarse

    // Animación de ataques
    private boolean animandoAtaque = false;
    private boolean animandoAtaqueJugador = false; // true si es el jugador, false si es el rival
    private float tiempoAnimacion = 0f;
    private int frameAnimacion = 0; // Frame actual (0, 1, 2)
    private static final float DURACION_FRAME = 0.3f; // Duración de cada frame en segundos (más lento para mejor
                                                      // apreciación)
    private static final float DURACION_TOTAL_ANIMACION = 0.9f; // Duración total (3 frames * 0.3s)

    /**
     * Ciclo principal de renderizado de la batalla.
     * Gestiona la lógica de turnos, animaciones y dibujado de la interfaz.
     * 
     * @param delta Tiempo transcurrido desde el último frame.
     */
    @Override
    public void render(float delta) {
        manejarInput();

        // Actualizar animación de ataque
        if (animandoAtaque) {
            tiempoAnimacion += delta;
            frameAnimacion = (int) (tiempoAnimacion / DURACION_FRAME) % 3; // Ciclar entre 0, 1, 2

            // Terminar animación después de la duración total
            if (tiempoAnimacion >= DURACION_TOTAL_ANIMACION) {
                animandoAtaque = false;
                tiempoAnimacion = 0f;
                frameAnimacion = 0; // Volver al frame inicial
            }
        }

        // Si es el turno del rival y está vivo, atacar automáticamente después de un
        // delay
        if (!batalla.estaTerminada() && batalla.getAtacante() == pokemonRival &&
                pokemonRival.estaVivo() && pokemonJugador.estaVivo() &&
                !procesandoDerrotaJugador &&
                estadoActual == EstadoMenu.MENU_PRINCIPAL && !esperandoAtaqueRival && !animandoAtaque) {
            tiempoEsperaRival += delta;
            if (tiempoEsperaRival >= DELAY_ATAQUE_RIVAL) {
                realizarAtaqueRival();
                tiempoEsperaRival = 0f;
            }
        } else if (batalla.getAtacante() != pokemonRival || batalla.estaTerminada()) {
            // Resetear el timer si ya no es el turno del rival
            tiempoEsperaRival = 0f;
        }

        // Lógica de derrota del jugador
        if (procesandoDerrotaJugador) {
            tiempoPantallaNegra += delta;
            if (tiempoPantallaNegra >= DURACION_PANTALLA_NEGRA) {
                finalizarDerrotaJugador();
                return;
            }
        }

        // Manejar input para estados de victoria/derrota MOVIDO A manejarInput()
        // para evitar que se procese el mismo frame que se activa el estado.

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        // Si estamos en derrota, dibujar pantalla negra
        if (procesandoDerrotaJugador) {
            // Dibujar fondo negro (usando textura blanca tintada de negro cubriendo todo)
            game.batch.setColor(Color.BLACK);
            game.batch.draw(texturaBlanca, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.batch.setColor(Color.WHITE);
            // Mostrar texto quizás? El usuario dijo "pantalla se pondra en negro por 3
            // segundos"
            // Puede que quiera el log visible antes, pero luego negro.
            // Asumiremos negro total con imagen centrada.

            if (imgGatoRisa != null) {
                float w = imgGatoRisa.getWidth();
                float h = imgGatoRisa.getHeight();
                // Centrar imagen
                game.batch.draw(imgGatoRisa,
                        (Gdx.graphics.getWidth() - w) / 2,
                        (Gdx.graphics.getHeight() - h) / 2);
            }

            game.batch.end();
            return;
        }

        // 1. Dibujar Fondo
        game.batch.draw(fondoBatalla, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 2. Dibujar Pokemon
        dibujarEscenaBatalla();

        // 3. Dibujar UI según el estado actual
        switch (estadoActual) {
            case MENU_PRINCIPAL:
                // Solo dibujar botones si es turno del jugador y no hay animación en curso
                boolean turnoJugador = !batalla.estaTerminada() && batalla.getAtacante() == pokemonJugador;
                if (turnoJugador && !animandoAtaque && !esperandoAtaqueRival) {
                    dibujarBotones();
                }
                break;
            case MENU_ATAQUES:
                dibujarMenuAtaques();
                break;
            case MENU_MOCHILA:
                dibujarMenuMochila();
                break;
            case MENU_EQUIPO:
                dibujarMenuEquipo();
                break;
            case MENSAJE_DERROTA:
            case MENSAJE_VICTORIA:
            case MENSAJE_CAPTURA:
                // No dibujamos menús en estos estados, solo el mensaje que se dibuja abajo
                break;
        }

        // 4. Dibujar Mensajes con cuadro blanco de fondo
        if (!mensajeLog.isEmpty()) {
            dibujarMensajeConFondo(mensajeLog);
        }

        game.batch.end();
    }

    /**
     * Realiza el ataque automático del rival.
     */
    private void realizarAtaqueRival() {
        if (batalla.estaTerminada() || !pokemonRival.estaVivo() || !pokemonJugador.estaVivo()) {
            return;
        }

        esperandoAtaqueRival = true;

        // Iniciar animación del rival atacando
        animandoAtaque = true;
        animandoAtaqueJugador = false; // Es el rival quien ataca
        tiempoAnimacion = 0f;
        frameAnimacion = 0;

        String ataqueRival = "";

        if (esBatallaEntrenador) {
            // NPC tiene opciones: Atacar (2 moves), Cura, Exp.
            // Vamos a darles probabilidad aleatoria.
            int opcion = com.badlogic.gdx.math.MathUtils.random(3); // 0, 1, 2, 3

            if (opcion <= 1) { // 0 o 1 (50% probabilidad de atacar)
                ataqueRival = batalla.realizarAtaque();
            } else if (opcion == 2) { // 25% Curar
                pokemonRival.curar(40);
                ataqueRival = "¡El rival usó pokeballCura!\n" + pokemonRival.getNombre() + " recuperó 40 PS.";
                batalla.consumirTurno();
            } else { // 3 (25% Exp)
                pokemonRival.subirNivel();
                ataqueRival = "¡El rival usó pokeballexp!\n¡" + pokemonRival.getNombre() + " subió de nivel!";
                batalla.consumirTurno();
            }
        } else {
            // Rival salvaje solo ataca
            ataqueRival = batalla.realizarAtaque();
        }

        mensajeLog = ataqueRival;

        // Verificar si la batalla terminó después del ataque del rival
        if (batalla.estaTerminada()) {
            Pokemon ganador = batalla.obtenerGanador();
            if (ganador == pokemonRival) {
                // El jugador perdió con este pokemon
                mensajeLog = pokemonJugador.getNombre() + " se debilitó!";
                verificarDerrotaJugador(); // Decide si es fin del juego o cambio de pokemon
            }
            // Importante: Resetear el flag para que al cambiar de pokemon (si quedan vivos)
            // el UI se dibuje correctamente (turno del jugador).
            esperandoAtaqueRival = false;
        } else {
            esperandoAtaqueRival = false;
        }
    }

    /**
     * Verifica si el jugador ha sido derrotado (sin Pokemon vivos).
     * Si pierde, muestra la pantalla de derrota. Si le quedan Pokemon, fuerza un
     * cambio.
     */
    private void verificarDerrotaJugador() {
        // Verificar si le quedan pokemons vivos
        boolean tieneVivos = false;
        for (Pokemon p : game.getJugador().getEquipo().getPokemons()) {
            if (p.estaVivo()) {
                tieneVivos = true;
                break;
            }
        }

        if (!tieneVivos) {
            // Derrota Total
            mensajeLog += "\n¡Todo tu equipo está debilitado!";
            aplicarPenalizacionDerrota(esBatallaEntrenador);
            mensajeLog += "\nHas perdido el combate. (ENTER)";
            estadoActual = EstadoMenu.MENSAJE_DERROTA;
            // Nota: procesandoDerrotaJugador se activará cuando den ENTER en el update()
        } else {
            // Aún quedan vivos
            mensajeLog += "\n¡Elige otro Pokémon!";
            estadoActual = EstadoMenu.MENU_EQUIPO;
            cambioForzado = true;
        }
    }

    /**
     * Finaliza la secuencia de derrota, curando al equipo y enviando al jugador al
     * Centro Pokemon.
     */
    private void finalizarDerrotaJugador() {
        // Curar equipo
        for (Pokemon p : game.getJugador().getEquipo().getPokemons()) {
            p.curar();
        }

        // Ir al Centro Pokemon
        // Usamos nombreMapaAnterior = "RespawnCentro" para que Mapa sepa qué hacer
        String mensajeDialogo = "Luego de que todos sus pokemones quedaran debilitados, el jugador los curo en el CentroPokemon";

        // Importante: Mapa.java debe tener las correcciones que hicimos previamente
        // para aceptar el mensaje inicial y el respawn especial.
        // Asumiendo que Mapa.java ya tiene el constructor extendido y la lógica.

        com.Proyecto.Pokemon.gui.Mapa mapaCentro = new com.Proyecto.Pokemon.gui.Mapa(game, "Tiled/MapaCentro.tmx",
                "RespawnCentro", mensajeDialogo);
        game.setScreen(mapaCentro);
        dispose();
    }

    /**
     * Dibuja la escena principal de batalla con los sprites de los Pokemon y el
     * fondo.
     * Gestiona la visualización de animaciones de ataque.
     */
    private void dibujarEscenaBatalla() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        // Obtener sprites con animación si está atacando
        Texture spriteActualJugador = spriteJugador;
        Texture spriteActualRival = spriteRival;

        if (animandoAtaque) {
            if (animandoAtaqueJugador) {
                // Jugador está atacando - usar frame de animación en posición ATRAS
                spriteActualJugador = gestorSprites.obtenerSpriteAtras(pokemonJugador.getNombre(), frameAnimacion);
            } else {
                // Rival está atacando - usar frame de animación en posición FRENTE
                spriteActualRival = gestorSprites.obtenerSpriteFrente(pokemonRival.getNombre(), frameAnimacion);
            }
        }

        // Pokemon Jugador (Izquierda - Abajo)
        float xJugador = sw * 0.15f;
        float yJugador = sh * 0.0f; // Bajado aún más para mejor visibilidad (desde 0.05f)
        float sizeJugador = sh * 0.35f;

        // Dibujar cuadro blanco con nombre y PS del jugador ARRIBA del sprite
        String textoJugador = pokemonJugador.getNombre() + " Lv." + pokemonJugador.getNivel() + " PS: "
                + pokemonJugador.getVida() + "/"
                + pokemonJugador.getVidaMaxima();
        dibujarCuadroTextoPokemon(textoJugador, xJugador, yJugador + sizeJugador + 30);

        if (spriteActualJugador != null) {
            game.batch.draw(spriteActualJugador, xJugador, yJugador, sizeJugador, sizeJugador);
        } else {
            game.batch.draw(spriteJugador, xJugador, yJugador, sizeJugador, sizeJugador);
        }

        // Pokemon Rival (Derecha - Arriba)
        float xRival = sw * 0.65f;
        float yRival = sh * 0.45f; // Bajado desde 0.55f para mejor visibilidad
        float sizeRival = sh * 0.25f;

        if (spriteActualRival != null) {
            if (estadoActual != EstadoMenu.MENSAJE_CAPTURA) {
                game.batch.draw(spriteActualRival, xRival, yRival, sizeRival, sizeRival);
            }
        } else {
            if (estadoActual != EstadoMenu.MENSAJE_CAPTURA) {
                game.batch.draw(spriteRival, xRival, yRival, sizeRival, sizeRival);
            }
        }

        // Dibujar cuadro blanco con nombre y PS del rival
        if (estadoActual != EstadoMenu.MENSAJE_CAPTURA) {
            String textoRival = pokemonRival.getNombre() + " Lv." + pokemonRival.getNivel() + " PS: "
                    + pokemonRival.getVida() + "/"
                    + pokemonRival.getVidaMaxima();
            dibujarCuadroTextoPokemon(textoRival, xRival, yRival + sizeRival + 20);
        }
    }

    /**
     * Dibuja los botones del menú principal de batalla (Pelea, Mochila, Equipo,
     * Huir).
     */
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

    /**
     * Dibuja el submenú de selección de ataques.
     */
    private void dibujarMenuAtaques() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        float menuX = sw * 0.55f;
        float menuY = sh * 0.15f;
        float itemH = 40;
        float paddingX = 20f;
        float paddingY = 15f;

        // Calcular dimensiones del menú
        float anchoMenu = 250f; // Ancho fijo para el menú
        float altoMenu = itemH * (ataquesDisponibles.size() + 1) + paddingY * 2; // Alto basado en cantidad de items +
                                                                                 // título

        // Dibujar cuadro blanco de fondo
        game.batch.setColor(Color.WHITE);
        game.batch.draw(texturaBlanca, menuX - paddingX, menuY - paddingY, anchoMenu, altoMenu);

        // Dibujar borde negro
        float grosorBorde = 3f;
        game.batch.setColor(Color.BLACK);
        // Borde superior
        game.batch.draw(texturaBlanca, menuX - paddingX, menuY - paddingY + altoMenu - grosorBorde, anchoMenu,
                grosorBorde);
        // Borde inferior
        game.batch.draw(texturaBlanca, menuX - paddingX, menuY - paddingY, anchoMenu, grosorBorde);
        // Borde izquierdo
        game.batch.draw(texturaBlanca, menuX - paddingX, menuY - paddingY, grosorBorde, altoMenu);
        // Borde derecho
        game.batch.draw(texturaBlanca, menuX - paddingX + anchoMenu - grosorBorde, menuY - paddingY, grosorBorde,
                altoMenu);
        game.batch.setColor(Color.WHITE); // Resetear color

        // Dibujar texto del menú
        fontPequeña.setColor(Color.BLACK);
        fontPequeña.draw(game.batch, "Selecciona un ataque:", menuX, menuY + itemH * (ataquesDisponibles.size() + 1));

        for (int i = 0; i < ataquesDisponibles.size(); i++) {
            String ataque = ataquesDisponibles.get(i);
            Color color = (i == ataqueSeleccionado) ? new Color(1f, 0.84f, 0f, 1f) : Color.BLACK; // Amarillo más oscuro
                                                                                                  // para mejor
                                                                                                  // contraste sobre
                                                                                                  // blanco
            fontPequeña.setColor(color);
            fontPequeña.draw(game.batch, (i == ataqueSeleccionado ? "> " : "  ") + ataque, menuX,
                    menuY + itemH * (ataquesDisponibles.size() - i));
        }
        fontPequeña.setColor(Color.WHITE); // Resetear para otros usos
    }

    /**
     * Dibuja el submenú de selección de objetos (Mochila).
     */
    private void dibujarMenuMochila() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        float menuX = sw * 0.55f;
        float menuY = sh * 0.15f;
        float itemH = 40;
        float paddingX = 20f;
        float paddingY = 15f;

        // NOTA: "Salir" ahora siempre está en la lista, así que este bloque de lista
        // vacía
        // ya no debería ejecutarse si se llama a inicializarPokebolasDisponibles
        // correctamente.
        // Lo mantenemos por seguridad o lo eliminamos. Lo eliminamos para usar la
        // lógica unificada.

        // Calcular dimensiones del menú
        float anchoMenu = 300f; // Ancho fijo para el menú (más ancho por las cantidades)
        // Adjust menu height logic

        float altoMenu = itemH * (pokebolasDisponibles.size() + 1) + paddingY * 2; // Alto basado en cantidad de items +
                                                                                   // título

        // Dibujar cuadro blanco de fondo
        game.batch.setColor(Color.WHITE);
        game.batch.draw(texturaBlanca, menuX - paddingX, menuY - paddingY, anchoMenu, altoMenu);

        // Dibujar borde negro
        float grosorBorde = 3f;
        game.batch.setColor(Color.BLACK);
        // Borde superior
        game.batch.draw(texturaBlanca, menuX - paddingX, menuY - paddingY + altoMenu - grosorBorde, anchoMenu,
                grosorBorde);
        // Borde inferior
        game.batch.draw(texturaBlanca, menuX - paddingX, menuY - paddingY, anchoMenu, grosorBorde);
        // Borde izquierdo
        game.batch.draw(texturaBlanca, menuX - paddingX, menuY - paddingY, grosorBorde, altoMenu);
        // Borde derecho
        game.batch.draw(texturaBlanca, menuX - paddingX + anchoMenu - grosorBorde, menuY - paddingY, grosorBorde,
                altoMenu);
        game.batch.setColor(Color.WHITE); // Resetear color

        // Dibujar texto del menú
        fontPequeña.setColor(Color.BLACK);
        fontPequeña.draw(game.batch, "Selecciona una pokebola:", menuX,
                menuY + itemH * (pokebolasDisponibles.size() + 1));

        HashMap<String, Integer> inventario = game.getJugador().getInventario().getMapa();
        for (int i = 0; i < pokebolasDisponibles.size(); i++) {
            String pokebola = pokebolasDisponibles.get(i);
            String textoItem;

            if (pokebola.equals("Salir")) {
                textoItem = "Salir";
            } else {
                int cantidad = inventario.getOrDefault(pokebola, 0);
                textoItem = pokebola + " x" + cantidad;
            }

            Color color = (i == pokebolaSeleccionada) ? new Color(1f, 0.84f, 0f, 1f) : Color.BLACK;
            fontPequeña.setColor(color);
            fontPequeña.draw(game.batch, (i == pokebolaSeleccionada ? "> " : "  ") + textoItem, menuX,
                    menuY + itemH * (pokebolasDisponibles.size() - i));
        }

        // Mostrar probabilidad de captura si está seleccionada una Pokeball y el rival
        // está vivo
        if (pokebolaSeleccionada < pokebolasDisponibles.size()) {
            String pokebolaSeleccionadaStr = pokebolasDisponibles.get(pokebolaSeleccionada);
            if (!pokebolaSeleccionadaStr.equals("Salir") && pokemonRival != null && pokemonRival.estaVivo()
                    && pokebolaSeleccionadaStr.equals("Pokeball") && !esBatallaEntrenador) {
                try {
                    CapturaPokemon captura = game.getJugador().getSistemaCaptura();
                    double probabilidad = captura.calcularProbabilidadCaptura(pokemonRival, pokebolaSeleccionadaStr);
                    String probTexto = "Probabilidad de captura: " + String.format("%.1f%%", probabilidad * 100);

                    // Mostrar debajo del menú
                    fontPequeña.setColor(Color.CYAN);
                    fontPequeña.draw(game.batch, probTexto, menuX, menuY - 30);
                } catch (Exception e) {
                    // Ignorar errores al calcular probabilidad
                }
            }
        }

        fontPequeña.setColor(Color.WHITE); // Resetear para otros usos
    }

    /**
     * Dibuja el submenú de selección de equipo Pokemon.
     */
    private void dibujarMenuEquipo() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        // Fondo oscuro semi-transparente para resaltar el equipo
        game.batch.setColor(0, 0, 0, 0.8f);
        game.batch.draw(texturaBlanca, 0, 0, sw, sh);
        game.batch.setColor(Color.WHITE);

        List<Pokemon> team = game.getJugador().getEquipo().getPokemons();
        int numPokemons = team.size();

        // Dibujar 6 slots verticalmente a la derecha
        float slotW = 240, slotH = 85;
        float startX = sw - slotW - 30;
        float startY = sh - 110;
        float spacingY = 5;

        for (int i = 0; i < 6; i++) {
            float x = startX;
            float y = startY - i * (slotH + spacingY);

            boolean isSelected = (equipoSeleccionado == i);

            if (i < numPokemons) {
                Pokemon p = team.get(i);

                String nombreNorm = p.getNombre().toLowerCase()
                        .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                        .replace("ñ", "n");

                Texture marco = isSelected ? marcosPokemon.get(nombreNorm) : marcosPokemonVacio.get(nombreNorm);
                if (marco == null)
                    marco = marcoGenericoVacio;

                // El usuario dice que la imagen del pokemon ya está en el propio marco/png
                game.batch.draw(marco, x, y, slotW, slotH);

                // Reset de color tras dibujar el marco si estaba oscurecido
                // game.batch.setColor(Color.WHITE);

                // Vida en texto (ajustada a la posición del nuevo marco)
                fontPequeña.setColor(p.estaVivo() ? Color.WHITE : Color.RED);
                fontPequeña.draw(game.batch, "HP: " + p.getVida() + "/" + p.getVidaMaxima(), x + slotW * 0.45f,
                        y + slotH * 0.4f);
            } else {
                // Slot vacío genérico
                game.batch.draw(marcoGenericoVacio, x, y, slotW, slotH);
            }
        }

        // Botón Salir (opción 6)
        boolean salirSelected = (equipoSeleccionado == 6);
        Texture texS = salirSelected ? texSalirC : texSalir;
        float salirSizeW = 200, salirSizeH = 60;
        game.batch.draw(texS, (sw - salirSizeW) / 2f, 20, salirSizeW, salirSizeH);

        font.getData().setScale(1.5f); // Reset
    }

    /**
     * Dibuja un botón en la interfaz.
     * 
     * @param inactivo Textura del botón en estado inactivo.
     * @param activo   Textura del botón en estado activo.
     * @param id       ID de la opción para verificar selección.
     * @param x        Posición X.
     * @param y        Posición Y.
     * @param w        Ancho.
     * @param h        Alto.
     */
    private void drawBtn(Texture inactivo, Texture activo, int id, float x, float y, float w, float h) {
        if (opcionSeleccionada == id) {
            game.batch.draw(activo, x, y, w, h);
        } else {
            game.batch.draw(inactivo, x, y, w, h);
        }
    }

    /**
     * Dibuja un mensaje con un cuadro blanco de fondo.
     */
    private void dibujarMensajeConFondo(String mensaje) {
        if (mensaje == null || mensaje.isEmpty()) {
            return;
        }

        // Calcular dimensiones del texto
        layout.setText(font, mensaje);

        float anchoTexto = layout.width;
        float altoTexto = layout.height;

        // Padding alrededor del texto
        float paddingX = 20f;
        float paddingY = 15f;
        float paddingInferior = 5f; // Menos padding abajo para ajuste visual

        // Dimensiones del cuadro
        float anchoCuadro = anchoTexto + (paddingX * 2);
        float altoCuadro = altoTexto + paddingY + paddingInferior;

        // Posición del cuadro (esquina inferior izquierda)
        float xCuadro = 30f;
        float yCuadro = Gdx.graphics.getHeight() - 80f - altoCuadro;

        // Dibujar cuadro blanco de fondo
        game.batch.setColor(Color.WHITE);
        game.batch.draw(texturaBlanca, xCuadro, yCuadro, anchoCuadro, altoCuadro);
        game.batch.setColor(Color.WHITE); // Resetear color para otros elementos

        // Dibujar borde negro alrededor del cuadro (opcional, para mejor visibilidad)
        float grosorBorde = 3f;
        game.batch.setColor(Color.BLACK);
        // Borde superior
        game.batch.draw(texturaBlanca, xCuadro, yCuadro + altoCuadro - grosorBorde, anchoCuadro, grosorBorde);
        // Borde inferior
        game.batch.draw(texturaBlanca, xCuadro, yCuadro, anchoCuadro, grosorBorde);
        // Borde izquierdo
        game.batch.draw(texturaBlanca, xCuadro, yCuadro, grosorBorde, altoCuadro);
        // Borde derecho
        game.batch.draw(texturaBlanca, xCuadro + anchoCuadro - grosorBorde, yCuadro, grosorBorde, altoCuadro);
        game.batch.setColor(Color.WHITE); // Resetear color

        // Dibujar texto en negro para mejor contraste sobre fondo blanco
        font.setColor(Color.BLACK);
        float xTexto = xCuadro + paddingX;
        float yTexto = yCuadro + altoTexto + paddingInferior;
        font.draw(game.batch, mensaje, xTexto, yTexto);
        font.setColor(Color.WHITE); // Restaurar color blanco para otros usos
    }

    /**
     * Dibuja un cuadro blanco con el nombre y PS de un Pokémon.
     * Versión simplificada para los nombres de Pokémon en batalla.
     */
    private void dibujarCuadroTextoPokemon(String texto, float x, float y) {
        if (texto == null || texto.isEmpty()) {
            return;
        }

        // Calcular dimensiones del texto
        layout.setText(fontPequeña, texto);

        float anchoTexto = layout.width;
        float altoTexto = layout.height;

        // Padding alrededor del texto
        float paddingX = 10f;
        float paddingY = 8f;

        // Dimensiones del cuadro
        float anchoCuadro = anchoTexto + (paddingX * 2);
        float altoCuadro = altoTexto + (paddingY * 2);

        // Dibujar cuadro blanco de fondo
        game.batch.setColor(Color.WHITE);
        game.batch.draw(texturaBlanca, x, y - altoCuadro, anchoCuadro, altoCuadro);

        // Dibujar borde negro
        float grosorBorde = 2f;
        game.batch.setColor(Color.BLACK);
        // Borde superior
        game.batch.draw(texturaBlanca, x, y - altoCuadro + altoCuadro - grosorBorde, anchoCuadro, grosorBorde);
        // Borde inferior
        game.batch.draw(texturaBlanca, x, y - altoCuadro, anchoCuadro, grosorBorde);
        // Borde izquierdo
        game.batch.draw(texturaBlanca, x, y - altoCuadro, grosorBorde, altoCuadro);
        // Borde derecho
        game.batch.draw(texturaBlanca, x + anchoCuadro - grosorBorde, y - altoCuadro, grosorBorde, altoCuadro);
        game.batch.setColor(Color.WHITE); // Resetear color

        // Dibujar texto en negro
        fontPequeña.setColor(Color.BLACK);
        float xTexto = x + paddingX;
        float yTexto = y - paddingY;
        fontPequeña.draw(game.batch, texto, xTexto, yTexto);
        fontPequeña.setColor(Color.WHITE); // Restaurar color blanco
    }

    private void regresarAlMapa() {
        String nombreRival = pokemonRival.getNombre();
        // Lógica especial para Bosses
        if (nombreRival.equalsIgnoreCase("Dracórnea") || nombreRival.equalsIgnoreCase("Dracornea")) {
            GestorMusica.reproducirMusica(GestorMusica.TipoMusica.MAPA_VERDE);
            game.setScreen(new com.Proyecto.Pokemon.gui.Mapa(game, "Tiled/MapaVerdePokemon.tmx", "abajo3"));
        } else if (nombreRival.equalsIgnoreCase("Aethergon")) {
            // Asumimos que Aethergon está en MapaAzul y la música corresponde
            // Nota: GestorMusica podría no tener MAPA_AZUL, usamos VERDE o MENU por defecto
            // si no
            GestorMusica.reproducirMusica(GestorMusica.TipoMusica.MAPA_VERDE);
            game.setScreen(new com.Proyecto.Pokemon.gui.Mapa(game, "Tiled/MapaAzulPokemon.tmx", "abajo"));
        } else {
            // Comportamiento normal
            GestorMusica.reproducirMusica(GestorMusica.TipoMusica.MAPA_VERDE);
            game.setScreen(pantallaAnterior);
        }
        dispose();
    }

    private void manejarInput() {
        switch (estadoActual) {
            case MENU_PRINCIPAL:
                manejarInputMenuPrincipal();
                break;
            case MENU_ATAQUES:
                manejarInputMenuAtaques();
                break;
            case MENU_MOCHILA:
                manejarInputMenuMochila();
                break;
            case MENU_EQUIPO:
                manejarInputMenuEquipo();
                break;
            case MENSAJE_DERROTA:
                if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                    // Iniciar transición a pantalla negra
                    procesandoDerrotaJugador = true;
                    tiempoPantallaNegra = 0f;
                }
                break;
            case MENSAJE_VICTORIA:
                if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                    // Salir de la batalla (victoria normal)
                    regresarAlMapa();
                }
                break;
            case MENSAJE_CAPTURA:
                if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                    // Salir de la batalla (captura exitosa)
                    regresarAlMapa();
                }
                break;
        }
    }

    private void manejarInputMenuPrincipal() {
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
            ejecutarAccionMenuPrincipal();
        }
    }

    private void manejarInputMenuAtaques() {
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            ataqueSeleccionado = (ataqueSeleccionado - 1 + ataquesDisponibles.size()) % ataquesDisponibles.size();
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            ataqueSeleccionado = (ataqueSeleccionado + 1) % ataquesDisponibles.size();
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            String seleccion = ataquesDisponibles.get(ataqueSeleccionado);
            if (seleccion.equals("Salir")) {
                estadoActual = EstadoMenu.MENU_PRINCIPAL;
            } else {
                ejecutarAtaque();
            }
        }
        // ESCAPE deshabilitado para ataques
    }

    private void manejarInputMenuMochila() {
        // La lista nunca estará vacía porque contiene "Salir"

        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            pokebolaSeleccionada = (pokebolaSeleccionada - 1 + pokebolasDisponibles.size())
                    % pokebolasDisponibles.size();
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            pokebolaSeleccionada = (pokebolaSeleccionada + 1) % pokebolasDisponibles.size();
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            String seleccion = pokebolasDisponibles.get(pokebolaSeleccionada);
            if (seleccion.equals("Salir")) {
                estadoActual = EstadoMenu.MENU_PRINCIPAL;
            } else {
                usarPokebola();
            }
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
        }
    }

    private void manejarInputMenuEquipo() {
        List<Pokemon> pokemons = game.getJugador().getEquipo().getPokemons();
        int numPokemons = pokemons.size();

        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            do {
                equipoSeleccionado = (equipoSeleccionado - 1 + 7) % 7;
            } while (equipoSeleccionado < 6 && equipoSeleccionado >= numPokemons);
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            do {
                equipoSeleccionado = (equipoSeleccionado + 1) % 7;
            } while (equipoSeleccionado < 6 && equipoSeleccionado >= numPokemons);
        }
        // Quitamos LEFT/RIGHT ya que ahora es una lista vertical

        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            if (equipoSeleccionado == 6) {
                estadoActual = EstadoMenu.MENU_PRINCIPAL;
            } else if (equipoSeleccionado < numPokemons) {
                Pokemon p = pokemons.get(equipoSeleccionado);

                // Verificación estricta por referencia (identidad de objeto)
                boolean esMismoPokemon = (p == pokemonJugador);

                if (esMismoPokemon) {
                    mensajeLog = "¡" + p.getNombre() + " ya está en batalla!";
                    // Mantenemos al usuario en el menú de equipo
                } else if (!p.estaVivo()) {
                    mensajeLog = "¡" + p.getNombre() + " no tiene energía!";
                } else {
                    cambiarPokemonJugador(p);
                    // Cambiar de Pokemon consume el turno SOLO SI NO ES FORZADO
                    if (!cambioForzado && batalla.getAtacante() == pokemonJugador) {
                        batalla.consumirTurno();
                    }
                    // Resetear flag
                    cambioForzado = false;

                    estadoActual = EstadoMenu.MENU_PRINCIPAL;
                    mensajeLog = "¡Adelante " + p.getNombre() + "!";
                }
            }
        }
        // ESCAPE deshabilitado para equipo
    }

    private void ejecutarAccionMenuPrincipal() {
        switch (opcionSeleccionada) {
            case 0: // PELEA
                if (!batalla.estaTerminada() && batalla.getAtacante() == pokemonJugador) {
                    estadoActual = EstadoMenu.MENU_ATAQUES;
                } else if (batalla.estaTerminada()) {
                    mensajeLog = "La batalla ha terminado.\nPulsa Huir para salir.";
                } else if (batalla.getAtacante() == pokemonRival) {
                    // Si es el turno del rival, atacar automáticamente
                    realizarAtaqueRival();
                } else {
                    mensajeLog = "Espera tu turno...";
                }
                break;
            case 1: // MOCHILA
                inicializarPokebolasDisponibles();
                estadoActual = EstadoMenu.MENU_MOCHILA;
                break;
            case 2: // EQUIPO - Ver Pokemon capturados
                equipoSeleccionado = 0;
                estadoActual = EstadoMenu.MENU_EQUIPO;
                mensajeLog = ""; // Limpiar mensaje al entrar
                break;
            case 3: // HUIR
                huirDeBatalla();
                break;
        }
    }

    private void ejecutarAtaque() {
        if (batalla.estaTerminada()) {
            mensajeLog = "La batalla ha terminado.";
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
            return;
        }

        // Solo atacar si es tu turno
        if (batalla.getAtacante() != pokemonJugador) {
            mensajeLog = "Espera tu turno...";
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
            return;
        }

        String nombreAtaque = ataquesDisponibles.get(ataqueSeleccionado);

        // Iniciar animación del jugador atacando
        animandoAtaque = true;
        animandoAtaqueJugador = true;
        tiempoAnimacion = 0f;
        frameAnimacion = 0;

        mensajeLog = batalla.realizarAtaque(nombreAtaque);

        // Verificar si la batalla terminó después de tu ataque
        if (batalla.estaTerminada()) {
            Pokemon ganador = batalla.obtenerGanador();
            if (ganador == pokemonJugador) {
                // Victoria del Jugador
                verificarVictoriaLegendario(); // Check side-effects for legends

                // Mensaje General de Victoria
                if (!mensajeLog.contains("FELICIDADES")) {
                    mensajeLog = "Has ganado la pelea felicidades";
                }

                sumarPuntosInvestigacion(pokemonRival.getNombre(), 1);
                aplicarRecompensaVictoria(esBatallaEntrenador);

                mensajeLog += "\n(ENTER)";
                estadoActual = EstadoMenu.MENSAJE_VICTORIA;
            }
        } else {
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
            // El rival atacará automáticamente en el próximo frame si le toca su turno
        }
    }

    private void usarPokebola() {
        if (pokebolasDisponibles.isEmpty()) {
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
            mensajeLog = "No tienes pokebolas disponibles.";
            return;
        }

        if (batalla.estaTerminada()) {
            mensajeLog = "La batalla ya ha terminado.";
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
            return;
        }

        String tipoPokebola = pokebolasDisponibles.get(pokebolaSeleccionada);

        // --- LÓGICA POR TIPO DE ITEM ---
        if (tipoPokebola.equals("PokeballCura")) {
            // Curar
            try {
                game.getJugador().getInventario().eliminarObjeto("PokeballCura", 1);
                pokemonJugador.curar(40);
                mensajeLog = "Usaste PokeballCura.\n" + pokemonJugador.getNombre() + " recupero 40 PS.";

                // Consumir turno
                if (batalla.getAtacante() == pokemonJugador) {
                    batalla.consumirTurno();
                }
                // Rival ataca
                verificarContraataqueRival();

                estadoActual = EstadoMenu.MENU_PRINCIPAL;
            } catch (Exception e) {
                mensajeLog = "Error al usar item: " + e.getMessage();
            }

        } else if (tipoPokebola.equals("PokeballEXP")) {
            // Subir Nivel
            try {
                game.getJugador().getInventario().eliminarObjeto("PokeballEXP", 1);
                pokemonJugador.subirNivel();
                mensajeLog = "Usaste PokeballEXP.\n¡" + pokemonJugador.getNombre() + " subio de nivel!";

                // Consumir turno
                if (batalla.getAtacante() == pokemonJugador) {
                    batalla.consumirTurno();
                }
                // Rival ataca
                verificarContraataqueRival();

                estadoActual = EstadoMenu.MENU_PRINCIPAL;
            } catch (Exception e) {
                mensajeLog = "Error al usar item: " + e.getMessage();
            }

        } else if (tipoPokebola.equals("Pokeball")) {
            // Capturar (Lógica antigua)
            if (esBatallaEntrenador) {
                mensajeLog = "No puedes robar los Pokemon\nde otro entrenador!";
                estadoActual = EstadoMenu.MENU_PRINCIPAL;
                return;
            }
            if (!pokemonRival.estaVivo()) {
                mensajeLog = "No puedes capturar un Pokemon derrotado.";
                estadoActual = EstadoMenu.MENU_PRINCIPAL;
                return;
            }

            CapturaPokemon captura = game.getJugador().getSistemaCaptura();
            try {
                boolean capturado = captura.intentarCapturar(pokemonRival, tipoPokebola);
                if (capturado) {
                    mensajeLog = "Has atrapado un pokemon salvaje!";
                    sumarPuntosInvestigacion(pokemonRival.getNombre(), 2);
                    aplicarRecompensaVictoria(esBatallaEntrenador);
                    mensajeLog += "\n(Pulsa ENTER)";
                    estadoActual = EstadoMenu.MENSAJE_CAPTURA;
                    return;
                } else {
                    mensajeLog = "La Pokeball fallo.\n" + pokemonRival.getNombre() + " escapo.";
                    if (batalla.getAtacante() == pokemonJugador) {
                        batalla.consumirTurno();
                    }
                    verificarContraataqueRival();
                    estadoActual = EstadoMenu.MENU_PRINCIPAL;
                }
            } catch (ExcepcionPokebolaInsuficiente e) {
                mensajeLog = e.getMessage();
            } catch (ExcepcionEquipoLleno e) {
                mensajeLog = e.getMessage();
            }
        }
    }

    private void verificarContraataqueRival() {
        if (!batalla.estaTerminada() && pokemonRival.estaVivo() && pokemonJugador.estaVivo()) {
            if (batalla.getAtacante() == pokemonRival) {
                // El ataque real se procesa en el update() con delay (verificar espera ataque),
                // pero si queremos forzarlo inmediato o marcarlo:
                // Dejamos que el update() lo maneje.
            }
        }
    }

    /**
     * Método público para cambiar el pokemon del jugador durante la batalla.
     */
    public void cambiarPokemonJugador(Pokemon nuevoPokemon) {
        pokemonJugador = nuevoPokemon;
        batalla.cambiarPokemonJugador(nuevoPokemon);
        inicializarAtaquesDisponibles();

        // Recargar sprite del pokemon usando GestorSpritesPokemon
        if (spriteJugador != null && gestorSprites != null) {
            // El gestor maneja la liberación de recursos
            spriteJugador = gestorSprites.obtenerSpriteAtras(nuevoPokemon.getNombre(), 0);
            if (spriteJugador == null) {
                // Fallback si el gestor falla
                com.badlogic.gdx.graphics.Pixmap p = new com.badlogic.gdx.graphics.Pixmap(32, 32,
                        com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
                p.setColor(Color.MAGENTA);
                p.fill();
                spriteJugador = new Texture(p);
            }
        }

        mensajeLog = "¡Has cambiado a " + nuevoPokemon.getNombre() + "!";
    }

    /**
     * Verifica si se derrotó un Pokemon legendario y reproduce la música de ganador
     * si ambos han sido derrotados.
     */
    private void verificarVictoriaLegendario() {
        // Verificar si el rival era un Pokemon legendario (Dracornea o Aethergon)
        if (pokemonRival instanceof PokeDragon) {
            PokeDragon dragon = (PokeDragon) pokemonRival;
            String nombre = dragon.getNombre();

            // Si es Dracornea o Aethergon, son los pokémons legendarios
            if (nombre.equals("Dracórnea") || nombre.equals("Dracornea") ||
                    nombre.equals("Aethergon")) {
                // Marcar el Pokemon legendario como derrotado
                game.marcarLegendarioDerrotado(nombre);

                // Si ambos pokémons legendarios han sido derrotados, reproducir música de
                // ganador
                if (game.esJuegoCompleto()) {
                    GestorMusica.reproducirMusica(GestorMusica.TipoMusica.GANADOR);
                    System.out.println("¡HAS COMPLETADO EL JUEGO! ¡Has derrotado a ambos pokémons legendarios!");
                    mensajeLog = "¡FELICIDADES!\nHas derrotado a ambos\npokémons legendarios!\n¡HAS COMPLETADO EL JUEGO!";
                } else {
                    // Solo se derrotó uno de los dos
                    System.out
                            .println("¡Has derrotado a " + nombre + "! Te falta derrotar al otro pokémon legendario.");
                    if (nombre.equals("Dracórnea") || nombre.equals("Dracornea")) {
                        mensajeLog = "¡Has derrotado a Dracornea!\nTe falta derrotar a Aethergon\npara completar el juego.";
                    } else {
                        mensajeLog = "¡Has derrotado a Aethergon!\nTe falta derrotar a Dracornea\npara completar el juego.";
                    }
                }
            }
        }
    }

    private void huirDeBatalla() {
        // Guardar la vida actual del Pokémon del jugador antes de huir
        // El pokemonJugador es una referencia directa al Pokémon en la lista del
        // jugador,
        // por lo que los cambios en su vida ya están guardados automáticamente.
        // Sin embargo, sincronizamos la vida en la lista por si acaso cambió de
        // Pokémon.

        // Sincronizar la vida del Pokémon en el equipo del jugador
        List<Pokemon> pokemonsEquipo = game.getJugador().getEquipo().getPokemons();
        int vidaActual = pokemonJugador.getVida();

        // Buscar el Pokémon en el equipo y sincronizar su vida
        for (Pokemon p : pokemonsEquipo) {
            // Si encontramos el mismo Pokemon (mismo nombre y sexo), sincronizar su vida
            if (p == pokemonJugador) {
                // Es la misma referencia, la vida ya está sincronizada automáticamente
                break;
            } else if (p.getNombre().equals(pokemonJugador.getNombre()) &&
                    p.getSexo().equals(pokemonJugador.getSexo())) {
                // Es el mismo Pokémon pero diferente referencia, sincronizar su vida
                // Curar primero al máximo, luego aplicar el daño correspondiente
                p.curar();
                int danioAplicar = p.getVidaMaxima() - vidaActual;
                if (danioAplicar > 0) {
                    p.recibirDanio(danioAplicar);
                }
                break;
            }
        }

        System.out.println("Has huido de la batalla. Vida actual de " + pokemonJugador.getNombre() +
                ": " + pokemonJugador.getVida() + "/" + pokemonJugador.getVidaMaxima());

        // Volver a la música del mapa anterior
        // La música se restaurará cuando se muestre el mapa
        regresarAlMapa();
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

        // Liberar recursos del gestor de sprites (maneja los sprites principales)
        if (gestorSprites != null) {
            gestorSprites.dispose();
        }
        // Los fallbacks se crean como texturas nuevas y deben ser liberados manualmente
        // pero por simplicidad, el Gestor maneja los principales
        if (spriteJugador != null && !gestorSprites.texturasPokemonContains(spriteJugador)) {
            spriteJugador.dispose();
        }
        if (spriteRival != null && (pokemonRival.getNombre().equals("MissingNo")
                || !gestorSprites.texturasPokemonContains(spriteRival))) {
            spriteRival.dispose();
        }

        font.dispose();
        fontPequeña.dispose();

        // Liberar marcos 8-bit
        if (marcoGenericoVacio != null)
            marcoGenericoVacio.dispose();
        if (texSalir != null)
            texSalir.dispose();
        if (texSalirC != null)
            texSalirC.dispose();
        if (marcosPokemon != null) {
            for (Texture t : marcosPokemon.values())
                t.dispose();
            for (Texture t : marcosPokemonVacio.values())
                t.dispose();
        }

        if (texturaBlanca != null) {
            texturaBlanca.dispose();
        }
    }

    private void sumarPuntosInvestigacion(String nombre, int puntos) {
        // Normalizar nombre para evitar duplicados por mayusculas/minusculas si fuera
        // necesario
        // Pero usamos el nombre directo del pokemon por ahora
        game.getJugador().agregarPuntosInvestigacion(nombre, puntos);
        System.out.println("Investigacion " + nombre + " +" + puntos);
    }

    private void aplicarRecompensaVictoria(boolean esEntrenador) {
        String item = "";
        String tipo = "";

        List<String> opciones;
        if (esEntrenador) {
            opciones = java.util.Arrays.asList("Pokeball", "PokeballCura", "PokeballEXP");
            tipo = "pokeball";
        } else {
            opciones = java.util.Arrays.asList("Plastico", "Goma", "Madera");
            tipo = "item";
        }

        item = opciones.get(com.badlogic.gdx.math.MathUtils.random(opciones.size() - 1));

        try {
            game.getJugador().getInventario().agregarObjeto(item, tipo);
            mensajeLog += "\n¡Obtienes 1 " + item + "!";
        } catch (com.Proyecto.Pokemon.excepciones.ExcepcionInventarioLleno e) {
            mensajeLog += "\nObtuviste " + item + " pero tu inventario esta lleno.";
        }
    }

    private void aplicarPenalizacionDerrota(boolean esEntrenador) {
        HashMap<String, Integer> inv = game.getJugador().getInventario().getMapa();
        List<String> candidatos = new ArrayList<>();

        if (esEntrenador) {
            if (inv.containsKey("Pokeball") && inv.get("Pokeball") > 0)
                candidatos.add("Pokeball");
            if (inv.containsKey("PokeballCura") && inv.get("PokeballCura") > 0)
                candidatos.add("PokeballCura");
            if (inv.containsKey("PokeballEXP") && inv.get("PokeballEXP") > 0)
                candidatos.add("PokeballEXP");
        } else {
            if (inv.containsKey("Plastico") && inv.get("Plastico") > 0)
                candidatos.add("Plastico");
            if (inv.containsKey("Goma") && inv.get("Goma") > 0)
                candidatos.add("Goma");
            if (inv.containsKey("Madera") && inv.get("Madera") > 0)
                candidatos.add("Madera");
        }

        if (!candidatos.isEmpty()) {
            String itemPerdido = candidatos.get(com.badlogic.gdx.math.MathUtils.random(candidatos.size() - 1));
            int cantidad = inv.get(itemPerdido);
            inv.put(itemPerdido, cantidad - 1);
            mensajeLog += "\n¡Perdiste 1 " + itemPerdido + "!";
        }
    }
}
