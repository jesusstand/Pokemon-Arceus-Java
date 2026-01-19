package com.Proyecto.Pokemon;

import com.Proyecto.Pokemon.pokemon.Pokemon;
import com.Proyecto.Pokemon.sistema.CapturaPokemon;
import com.Proyecto.Pokemon.gui.GestorSpritesPokemon;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Pixmap;
import java.util.List;

/**
 * Pantalla dedicada para ver los Pokemon capturados.
 * Muestra una lista de todos los Pokemon capturados con sus detalles.
 */
public class PantallaPokemonCapturados implements Screen {

    private Main game;
    private Screen pantallaAnterior;
    private CapturaPokemon sistemaCaptura;
    private OrthographicCamera camera;
    private BitmapFont font;
    private Texture pixel; // Textura de 1x1 para dibujar rectángulos
    
    private int pokemonSeleccionado = 0;
    private GestorSpritesPokemon gestorSprites;
    private boolean modoBatalla; // Si está en modo batalla, permite cambiar pokemon

    public PantallaPokemonCapturados(Main game, Screen pantallaAnterior) {
        this(game, pantallaAnterior, false);
    }
    
    public PantallaPokemonCapturados(Main game, Screen pantallaAnterior, boolean modoBatalla) {
        this.game = game;
        this.pantallaAnterior = pantallaAnterior;
        this.modoBatalla = modoBatalla;
        this.sistemaCaptura = game.getJugador().getSistemaCaptura();
        
        // Inicializar cámara
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Inicializar fuente
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        
        // Crear textura de 1x1 para dibujar rectángulos
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
        
        // Inicializar gestor de sprites
        gestorSprites = new GestorSpritesPokemon();
        
        List<Pokemon> pokemons = sistemaCaptura.getPokemonsCapturados();
        if (!pokemons.isEmpty()) {
            pokemonSeleccionado = 0;
        }
    }

    @Override
    public void render(float delta) {
        manejarInput();
        
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        dibujarPantalla();
    }
    
    private void manejarInput() {
        List<Pokemon> pokemons = sistemaCaptura.getPokemonsCapturados();
        
        if (pokemons.isEmpty()) {
            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                volver();
            }
            return;
        }
        
        // Navegar con flechas arriba/abajo
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            pokemonSeleccionado = (pokemonSeleccionado - 1 + pokemons.size()) % pokemons.size();
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            pokemonSeleccionado = (pokemonSeleccionado + 1) % pokemons.size();
        }
        
        // En modo batalla, ENTER selecciona el pokemon para cambiar
        if (modoBatalla && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            Pokemon seleccionado = pokemons.get(pokemonSeleccionado);
            // Si la pantalla anterior es PantallaBatalla, cambiar el pokemon
            if (pantallaAnterior instanceof PantallaBatalla) {
                PantallaBatalla batalla = (PantallaBatalla) pantallaAnterior;
                batalla.cambiarPokemonJugador(seleccionado);
                volver();
            } else {
                volver();
            }
        } else if (!modoBatalla && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            // Si NO está en modo batalla, ENTER también permite salir
            volver();
        } else if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            volver();
        }
    }
    
    private void dibujarPantalla() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        
        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
        game.batch.begin();
        
        // Fondo oscuro semi-transparente
        game.batch.setColor(0, 0, 0, 0.9f);
        game.batch.draw(pixel, 0, 0, sw, sh);
        game.batch.setColor(1, 1, 1, 1);
        
        List<Pokemon> pokemons = sistemaCaptura.getPokemonsCapturados();
        
        // Título
        font.setColor(Color.YELLOW);
        font.getData().setScale(2.5f);
        String titulo = "POKEMON CAPTURADOS";
        GlyphLayout layoutTitulo = new GlyphLayout(font, titulo);
        font.draw(game.batch, titulo, (sw - layoutTitulo.width) / 2f, sh - 50);
        
        if (pokemons.isEmpty()) {
            // Mensaje si no hay Pokemon
            font.setColor(Color.WHITE);
            font.getData().setScale(2.0f);
            String mensaje = "No has capturado ningún Pokemon aún";
            GlyphLayout layoutMensaje = new GlyphLayout(font, mensaje);
            font.draw(game.batch, mensaje, (sw - layoutMensaje.width) / 2f, sh / 2f);
            
            font.getData().setScale(1.5f);
            String instruccion = "Presiona ESCAPE o ENTER para volver";
            GlyphLayout layoutInst = new GlyphLayout(font, instruccion);
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
                
                // Sprite del Pokemon (pequeño en la lista)
                Texture spritePokemon = gestorSprites.obtenerSprite(p.getNombre());
                float spriteSize = itemHeight - 10;
                float nombreX = listaX + 15;
                if (spritePokemon != null) {
                    game.batch.draw(spritePokemon, nombreX, itemY - spriteSize, spriteSize, spriteSize);
                    nombreX += spriteSize + 10;
                }
                
                // Nombre del Pokemon
                font.setColor(index == pokemonSeleccionado ? Color.YELLOW : Color.WHITE);
                String nombreTexto = (index + 1) + ". " + p.getNombre();
                font.draw(game.batch, nombreTexto, nombreX, itemY);
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
                
                // Sprite del Pokemon grande
                Texture spritePokemon = gestorSprites.obtenerSprite(p.getNombre());
                if (spritePokemon != null) {
                    float spriteSize = 250f;
                    float spriteX = detallesX + (detallesW - spriteSize) / 2f;
                    float spriteY = detallesY - spriteSize - 40;
                    game.batch.draw(spritePokemon, spriteX, spriteY, spriteSize, spriteSize);
                }
                
                // Información del Pokemon
                float yPos = detallesY - 350;
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
                font.draw(game.batch, p.getTipoString(), detallesX + 200, yPos);
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
                font.draw(game.batch, p.getVida() + " / " + p.getVidaMaxima(), detallesX + 200, yPos);
                yPos -= 50;
                
                // Barra de vida visual
                float barraX = detallesX + 200;
                float barraY = yPos - 20;
                float barraW = 300;
                float barraH = 25;
                
                // Fondo de la barra
                game.batch.setColor(0.3f, 0.3f, 0.3f, 1f);
                game.batch.draw(pixel, barraX, barraY, barraW, barraH);
                
                // Barra de vida
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
            
            // Instrucciones
            font.setColor(Color.LIGHT_GRAY);
            font.getData().setScale(1.2f);
            String instrucciones;
            if (modoBatalla) {
                instrucciones = "↑ ↓: Navegar | ENTER: Cambiar Pokemon | ESCAPE: Cancelar";
            } else {
                instrucciones = "↑ ↓: Navegar | ESCAPE o ENTER: Volver";
            }
            GlyphLayout layoutInst = new GlyphLayout(font, instrucciones);
            font.draw(game.batch, instrucciones, (sw - layoutInst.width) / 2f, 40);
            
            // Contador
            font.setColor(Color.CYAN);
            String contador = "Total: " + pokemons.size() + " Pokemon";
            GlyphLayout layoutCont = new GlyphLayout(font, contador);
            font.draw(game.batch, contador, sw - layoutCont.width - 30, 40);
        }
        
        game.batch.end();
        game.batch.setProjectionMatrix(camera.combined);
    }
    
    private void volver() {
        game.setScreen(pantallaAnterior);
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
        font.dispose();
        if (pixel != null) {
            pixel.dispose();
        }
        gestorSprites.dispose();
    }
}
