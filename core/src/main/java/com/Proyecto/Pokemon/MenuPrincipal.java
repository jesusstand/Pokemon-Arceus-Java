package com.Proyecto.Pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;

/**
 * Representa la pantalla principal del menú del juego.
 * Gestiona la carga de texturas, la navegación entre opciones (Partida
 * Individual, Multiplayer, Opciones, Salir),
 * y aplica un diseño responsivo basado en coordenadas relativas y un efecto de
 * escalado visual al seleccionar.
 */
public class MenuPrincipal implements Screen {

    private Main game;
    // Declaración de todas las texturas (imágenes) necesarias.
    private Texture fondoMenu;
    private Texture partidaIndividual, partidaIndividualC, salirMenu, salirMenuC, multiplayer, multiplayerC,
            opcionesMenu, opcionesMenuC;

    // --- CONSTANTES DEL ESTADO DEL MENU (Indices de las opciones) ---
    // Usamos constantes para hacer el codigo mas legible y evitar 'numeros
    // magicos'.
    private static final int PARTIDA_INDIVIDUAL = 0;
    private static final int MULTIPLAYER = 1;
    private static final int OPCIONES_MENU = 2;
    private static final int SALIR = 3;
    private static final int OPCIONES = 4; // Total de 4 opciones

    private int opcionSeleccionada; // Variable de estado que guarda el índice actual seleccionado (0 a 3).

    // --- CONSTANTES DE POSICIÓN RELATIVA (RESPONSIVAS) ---
    // Definen la posición X de los botones como un porcentaje del ancho total de la
    // pantalla.
    private static final float BUTTON_X_POS = 0.08f; // 8% desde el borde izquierdo.

    // Definen la posición Y de los botones como un porcentaje del alto total de la
    // pantalla.
    // Esto asegura que se apilen de forma uniforme y el diseño no se desconfigure.
    private static final float BUTTON_Y_PARTIDA_INDIVIDUAL = 0.70f; // Arriba (70% de la altura)
    private static final float BUTTON_Y_MULTIPLAYER = 0.55f; // Segundo
    private static final float BUTTON_Y_OPCIONES = 0.40f; // Tercero
    private static final float BUTTON_Y_SALIR = 0.25f; // Abajo (25% de la altura)

    // --- CONSTANTES DE TAMAÑO RELATIVO (RESPONSIVAS) ---
    // Definen el tamaño base de los botones como un factor del tamaño de la
    // pantalla.
    private static final float BUTTON_WIDTH_FACTOR = 0.35f; // 35% del ancho de la pantalla.
    private static final float BUTTON_HEIGHT_FACTOR = 0.10f; // 10% del alto de la pantalla.

    /**
     * Constructor de la pantalla del menú. Se encarga de inicializar la referencia
     * al juego
     * y cargar todos los recursos gráficos (texturas).
     *
     * @param game La instancia principal del juego.
     */
    public MenuPrincipal(Main game) {
        this.game = game;

        // --- CARGA DE RECURSOS ---
        // Gdx.files.internal busca el archivo en la carpeta 'assets'.
        fondoMenu = new Texture(Gdx.files.internal("arceus.jpg"));
        partidaIndividual = new Texture(Gdx.files.internal("PartidaIndividual.png"));
        partidaIndividualC = new Texture(Gdx.files.internal("PartidaIndividualC.png"));
        salirMenu = new Texture(Gdx.files.internal("Salir.png"));
        salirMenuC = new Texture(Gdx.files.internal("SalirC.png"));
        multiplayer = new Texture(Gdx.files.internal("Multiplayer.png"));
        multiplayerC = new Texture(Gdx.files.internal("MultiplayerC.png"));
        opcionesMenu = new Texture(Gdx.files.internal("Opciones.png"));
        opcionesMenuC = new Texture(Gdx.files.internal("OpcionesC.png"));

        // Establecer la opción inicial al cargar el menú.
        opcionSeleccionada = PARTIDA_INDIVIDUAL;
    }

