package com.Proyecto.Pokemon;

import com.Proyecto.Pokemon.jugador.Player;
import com.Proyecto.Pokemon.gui.PantallaDeInicio;
import com.Proyecto.Pokemon.pokemon.Pokemon;
import com.Proyecto.Pokemon.pokemon.PokeFuego;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Clase principal que inicializa el juego y gestiona el ciclo de vida de la
 * aplicacion.
 * Extiende de {@link com.badlogic.gdx.Game} para manejar diferentes pantallas.
 */
public class Main extends Game {
    /**
     * SpriteBatch utilizado para dibujar todas las texturas en pantalla.
     * Se declara publico para que las diferentes pantallas puedan acceder a el.
     */
    public SpriteBatch batch;
    private Player jugador;
    private Pokemon pokemonInicial;
    
    // Sistema de rastreo de pokémons legendarios derrotados
    private boolean dracorneaDerrotado = false;
    private boolean aethergonDerrotado = false;

    @Override
    public void create() {
        // Se inicializa el SpriteBatch para el dibujado de graficos.
        batch = new SpriteBatch();

        // Inicializamos el jugador de forma global para persistir su inventario.
        jugador = new Player(10, 10);

        // Pokemon inicial por defecto (se sobreescribirá en PantallaDeEleccion)
        // Usamos un placeholder temporalmente
        pokemonInicial = new PokeFuego.Ignirrojo("Macho");

        // Se establece la pantalla inicial del juego.
        setScreen(new PantallaDeInicio(this));
    }

    public Player getJugador() {
        return jugador;
    }

    public void setJugador(Player jugador) {
        this.jugador = jugador;
    }

    public Pokemon getPokemonInicial() {
        return pokemonInicial;
    }

    public void setPokemonInicial(Pokemon pokemon) {
        this.pokemonInicial = pokemon;
    }
    
    /**
     * Marca un Pokemon legendario como derrotado.
     * @param nombre Nombre del Pokemon legendario derrotado.
     */
    public void marcarLegendarioDerrotado(String nombre) {
        if (nombre.equals("Dracórnea") || nombre.equals("Dracornea")) {
            dracorneaDerrotado = true;
            System.out.println("¡Dracornea derrotado!");
        } else if (nombre.equals("Aethergon")) {
            aethergonDerrotado = true;
            System.out.println("¡Aethergon derrotado!");
        }
        
        // Si ambos pokémons legendarios han sido derrotados, el juego está completo
        if (dracorneaDerrotado && aethergonDerrotado) {
            System.out.println("¡HAS COMPLETADO EL JUEGO! ¡Has derrotado a ambos pokémons legendarios!");
        }
    }
    
    /**
     * Verifica si ambos pokémons legendarios han sido derrotados.
     * @return true si ambos han sido derrotados, false en caso contrario.
     */
    public boolean esJuegoCompleto() {
        return dracorneaDerrotado && aethergonDerrotado;
    }
    
    public boolean isDracorneaDerrotado() {
        return dracorneaDerrotado;
    }
    
    public boolean isAethergonDerrotado() {
        return aethergonDerrotado;
    }

    @Override
    public void dispose() {
        // Se liberan los recursos del SpriteBatch al cerrar la aplicacion.
        batch.dispose();
        if (jugador != null) {
            jugador.dispose();
        }
    }
}
