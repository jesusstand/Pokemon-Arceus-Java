package com.Proyecto.Pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;

/**
 * Representa al jugador controlado por el usuario.
 * Gestiona la posicion en el mundo, las animaciones de movimiento y la
 * interaccion con el mapa.
 */
public class Player {
    private Texture texture;
    private Animation<TextureRegion> walkUp, walkDown, walkLeft, walkRight;
    private TextureRegion currentFrame;

    // Posicion actual suave para el dibujado (coordenadas de mundo).
    private Vector2 posicion;
    // Posicion objetivo en la cuadricula (grid) para la logica de movimiento.
    private Vector2 destino;
    // Direccion hacia la que mira el jugador, util para interactuar con objetos.
    private Vector2 direccionMirada = new Vector2(0, -1); // Por defecto mira hacia abajo.

    // Velocidad de desplazamiento en baldosas (tiles) por segundo.
    private final float VELOCIDAD = 5f;
    private boolean moviendose = false;
    private float stateTime = 0f;

    /**
     * Constructor del jugador.
     *
     * @param x Posicion inicial X en el grid.
     * @param y Posicion inicial Y en el grid.
     */
    public Player(float x, float y) {
        this.posicion = new Vector2(x, y);
        this.destino = new Vector2(x, y);
        texture = new Texture(Gdx.files.internal("player_sprite.png"));

        // Configuracion de la hoja de sprites (asumimos 4x4).
        int frameCols = 4;
        int frameRows = 4;
        int tileWidth = texture.getWidth() / frameCols;
        int tileHeight = texture.getHeight() / frameRows;

        TextureRegion[][] tmp = TextureRegion.split(texture, tileWidth, tileHeight);

        // Creamos las animaciones con un intervalo de 0.15 segundos por cuadro.
        // Las filas corresponden a: 0-Abajo, 1-Izquierda, 2-Derecha, 3-Arriba.
        walkDown = new Animation<>(0.15f, tmp[0]);
        walkLeft = new Animation<>(0.15f, tmp[1]);
        walkRight = new Animation<>(0.15f, tmp[2]);
        walkUp = new Animation<>(0.15f, tmp[3]);

        // Frame inicial (estatico mirando abajo).
        currentFrame = tmp[0][0];
    }

    /**
     * Actualiza el estado del jugador en cada frame.
     *
     * @param delta Tiempo transcurrido desde el ultimo frame.
     * @param mapa  Referencia al mapa para comprobar colisiones y eventos.
     */
    public void update(float delta, Mapa mapa) {
        stateTime += delta;

        if (moviendose) {
            // Movimiento fluido hacia la posicion destino.
            float distanciaTotal = posicion.dst(destino);
            float distanciaMover = VELOCIDAD * delta;

            if (distanciaMover >= distanciaTotal) {
                // El jugador ha llegado a la baldosa de destino.
                posicion.set(destino);
                moviendose = false;
                // Comprobamos si hay algun portal en la nueva posicion.
                mapa.revisarPortales(posicion.x, posicion.y);
                // Si el usuario mantiene pulsada una tecla, continuamos el movimiento.
                revisarEntrada(mapa);
            } else {
                // Seguir desplazandose hacia el destino.
                Vector2 direccion = new Vector2(destino).sub(posicion).nor();
                posicion.mulAdd(direccion, distanciaMover);
            }
        } else {
            // Si esta quieto, esperamos a que el usuario pulse una tecla.
            revisarEntrada(mapa);
        }
    }

    /**
     * Procesa las entradas del teclado para mover al jugador o interactuar.
     *
     * @param mapa Referencia al mapa para validar el movimiento.
     */
    private void revisarEntrada(Mapa mapa) {
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.UP))
            intentarMover(0, 1, mapa);
        else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.DOWN))
            intentarMover(0, -1, mapa);
        else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT))
            intentarMover(-1, 0, mapa);
        else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT))
            intentarMover(1, 0, mapa);

        // Accion al pulsar la tecla ENTER.
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            // Primero intentamos interactuar con la posicion actual (pisando el objeto).
            boolean interactuado = mapa.interactuar(destino.x, destino.y);

            if (!interactuado) {
                // Si no hay nada debajo, probamos con la baldosa que tenemos en frente.
                float targetX = destino.x + direccionMirada.x;
                float targetY = destino.y + direccionMirada.y;
                mapa.interactuar(targetX, targetY);
            }
        }
    }

    /**
     * Valida e inicia el movimiento hacia una nueva baldosa.
     *
     * @param dx   Incremento en el eje X.
     * @param dy   Incremento en el eje Y.
     * @param mapa Mapa para verificar si la baldosa destino es accesible.
     */
    private void intentarMover(int dx, int dy, Mapa mapa) {
        // Actualizamos la direccion de mirada siempre (gira el personaje aunque no
        // camine).
        direccionMirada.set(dx, dy);

        int proximoX = (int) (destino.x + dx);
        int proximoY = (int) (destino.y + dy);

        // Solo movemos si la baldosa no es solida.
        if (!mapa.esSolido(proximoX, proximoY)) {
            destino.set(proximoX, proximoY);
            moviendose = true;
        }
    }

    /**
     * Dibuja al jugador aplicando la animacion correspondiente.
     *
     * @param batch El SpriteBatch para el dibujado.
     */
    public void draw(SpriteBatch batch) {
        Animation<TextureRegion> animacionActual;

        // Determinamos la animacion segun la ultima direccion de mirada.
        if (direccionMirada.y > 0)
            animacionActual = walkUp;
        else if (direccionMirada.y < 0)
            animacionActual = walkDown;
        else if (direccionMirada.x > 0)
            animacionActual = walkRight;
        else
            animacionActual = walkLeft;

        // Seleccionamos el cuadro de animacion (estatico si esta quieto).
        if (moviendose) {
            currentFrame = animacionActual.getKeyFrame(stateTime, true);
        } else {
            currentFrame = animacionActual.getKeyFrames()[0];
        }

        // Dibujamos el personaje ocupando 1x1 unidades de mundo.
        batch.draw(currentFrame, posicion.x, posicion.y, 1f, 1f);
    }

    public float getX() {
        return posicion.x;
    }

    public float getY() {
        return posicion.y;
    }

    /**
     * Libera la textura del personaje al cerrar el juego.
     */
    public void dispose() {
        texture.dispose();
    }
}
