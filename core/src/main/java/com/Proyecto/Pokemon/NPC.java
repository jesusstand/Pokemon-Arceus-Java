package com.Proyecto.Pokemon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.math.Vector2;

/**
 * Representa un Personaje No Jugable (NPC) en el mundo.
 */
public class NPC {
    private Vector2 posicion;
    private TextureRegion textura;
    private String mensaje;
    private Color color;
    private String tipo; // "Enemigos", "Civil", etc.

    // Si queremos que el NPC sea solido (bloque el paso)
    private boolean solido;

    /**
     * Constructor del NPC.
     *
     * @param x       Posicion X inicial.
     * @param y       Posicion Y inicial.
     * @param textura Textura o region de textura a dibujar.
     * @param mensaje Mensaje que dice el NPC al interactuar.
     * @param color   Tinte de color opcional (puede ser null).
     * @param tipo    Tipo de NPC (ej: "Enemigo", "Civil").
     */
    public NPC(float x, float y, TextureRegion textura, String mensaje, Color color, String tipo) {
        this.posicion = new Vector2(x, y);
        this.textura = textura;
        this.mensaje = mensaje;
        this.color = color != null ? color : Color.WHITE;
        this.tipo = tipo;
        // Asumimos tamaño de tile estandar (1 unidad de mundo) o el de la textura
        this.solido = true;
    }

    /**
     * Dibuja el NPC en la pantalla usando el batch proporcionado.
     *
     * @param batch SpriteBatch para renderizar.
     */
    public void render(SpriteBatch batch) {
        if (textura != null) {
            Color oldParams = batch.getColor();
            batch.setColor(this.color);
            // Dibujamos al NPC (Asumiendo 1 unidad de mundo = 16px, ajusta si es necesario)
            batch.draw(textura, posicion.x, posicion.y, 1f, 1f);
            batch.setColor(oldParams);
        }
    }

    /**
     * Obtiene el mensaje del NPC.
     *
     * @return El mensaje de dialogo.
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Obtiene el tipo del NPC.
     *
     * @return Tipo de NPC.
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Metodo de utilidad para interacciones (deprecado/movido a Mapa).
     */
    public void interactuar() {
        // La logica ahora se maneja en Mapa.java para mostrar UI
    }

    /**
     * Obtiene la posicion actual del NPC.
     *
     * @return Vector2 con las coordenadas.
     */
    public Vector2 getPosicion() {
        return posicion;
    }

    /**
     * Verifica si el NPC bloquea el movimiento.
     *
     * @return true si es solido.
     */
    public boolean isSolido() {
        return solido;
    }
}
