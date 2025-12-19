package com.Proyecto.Pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;

/**
 * Representa al jugador (personaje) controlado por el usuario.
 * Maneja la posicion, el movimiento y el renderizado del personaje.
 */
public class Player {
    private Texture texture;
    private Animation<TextureRegion> walkUp, walkDown, walkLeft, walkRight;
    private TextureRegion currentFrame;

    // Posicion actual suave (dibujado)
    private Vector2 posicion;
    // Posicion objetivo en el grid (logica)
    private Vector2 destino;
    // Direccion hacia la que mira el jugador (para interactuar)
    private Vector2 direccionMirada = new Vector2(0, -1); // Por defecto mirando abajo

    // Velocidad de movimiento en tiles por segundo
    private final float VELOCIDAD = 5f;
    private boolean moviendose = false;
    private float stateTime = 0f;

    /**
     * Constructor del jugador.
     *
     * @param x Posicion inicial X en tiles.
     * @param y Posicion inicial Y en tiles.
     */
    public Player(float x, float y) {
        this.posicion = new Vector2(x, y);
        this.destino = new Vector2(x, y);
        texture = new Texture(Gdx.files.internal("player_sprite.png"));

        // Se asume una hoja de sprites de 4x4 (4 columnas, 4 filas)
        // Fila 0: Abajo, Fila 1: Izquierda, Fila 2: Derecha, Fila 3: Arriba
        // Calculamos el tamaño del tile dividiendo el ancho/alto total
        int frameCols = 4;
        int frameRows = 4;
        int tileWidth = texture.getWidth() / frameCols;
        int tileHeight = texture.getHeight() / frameRows;

        TextureRegion[][] tmp = TextureRegion.split(texture, tileWidth, tileHeight);

        // Creamos las animaciones (0.15f es la duracion de cada frame)
        // Importante: Ajustar filas segun el orden del sprite sheet generado.
        // El prompt pidió: Row 1 (0): Down, Row 2 (1): Left, Row 3 (2): Right, Row 4
        // (3): Up.
        walkDown = new Animation<>(0.15f, tmp[0]);
        walkLeft = new Animation<>(0.15f, tmp[1]);
        walkRight = new Animation<>(0.15f, tmp[2]);
        walkUp = new Animation<>(0.15f, tmp[3]);

        // Estado inicial
        currentFrame = tmp[0][0];
    }

    /**
     * Actualiza la logica del jugador. Se llama en cada frame.
     *
     * @param delta Tiempo transcurrido desde el ultimo frame.
     * @param mapa  Referencia al mapa para comprobar colisiones.
     */
    public void update(float delta, Mapa mapa) {
        stateTime += delta;

        if (moviendose) {
            // Movimiento constante hacia el destino
            float distanciaTotal = posicion.dst(destino);
            float distanciaMover = VELOCIDAD * delta;

            if (distanciaMover >= distanciaTotal) {
                // Hemos llegado al destino
                posicion.set(destino);
                moviendose = false;
                mapa.revisarPortales(posicion.x, posicion.y);
                // Revisamos la entrada aqui mismo para encadenar el movimiento si el jugador
                // sigue pulsando.
                revisarEntrada(mapa);
            } else {
                // Movemos hacia el destino normalizando el vector direccion y escalando por la
                // distancia a mover
                Vector2 direccion = new Vector2(destino).sub(posicion).nor();
                posicion.mulAdd(direccion, distanciaMover);
            }
        } else {
            // Si no nos movemos, revisamos si hay input para empezar a movernos
            revisarEntrada(mapa);
            // Reseteamos el stateTime para que la animacion empiece desde el inicio al
            // volver a caminar
            // (Opcional, a veces se ve mejor si sigue el ciclo, pero resetear previene
            // "deslizamiento" estatico)
            // stateTime = 0; // Comentado, a gusto del usuario
        }
    }

    /**
     * Revisa el input del usuario y determina la direccion del movimiento.
     *
     * @param mapa El mapa para verificar colisiones.
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

        // Interaccion
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            // Calculamos la tile objetivo frente al jugador
            float targetX = destino.x + direccionMirada.x;
            float targetY = destino.y + direccionMirada.y;
            mapa.interactuar(targetX, targetY);
        }
    }

    /**
     * Intenta mover al personaje una casilla en la direccion especificada.
     *
     * @param dx   Cambio en X.
     * @param dy   Cambio en Y.
     * @param mapa Mapa para comprobar si la casilla destino es solida.
     */
    private void intentarMover(int dx, int dy, Mapa mapa) {
        // Actualizamos la direccion de la mirada siempre, aunque haya colision
        direccionMirada.set(dx, dy);

        int proximoX = (int) (destino.x + dx); // Usamos destino.x para calcular el siguiente
        int proximoY = (int) (destino.y + dy);

        if (!mapa.esSolido(proximoX, proximoY)) {
            destino.set(proximoX, proximoY);
            moviendose = true;
        }
    }

    /**
     * Dibuja al personaje en la pantalla.
     *
     * @param batch El SpriteBatch utilizado para dibujar.
     */
    public void draw(SpriteBatch batch) {
        // Seleccionamos la animacion segun la direccion de la mirada
        Animation<TextureRegion> animacionActual;

        // Prioridad: Up/Down > Left/Right en caso de diagonal (aunque aqui es grid
        // based puro)
        if (direccionMirada.y > 0)
            animacionActual = walkUp;
        else if (direccionMirada.y < 0)
            animacionActual = walkDown;
        else if (direccionMirada.x > 0)
            animacionActual = walkRight;
        else
            animacionActual = walkLeft; // Default left or last known

        // Si se esta moviendo, usamos el stateTime global.
        // Si ESTA QUIETO, forzamos el frame 0 (idle) de esa animacion.
        if (moviendose) {
            currentFrame = animacionActual.getKeyFrame(stateTime, true); // looping
        } else {
            currentFrame = animacionActual.getKeyFrames()[0]; // Frame estatico (idle)
        }

        batch.draw(currentFrame, posicion.x, posicion.y, 1f, 1f);
    }

    public float getX() {
        return posicion.x;
    }

    public float getY() {
        return posicion.y;
    }

    /** Libera los recursos graficos. */
    public void dispose() {
        texture.dispose();
    }
}
