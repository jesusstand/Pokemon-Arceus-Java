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

    public NPC(float x, float y, TextureRegion textura, String mensaje, Color color, String tipo) {
        this.posicion = new Vector2(x, y);
        this.textura = textura;
        this.mensaje = mensaje;
        this.color = color != null ? color : Color.WHITE;
        this.tipo = tipo;
        // Asumimos tamaño de tile estandar (1 unidad de mundo) o el de la textura
        this.solido = true;
    }

    public void render(SpriteBatch batch) {
        if (textura != null) {
            Color oldParams = batch.getColor();
            batch.setColor(this.color);
            // Dibujamos al NPC (Asumiendo 1 unidad de mundo = 16px, ajusta si es necesario)
            batch.draw(textura, posicion.x, posicion.y, 1f, 1f);
            batch.setColor(oldParams);
        }
    }

    public void interactuar() {
        if ("Enemigo".equalsIgnoreCase(tipo)) {
            System.out.println("A PELEAR");
        } else {
            System.out.println("NPC dice: " + mensaje);
        }
    }

    public Vector2 getPosicion() {
        return posicion;
    }

    public boolean isSolido() {
        return solido;
    }
}