    /**
     * Método render: El bucle principal del juego, se llama continuamente.
     * Contiene la lógica de selección, el escalado visual y el dibujo.
     *
     * @param delta El tiempo transcurrido desde el último frame (útil para
     *              animaciones).
     */
    @Override
    public void render(float delta) {

        // CALCULO DE TAMANOS BASE RELATIVOS
        // Estos tamanos se ajustan automaticamente al cambiar el tamano de la ventana.
        float baseWidth = Gdx.graphics.getWidth() * BUTTON_WIDTH_FACTOR;
        float baseHeight = Gdx.graphics.getHeight() * BUTTON_HEIGHT_FACTOR;

        float scaleFactor = 1.1f; // Factor para aumentar el tamaño del botón seleccionado (10% más grande).

        // -----------------------------------------------------------
        // 1. LÓGICA DE SELECCIÓN Y DISEÑO (Asignación de texturas y escalado)
        // -----------------------------------------------------------

        // Inicialización de texturas (estado por defecto: deseleccionado)
        Texture texturaBotonPartidaIndividual = partidaIndividual;
        Texture texturaBotonMultiplayer = multiplayer;
        Texture texturaBotonOpciones = opcionesMenu;
        Texture texturaBotonSalir = salirMenu;

        // Inicialización de dimensiones (estado por defecto: tamaño base)
        float anchoPartidaIndividual = baseWidth;
        float alturaPartidaIndividual = baseHeight;
        float anchoMultiplayer = baseWidth;
        float alturaMultiplayer = baseHeight;
        float anchoOpciones = baseWidth;
        float alturaOpciones = baseHeight;
        float anchoSalir = baseWidth;
        float alturaSalir = baseHeight;

        // Estructura UNIFICADA DE SELECCIÓN: solo se modifica la textura y el tamaño
        // del botón que coincide con la opcionSeleccionada.
        if (opcionSeleccionada == PARTIDA_INDIVIDUAL) {
            texturaBotonPartidaIndividual = partidaIndividualC;
            anchoPartidaIndividual *= scaleFactor;
            alturaPartidaIndividual *= scaleFactor;
        } else if (opcionSeleccionada == MULTIPLAYER) {
            texturaBotonMultiplayer = multiplayerC;
            anchoMultiplayer *= scaleFactor;
            alturaMultiplayer *= scaleFactor;
        } else if (opcionSeleccionada == OPCIONES_MENU) {
            texturaBotonOpciones = opcionesMenuC;
            anchoOpciones *= scaleFactor;
            alturaOpciones *= scaleFactor;
        } else if (opcionSeleccionada == SALIR) {
            texturaBotonSalir = salirMenuC;
            anchoSalir *= scaleFactor;
            alturaSalir *= scaleFactor;
        }

        // --- LIMPIEZA DE PANTALLA ---
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- LÓGICA DE ENTRADA Y NAVEGACIÓN ---

        // Navegación hacia arriba (circula entre 0, 3, 2, 1)
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            opcionSeleccionada = (opcionSeleccionada - 1 + OPCIONES) % OPCIONES;
        }
        // Navegación hacia abajo (circula entre 0, 1, 2, 3)
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            opcionSeleccionada = (opcionSeleccionada + 1) % OPCIONES;
        }
        // Acción al pulsar ESPACIO/ENTER
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            switch (opcionSeleccionada) {
                case PARTIDA_INDIVIDUAL:
                    // Cambiar a la pantalla de inicio de juego
                    game.setScreen(new Mapa(game, "MapaVerdePokemon.tmx"));
                    dispose(); // Es crucial liberar los recursos del menú
                    break;
                case MULTIPLAYER:
                    Gdx.app.log("MENU", "Seleccionado: MULTIPLAYER");
                    // Aquí iría el cambio a la pantalla de multijugador
                    break;
                case OPCIONES_MENU:
                    Gdx.app.log("MENU", "Seleccionado: OPCIONES");
                    // Aquí iría el cambio a la pantalla de opciones
                    break;
                case SALIR:
                    // Terminar la aplicación de LibGDX
                    Gdx.app.exit();
                    break;
            }
        }

        // -----------------------------------------------------------
        // --- INICIO DEL DIBUJO ---
        // -----------------------------------------------------------
        game.batch.begin();

        // 1. Dibuja el Fondo (Responsivo: ocupa el 100% del ancho y alto)
        // Se aplica un efecto de transparencia (Alpha=0.8) al fondo.
        game.batch.setColor(1f, 1f, 1f, 0.8f);
        game.batch.draw(fondoMenu, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.setColor(1f, 1f, 1f, 1f); // Restablecer opacidad total para los botones.

        // -----------------------------------------------------------
        // 2. DIBUJO DE LOS 4 BOTONES (Usando coordenadas y tamaños relativos)
        // -----------------------------------------------------------

        // Las posiciones se calculan multiplicando el ancho/alto de la pantalla por la
        // constante de porcentaje.

        // 2.1 Dibuja PARTIDA INDIVIDUAL
        game.batch.draw(
                texturaBotonPartidaIndividual,
                Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_PARTIDA_INDIVIDUAL,
                anchoPartidaIndividual,
                alturaPartidaIndividual);

        // 2.2 Dibuja MULTIPLAYER
        game.batch.draw(
                texturaBotonMultiplayer,
                Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_MULTIPLAYER,
                anchoMultiplayer,
                alturaMultiplayer);

        // 2.3 Dibuja OPCIONES
        game.batch.draw(
                texturaBotonOpciones,
                Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_OPCIONES,
                anchoOpciones,
                alturaOpciones);

        // 2.4 Dibuja SALIR
        game.batch.draw(
                texturaBotonSalir,
                Gdx.graphics.getWidth() * BUTTON_X_POS,
                Gdx.graphics.getHeight() * BUTTON_Y_SALIR,
                anchoSalir,
                alturaSalir);

        // --- FINAL DEL DIBUJO ---
        game.batch.end();
    }

    // --- MÉTODOS DE LA INTERFAZ SCREEN (Obligatorios) ---

    // Llamado al crear la pantalla. Los recursos se cargan en el constructor.
    @Override
    public void show() {
    }

    // Llamado cuando la pantalla cambia de tamaño. Es crucial si usamos Viewports.
    @Override
    public void resize(int width, int height) {
    }

    // Llamado cuando el juego está minimizado o pierde el foco.
    @Override
    public void pause() {
    }

    // Llamado cuando el juego recupera el foco.
    @Override
    public void resume() {
    }

    // Llamado justo antes de que se oculte la pantalla.
    @Override
    public void hide() {
    }

    /**
     * Método dispose: CRÍTICO. Se llama justo antes de que la pantalla se destruya.
     * Libera todos los recursos (texturas) de la memoria para evitar fugas.
     */
    @Override
    public void dispose() {
        fondoMenu.dispose();
        partidaIndividual.dispose();
        partidaIndividualC.dispose();
        salirMenu.dispose();
        salirMenuC.dispose();
        multiplayer.dispose();
        multiplayerC.dispose();
        opcionesMenu.dispose();
        opcionesMenuC.dispose();
    }
}
