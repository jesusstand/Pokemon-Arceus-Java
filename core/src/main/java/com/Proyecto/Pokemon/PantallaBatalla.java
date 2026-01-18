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

    // Sprites temporales
    private Texture spriteJugador;
    private Texture spriteRival;

    // Lógica UI
    private int opcionSeleccionada = 0;
    private BitmapFont font;
    private BitmapFont fontPequeña;

    // Log de batalla
    private String mensajeLog = "";

    // Estados del menú
    private enum EstadoMenu {
        MENU_PRINCIPAL,
        MENU_ATAQUES,
        MENU_MOCHILA
    }
    private EstadoMenu estadoActual = EstadoMenu.MENU_PRINCIPAL;
    
    // Menú de ataques
    private List<String> ataquesDisponibles;
    private int ataqueSeleccionado = 0;
    
    // Menú de mochila
    private List<String> pokebolasDisponibles;
    private int pokebolaSeleccionada = 0;

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
        inicializarAtaquesDisponibles();
        inicializarPokebolasDisponibles();
        
        // Reproducir música de batalla
        GestorMusica.reproducirMusica(GestorMusica.TipoMusica.BATALLA);
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
        
        fontPequeña = new BitmapFont();
        fontPequeña.setColor(Color.WHITE);
        fontPequeña.getData().setScale(1.2f);
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
        ataqueSeleccionado = 0;
    }
    
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
        
        pokebolaSeleccionada = 0;
    }

    // Variable para controlar si el rival debe atacar automáticamente
    private boolean esperandoAtaqueRival = false;
    private float tiempoEsperaRival = 0f;
    private static final float DELAY_ATAQUE_RIVAL = 0.5f; // Esperar 0.5 segundos antes del ataque
    
    @Override
    public void render(float delta) {
        manejarInput();
        
        // Si es el turno del rival y está vivo, atacar automáticamente después de un delay
        if (!batalla.estaTerminada() && batalla.getAtacante() == pokemonRival && 
            pokemonRival.estaVivo() && pokemonJugador.estaVivo() && 
            estadoActual == EstadoMenu.MENU_PRINCIPAL && !esperandoAtaqueRival) {
            tiempoEsperaRival += delta;
            if (tiempoEsperaRival >= DELAY_ATAQUE_RIVAL) {
                realizarAtaqueRival();
                tiempoEsperaRival = 0f;
            }
        } else if (batalla.getAtacante() != pokemonRival || batalla.estaTerminada()) {
            // Resetear el timer si ya no es el turno del rival
            tiempoEsperaRival = 0f;
        }

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        // 1. Dibujar Fondo
        game.batch.draw(fondoBatalla, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 2. Dibujar Pokemon
        dibujarEscenaBatalla();

        // 3. Dibujar UI según el estado actual
        switch (estadoActual) {
            case MENU_PRINCIPAL:
                dibujarBotones();
                break;
            case MENU_ATAQUES:
                dibujarMenuAtaques();
                break;
            case MENU_MOCHILA:
                dibujarMenuMochila();
                break;
        }

        // 4. Dibujar Mensajes
        font.draw(game.batch, mensajeLog, 50, Gdx.graphics.getHeight() - 50);

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
        // El rival ataca automáticamente (ataque aleatorio)
        String ataqueRival = batalla.realizarAtaque();
        mensajeLog = ataqueRival;
        
        // Verificar si la batalla terminó después del ataque del rival
        if (batalla.estaTerminada()) {
            Pokemon ganador = batalla.obtenerGanador();
            if (ganador == pokemonRival) {
                mensajeLog += "\n" + pokemonJugador.getNombre() + " ha sido derrotado!";
            }
        }
        
        esperandoAtaqueRival = false;
    }

    private void dibujarEscenaBatalla() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        // Pokemon Jugador (Izquierda - Abajo)
        float xJugador = sw * 0.15f;
        float yJugador = sh * 0.25f;
        float sizeJugador = sh * 0.35f;

        game.batch.draw(spriteJugador, xJugador, yJugador, sizeJugador, sizeJugador);
        font.draw(game.batch, pokemonJugador.getNombre() + " PS: " + pokemonJugador.getVida() + "/" + pokemonJugador.getVidaMaxima(), xJugador, yJugador - 20);

        // Pokemon Rival (Derecha - Arriba)
        float xRival = sw * 0.65f;
        float yRival = sh * 0.55f;
        float sizeRival = sh * 0.25f;

        game.batch.draw(spriteRival, xRival, yRival, sizeRival, sizeRival);
        font.draw(game.batch, pokemonRival.getNombre() + " PS: " + pokemonRival.getVida() + "/" + pokemonRival.getVidaMaxima(), xRival,
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
    
    private void dibujarMenuAtaques() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        
        float menuX = sw * 0.55f;
        float menuY = sh * 0.15f;
        float itemH = 40;
        
        fontPequeña.draw(game.batch, "Selecciona un ataque:", menuX, menuY + itemH * (ataquesDisponibles.size() + 1));
        
        for (int i = 0; i < ataquesDisponibles.size(); i++) {
            String ataque = ataquesDisponibles.get(i);
            Color color = (i == ataqueSeleccionado) ? Color.YELLOW : Color.WHITE;
            fontPequeña.setColor(color);
            fontPequeña.draw(game.batch, (i == ataqueSeleccionado ? "> " : "  ") + ataque, menuX, menuY + itemH * (ataquesDisponibles.size() - i));
        }
        fontPequeña.setColor(Color.WHITE);
    }
    
    private void dibujarMenuMochila() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        
        float menuX = sw * 0.55f;
        float menuY = sh * 0.15f;
        float itemH = 40;
        
        if (pokebolasDisponibles.isEmpty()) {
            fontPequeña.draw(game.batch, "No tienes pokebolas!", menuX, menuY + itemH);
            return;
        }
        
        fontPequeña.draw(game.batch, "Selecciona una pokebola:", menuX, menuY + itemH * (pokebolasDisponibles.size() + 1));
        
        HashMap<String, Integer> inventario = game.getJugador().getInventario().getMapa();
        for (int i = 0; i < pokebolasDisponibles.size(); i++) {
            String pokebola = pokebolasDisponibles.get(i);
            int cantidad = inventario.getOrDefault(pokebola, 0);
            Color color = (i == pokebolaSeleccionada) ? Color.YELLOW : Color.WHITE;
            fontPequeña.setColor(color);
            fontPequeña.draw(game.batch, (i == pokebolaSeleccionada ? "> " : "  ") + pokebola + " x" + cantidad, menuX, menuY + itemH * (pokebolasDisponibles.size() - i));
        }
        fontPequeña.setColor(Color.WHITE);
    }

    private void drawBtn(Texture inactivo, Texture activo, int id, float x, float y, float w, float h) {
        if (opcionSeleccionada == id) {
            game.batch.draw(activo, x, y, w, h);
        } else {
            game.batch.draw(inactivo, x, y, w, h);
        }
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
            ejecutarAtaque();
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
        }
    }
    
    private void manejarInputMenuMochila() {
        if (pokebolasDisponibles.isEmpty()) {
            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                estadoActual = EstadoMenu.MENU_PRINCIPAL;
            }
            return;
        }
        
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            pokebolaSeleccionada = (pokebolaSeleccionada - 1 + pokebolasDisponibles.size()) % pokebolasDisponibles.size();
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            pokebolaSeleccionada = (pokebolaSeleccionada + 1) % pokebolasDisponibles.size();
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            usarPokebola();
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
        }
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
                // Abrir pantalla de Pokemon capturados con callback para cambiar pokemon
                game.setScreen(new PantallaPokemonCapturados(game, this, true));
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
        mensajeLog = batalla.realizarAtaque(nombreAtaque);
        
        // Verificar si la batalla terminó después de tu ataque
        if (batalla.estaTerminada()) {
            Pokemon ganador = batalla.obtenerGanador();
            if (ganador == pokemonJugador) {
                verificarVictoriaLegendario();
            }
        }
        
        estadoActual = EstadoMenu.MENU_PRINCIPAL;
        // El rival atacará automáticamente en el próximo frame si le toca su turno
    }
    
    private void usarPokebola() {
        if (pokebolasDisponibles.isEmpty()) {
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
            mensajeLog = "No tienes pokebolas disponibles.";
            return;
        }
        
        // Solo puedes usar pokebola si es tu turno y el rival está vivo
        if (batalla.estaTerminada()) {
            mensajeLog = "La batalla ya ha terminado.";
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
            return;
        }
        
        if (!pokemonRival.estaVivo()) {
            mensajeLog = "No puedes capturar un Pokemon derrotado.";
            estadoActual = EstadoMenu.MENU_PRINCIPAL;
            return;
        }
        
        String tipoPokebola = pokebolasDisponibles.get(pokebolaSeleccionada);
        CapturaPokemon captura = game.getJugador().getSistemaCaptura();
        
        try {
            boolean capturado = captura.intentarCapturar(pokemonRival, tipoPokebola);
            if (capturado) {
                mensajeLog = "¡" + pokemonRival.getNombre() + " fue capturado!\nLa batalla ha terminado.";
                // Terminar batalla si se capturó exitosamente
                game.setScreen(pantallaAnterior);
                dispose();
                return;
            } else {
                mensajeLog = "La " + tipoPokebola + " falló.\n" + pokemonRival.getNombre() + " escapó.";
                // Consumir el turno del jugador (ya que usó una pokebola)
                if (batalla.getAtacante() == pokemonJugador) {
                    batalla.consumirTurno();
                }
                
                // Después de intentar capturar, el rival ataca automáticamente
                if (!batalla.estaTerminada() && pokemonRival.estaVivo() && pokemonJugador.estaVivo()) {
                    // Ahora es el turno del rival, que ataca
                    if (batalla.getAtacante() == pokemonRival) {
                        mensajeLog += "\n" + batalla.realizarAtaque();
                    }
                }
            }
        } catch (ExcepcionPokebolaInsuficiente e) {
            mensajeLog = e.getMessage();
        }
        
        inicializarPokebolasDisponibles();
        estadoActual = EstadoMenu.MENU_PRINCIPAL;
    }
    
    /**
     * Método público para cambiar el pokemon del jugador durante la batalla.
     */
    public void cambiarPokemonJugador(Pokemon nuevoPokemon) {
        pokemonJugador = nuevoPokemon;
        batalla.cambiarPokemonJugador(nuevoPokemon);
        inicializarAtaquesDisponibles();
        
        // Recargar sprite del pokemon
        if (spriteJugador != null) {
            spriteJugador.dispose();
        }
        spriteJugador = cargarSpritePara(pokemonJugador);
        
        mensajeLog = "¡Has cambiado a " + nuevoPokemon.getNombre() + "!";
    }

    /**
     * Verifica si se derrotó un Pokemon legendario y reproduce la música de ganador si ambos han sido derrotados.
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
                
                // Si ambos pokémons legendarios han sido derrotados, reproducir música de ganador
                if (game.esJuegoCompleto()) {
                    GestorMusica.reproducirMusica(GestorMusica.TipoMusica.GANADOR);
                    System.out.println("¡HAS COMPLETADO EL JUEGO! ¡Has derrotado a ambos pokémons legendarios!");
                    mensajeLog = "¡FELICIDADES!\nHas derrotado a ambos\npokémons legendarios!\n¡HAS COMPLETADO EL JUEGO!";
                } else {
                    // Solo se derrotó uno de los dos
                    System.out.println("¡Has derrotado a " + nombre + "! Te falta derrotar al otro pokémon legendario.");
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
        // El pokemonJugador es una referencia directa al Pokémon en la lista del jugador,
        // por lo que los cambios en su vida ya están guardados automáticamente.
        // Sin embargo, sincronizamos la vida en la lista por si acaso cambió de Pokémon.
        
        // Sincronizar la vida del Pokémon en la lista de capturados
        List<Pokemon> pokemonsCapturados = game.getJugador().getSistemaCaptura().getPokemonsCapturados();
        int vidaActual = pokemonJugador.getVida();
        
        // Buscar el Pokémon en la lista y sincronizar su vida
        for (Pokemon p : pokemonsCapturados) {
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
        if (spriteJugador != null) {
            spriteJugador.dispose();
        }
        if (spriteRival != null) {
            spriteRival.dispose();
        }
        font.dispose();
        fontPequeña.dispose();
    }
}
